package com.smartcity360.interpreter;

/**
 * Terminal expression 
 */
public class TerminalExpression implements Expression {

    private final String keyword;

    public TerminalExpression(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean interpret(String context) {
        return context.contains(keyword);
    }

  
    @Override
    public String toString() {
        return keyword;
    }
}

