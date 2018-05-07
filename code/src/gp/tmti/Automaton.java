package gp.tmti;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import bgu.cs.util.Pair;
import bgu.cs.util.graph.HashMultiGraph;
import bgu.cs.util.rel.HashRel2;
import gp.Domain.Update;

/**
 * A program automaton.
 * 
 * @author romanm
 */
public class Automaton extends HashMultiGraph<State, Action> {
	/**
	 * The initial state.
	 */
	private State entry;

	/**
	 * The final state.
	 */
	private State exit;

	public Automaton() {
		entry = new State("initial");
		exit = new State("final");
		addNode(entry);
		addNode(exit);
	}

	public State getInitial() {
		return entry;
	}

	public State getFinal() {
		return exit;
	}

	/**
	 * Returns a deep copy of this automaton.
	 */
	@Override
	public Automaton clone() {
		var result = new Automaton();
		var newStates = new ArrayList<State>(this.getNodes().size());
		var oldStateToNewState = new HashMap<State, State>(this.getNodes().size());
		// Create a copy of each state with a copy of the trace points.
		for (var oldState : getNodes()) {
			final State newState;
			if (oldState == getInitial()) {
				newState = result.getInitial();
			} else if (oldState == getFinal()) {
				newState = result.getFinal();
			} else {
				newState = new State(oldState.id);
			}
			newStates.add(newState);
			newState.addAllTracePoints(oldState.getPoints());
			result.addNode(newState);
			oldStateToNewState.put(oldState, newState);
		}

		// Now copy the transitions over to the new automaton
		for (var oldState : getNodes()) {
			for (Edge<State, Action> transition : this.succEdges(oldState)) {
				var newSrc = oldStateToNewState.get(transition.getSrc());
				var newDst = oldStateToNewState.get(transition.getDst());
				var newAction = transition.getLabel().clone();
				result.addEdge(newSrc, newDst, newAction);
			}
		}

		return result;
	}

	public Optional<Pair<Action, State>> findTransition(State src, Update update) {
		Action foundAction = null;
		State foundState = null;
		for (Edge<State, Action> transition : succEdges(src)) {
			Action transitionAction = transition.getLabel();
			if (transitionAction.update.equals(update)) {
				foundAction = transitionAction;
				foundState = transition.getDst();
				break;
			}
		}
		if (foundAction == null) {
			return Optional.empty();
		} else {
			return Optional.of(new Pair<>(foundAction, foundState));
		}
	}

	/**
	 * Tests whether all states are update-deterministic.
	 */
	public boolean isUpdateDeterministic() {
		for (final var state : this.getNodes()) {
			if (!isUpdateDeterministic(state)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests whether all outgoing transitions are labeled by unique updates.
	 */
	public boolean isUpdateDeterministic(State state) {
		final var stateUpdates = new HashSet<Update>();
		for (final var transition : this.succEdges(state)) {
			final var freshUpdate = stateUpdates.add(transition.getLabel().update);
			if (!freshUpdate) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Recursively makes states update-deterministic, starting from the given state
	 * up to states that are already update-deterministic.
	 */
	public void fold(State state) {
		var change = makeUpdateDeterministic(state);
		if (!change) {
			return;
		}
		var succStates = new ArrayList<State>();
		for (Edge<State, Action> transition : this.succEdges(state)) {
			var succState = transition.getDst();
			if (succState != state) {
				succStates.add(succState);
			}
		}
		for (var succState : succStates) {
			fold(succState);
		}
	}

	public boolean makeUpdateDeterministic(State state) {
		if (!containsNode(state) || isUpdateDeterministic(state)) {
			return false;
		}
		var updateToTargetState = new HashRel2<Update, State>();
		for (Edge<State, Action> transition : this.succEdges(state)) {
			var transitionAction = transition.getLabel();
			updateToTargetState.add(transitionAction.update, transition.getDst());
		}
		for (var update : updateToTargetState.all1()) {
			var statesToFold = updateToTargetState.select1(update);
			mergeStates(statesToFold);
		}

		// Maintain a single edge per update.
		var updateToAction = new HashRel2<Update, Edge<State, Action>>();
		for (var transition : this.succEdges(state)) {
			var transitionAction = transition.getLabel();
			updateToAction.add(transitionAction.update, transition);
		}
		for (var update : updateToAction.all1()) {
			var updateEdges = updateToAction.select1(update);
			if (updateEdges.size() > 1) {
				var edgeiter = updateEdges.iterator();
				edgeiter.next(); // Skip the first edge
				while (edgeiter.hasNext()) {
					var edge = edgeiter.next();
					super.removeEdge(edge);
				}
			}
		}
		return true;
	}

	/**
	 * Destructively merges all states in the given collection into the first one
	 * (the first one returned by an iterator over the given collection). None of
	 * which can be the final state. States that are not in the automaton are
	 * ignored.
	 * 
	 * @return The state into which all states where merged or empty if all states
	 *         in the collection were not in the automaton.
	 */
	public Optional<State> mergeStates(Collection<State> states) {
		var statesList = new ArrayList<State>();
		for (var state : states) {
			if (containsNode(state)) {
				statesList.add(state);
			}
		}
		if (statesList.size() == 0) {
			return Optional.empty();
		}
		if (states.size() == 1) {
			return Optional.of(states.iterator().next());
		}

		// Ensure that if the list includes the initial state then it appears first
		// so that we don't attempt to merge it into another state.
		for (int i = 0; i < statesList.size(); ++i) {
			if (statesList.get(i) == this.getInitial()) {
				Collections.swap(statesList, i, 0);
				break;
			}
		}
		var firstState = statesList.get(0);
		for (int i = 1; i < statesList.size(); ++i) {
			var otherState = statesList.get(i);
			this.mergeStates(otherState, firstState);
		}
		return Optional.ofNullable(firstState);
	}

	/**
	 * Destructively merges the first state into the second, resulting in an
	 * automaton that is not necessarily update-deterministic.<br>
	 * Precondition: 1) Neither of the states may be the final state.<br>
	 * 2) The first state may not be the initial state.
	 */
	public void mergeStates(State src, State dst) {
		if (src == this.getFinal() || dst == this.getFinal()) {
			throw new Error("Attempt to merge states including a final state!");
		}
		if (src == this.getInitial()) {
			throw new Error("Attempt to merge the initial state into another state!");
		}

		if (src == dst) {
			return;
		}

		dst.addAllTracePoints(src.getPoints());
		super.mergeInto(src, dst);
		assert !containsNode(src);
		assert containsNode(dst);
	}

}
