package com.smartcity360.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public final class RuleParser {

    private static final String OPEN = "(";
    private static final String CLOSE = ")";
    private static final String AND = "AND";
    private static final String OR = "OR";
    private static final String NOT = "NOT";

    private final List<String> tokens;
    private final Set<String> allowedKeywords;
    private int position;

    private RuleParser(List<String> tokens, Set<String> allowedKeywords) {
        this.tokens = tokens;
        this.allowedKeywords = allowedKeywords;
    }

    /**
     * @param ruleText      
     * @param allowedKeywords 
     * @throws RuleSyntaxException 
     */
    public static Expression parse(String ruleText, Set<String> allowedKeywords) {
        if (ruleText == null || ruleText.isBlank()) {
            throw new RuleSyntaxException("The rule is empty.");
        }
        RuleParser parser = new RuleParser(tokenize(ruleText), allowedKeywords);
        Expression result = parser.parseOr();
        if (parser.position < parser.tokens.size()) {
            throw new RuleSyntaxException("Unexpected \"" + parser.tokens.get(parser.position)
                    + "\" - expected AND, OR or the end of the rule.");
        }
        return result;
    }

    private static List<String> tokenize(String text) {
        List<String> result = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '(' || c == ')') {
                flush(word, result);
                result.add(String.valueOf(c));
            } else if (Character.isWhitespace(c)) {
                flush(word, result);
            } else if (Character.isLetterOrDigit(c) || c == '_') {
                word.append(c);
            } else {
                throw new RuleSyntaxException("Illegal character '" + c + "' in the rule.");
            }
        }
        flush(word, result);
        return result;
    }

    private static void flush(StringBuilder word, List<String> result) {
        if (word.length() > 0) {
            result.add(word.toString());
            word.setLength(0);
        }
    }

    private Expression parseOr() {
        Expression left = parseAnd();
        while (isNext(OR)) {
            position++;
            left = new OrExpression(left, parseAnd());
        }
        return left;
    }

    private Expression parseAnd() {
        Expression left = parseNot();
        while (isNext(AND)) {
            position++;
            left = new AndExpression(left, parseNot());
        }
        return left;
    }

    private Expression parseNot() {
        if (position >= tokens.size()) {
            throw new RuleSyntaxException("The rule ends too early - a keyword or \"(\" was expected.");
        }
        String token = tokens.get(position);
        if (token.equalsIgnoreCase(NOT)) {
            position++;
            return new NotExpression(parseNot());
        }
        if (OPEN.equals(token)) {
            position++;
            Expression inner = parseOr();
            if (!isNext(CLOSE)) {
                throw new RuleSyntaxException("A closing bracket \")\" is missing.");
            }
            position++;
            return inner;
        }
        return parseKeyword(token);
    }

    private Expression parseKeyword(String token) {
        if (CLOSE.equals(token) || token.equalsIgnoreCase(AND) || token.equalsIgnoreCase(OR)) {
            throw new RuleSyntaxException("Unexpected \"" + token + "\" - a keyword was expected.");
        }
        String keyword = token.toUpperCase();
        if (allowedKeywords != null && !allowedKeywords.contains(keyword)) {
            throw new RuleSyntaxException("Unknown keyword \"" + token + "\". Known keywords: "
                    + String.join(", ", new java.util.TreeSet<>(allowedKeywords)));
        }
        position++;
        return new TerminalExpression(keyword);
    }

    private boolean isNext(String expected) {
        return position < tokens.size() && tokens.get(position).equalsIgnoreCase(expected);
    }
}

