package xyz.cofe.json.stream.token;

/**
 * Интерфейс записи лексем
 */
public interface TokenWriter {
    void write(BigIntToken<?> token);
    void write(LongToken<?> token);
    void write(IntToken<?> token);
    void write(DoubleToken<?> token);
    void write(StringToken<?> token);
    void write(FalseToken<?> token);
    void write(TrueToken<?> token);
    void write(NullToken<?> token);
    void write(IdentifierToken<?> token);
    void write(OpenParentheses<?> token);
    void write(CloseParentheses<?> token);
    void write(OpenSquare<?> token);
    void write(CloseSquare<?> token);
    void write(Colon<?> token);
    void write(Comma<?> token);
    void write(MLComment<?> token);
    void write(SLComment<?> token);
    void write(Whitespace<?> token);
}
