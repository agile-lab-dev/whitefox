package io.whitefox.core.types.predicates;

public class PredicateValidationException extends PredicateException {


    final private int actualNumOfChildren;
    final private Arity op;
    final private int expectedNumOfChildren;

    public PredicateValidationException(int actualNumOfChildren, Arity op, int expectedNumOfChildren) {
        this.actualNumOfChildren = actualNumOfChildren;
        this.op = op;
        this.expectedNumOfChildren = expectedNumOfChildren;
    }


    @Override
    public String getMessage() {
        if (op instanceof NaryOp)
            return op + " : expected at least " + expectedNumOfChildren + " children, but found " + actualNumOfChildren + " children";
        else
            return op + " : expected " + expectedNumOfChildren + " children, but found " + actualNumOfChildren + " children";
    }
}
