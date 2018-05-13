package gp.separation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import gp.Domain;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * An inferencer that simply iterates over a list of given predicates and
 * returns the first one that separates.
 * 
 * @author romanm
 */
public class LinearInferencer<ValueType extends Value, UpdateType extends Update, GuardType extends Guard>
		extends ConditionInferencer<ValueType, UpdateType, GuardType> {
	private final List<GuardType> guards;

	public LinearInferencer(Domain<ValueType, UpdateType, GuardType> domain, List<GuardType> guards) {
		super(domain);
		this.guards = guards;
	}

	@Override
	public Optional<GuardType> infer(Collection<? extends Value> first, Collection<? extends Value> second) {
		for (var guard : guards) {
			var separates = true;
			for (var val1 : first) {
				@SuppressWarnings("unchecked")
				var val1Typed = (ValueType) val1;
				if (!domain.test(guard, val1Typed)) {
					separates = false;
					break;
				}
			}
			if (!separates) {
				continue;
			}
			for (var val2 : second) {
				@SuppressWarnings("unchecked")
				var val2Typed = (ValueType) val2;
				if (domain.test(guard, val2Typed)) {
					separates = false;
					break;
				}
			}
			if (separates) {
				return Optional.of(guard);
			}
		}
		return Optional.empty();
	}
}