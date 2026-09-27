package net.exmo.exmodifier.core.data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/** Tiny arithmetic evaluator for datapack expressions. Variables: level, l. */
public final class LevelExpression {
    private LevelExpression() {}

    public static double evaluate(String expression, int level) {
        String source = expression == null ? "" : expression.trim();
        if (source.isEmpty()) return 0.0D;
        try {
            return new Parser(source, level).parse();
        } catch (RuntimeException exception) {
            return 0.0D;
        }
    }

    private static final class Parser {
        private final String source;
        private final int level;
        private int index;

        private Parser(String source, int level) {
            this.source = source;
            this.level = level;
        }

        private double parse() {
            double value = parseAdd();
            skipSpaces();
            if (index != source.length()) throw new IllegalArgumentException("trailing input");
            return value;
        }

        private double parseAdd() {
            double value = parseMul();
            while (true) {
                skipSpaces();
                if (match('+')) value += parseMul();
                else if (match('-')) value -= parseMul();
                else return value;
            }
        }

        private double parseMul() {
            double value = parseUnary();
            while (true) {
                skipSpaces();
                if (match('*')) value *= parseUnary();
                else if (match('/')) {
                    double divisor = parseUnary();
                    value = divisor == 0.0D ? 0.0D : value / divisor;
                } else return value;
            }
        }

        private double parseUnary() {
            skipSpaces();
            if (match('+')) return parseUnary();
            if (match('-')) return -parseUnary();
            return parsePrimary();
        }

        private double parsePrimary() {
            skipSpaces();
            if (match('(')) {
                double value = parseAdd();
                skipSpaces();
                if (!match(')')) throw new IllegalArgumentException("missing )");
                return value;
            }
            if (Character.isDigit(peek()) || peek() == '.') return parseNumber();
            String ident = parseIdent();
            if ("level".equals(ident) || "l".equals(ident)) return level;
            throw new IllegalArgumentException("unknown ident " + ident);
        }

        private double parseNumber() {
            int start = index;
            while (index < source.length() && (Character.isDigit(source.charAt(index)) || source.charAt(index) == '.')) index++;
            return Double.parseDouble(source.substring(start, index));
        }

        private String parseIdent() {
            int start = index;
            while (index < source.length() && (Character.isLetter(source.charAt(index)) || source.charAt(index) == '_')) index++;
            if (start == index) throw new IllegalArgumentException("expected ident");
            return source.substring(start, index).toLowerCase(Locale.ROOT);
        }

        private void skipSpaces() {
            while (index < source.length() && Character.isWhitespace(source.charAt(index))) index++;
        }

        private boolean match(char expected) {
            if (peek() != expected) return false;
            index++;
            return true;
        }

        private char peek() {
            return index < source.length() ? source.charAt(index) : '\0';
        }
    }
}
