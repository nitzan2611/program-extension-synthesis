package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * An 'if-then-else' statement.
 * 
 * @author romanm
 */
public class IfStmt extends Stmt {
	public IfStmt(Node condNode, Node thenNode, Node elseNode) {
		super(condNode, thenNode, elseNode != null ? elseNode : SkipStmt.v);
	}
	public Node getCond() {
		return args.get(0);
	}

	public Node getThen() {
		return args.get(1);
	}

	public Node getElse() {
		return args.get(2);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	protected IfStmt(List<Node> args) {
		super(countNonterminals(args));
		assertNumOfArgs(3);
	}

	@Override
	public IfStmt clone(List<Node> args) {
		return new IfStmt(args);
	}
}