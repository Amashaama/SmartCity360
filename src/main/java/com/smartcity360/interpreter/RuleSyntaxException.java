package com.smartcity360.interpreter;

/** Thrown when the text of a policy rule is not valid in the mini-language. */
public class RuleSyntaxException extends IllegalArgumentException {

    public RuleSyntaxException(String message) {
        super(message);
    }
}

