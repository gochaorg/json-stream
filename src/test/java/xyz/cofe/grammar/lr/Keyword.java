package xyz.cofe.grammar.lr;

import java.util.Optional;

public enum Keyword {
    OpenParentheses,
    CloseParentheses,
    Plus,
    Minus,
    Multiple,
    Divide
    ;

    public static class Parser implements TokenParser<Keyword,StringSource> {
        @Override
        public Optional<ParsedToken<Keyword, StringSource>> input(StringSource src) {
            if( src==null ) throw new IllegalArgumentException("src==null");

            var chrOpt = src.get();
            if( chrOpt.isEmpty() )return Optional.empty();

            var chr = chrOpt.get();
            return switch (chr){
                case '+' -> Optional.of( new ParsedToken<>(Keyword.Plus, src, src.move(1)) );
                case '-' -> Optional.of( new ParsedToken<>(Keyword.Minus, src, src.move(1)) );
                case '*' -> Optional.of( new ParsedToken<>(Keyword.Multiple, src, src.move(1)) );
                case '/' -> Optional.of( new ParsedToken<>(Keyword.Divide, src, src.move(1)) );
                case '(' -> Optional.of( new ParsedToken<>(Keyword.OpenParentheses, src, src.move(1)) );
                case ')' -> Optional.of( new ParsedToken<>(Keyword.CloseParentheses, src, src.move(1)) );
                default -> Optional.empty();
            };
        }

        @Override
        public void reset() {
        }

        @Override
        public boolean hasState() {
            return false;
        }
    }
}
