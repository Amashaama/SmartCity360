package com.smartcity360.interpreter;


public class NotExpression implements Expression {

    private final Expression expression;

    public NotExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean interpret(String context) {
        return !expression.interpret(context);
    }

    @Override
    public String toString() {
        return "(NOT " + expression + ")";
    }
}

