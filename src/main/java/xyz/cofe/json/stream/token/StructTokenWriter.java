package xyz.cofe.json.stream.token;

public class StructTokenWriter implements TokenWriter {
    private final TokenWriter tokenWriter;

    public StructTokenWriter(TokenWriter tokenWriter) {
        this.tokenWriter = tokenWriter;
    }

    @Override
    public void write(BigIntToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(LongToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(IntToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(DoubleToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(StringToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(FalseToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(TrueToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(NullToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(IdentifierToken<?> token) {tokenWriter.write(token);}

    @Override
    public void write(OpenParentheses<?> token) {tokenWriter.write(token);}

    @Override
    public void write(CloseParentheses<?> token) {tokenWriter.write(token);}

    @Override
    public void write(OpenSquare<?> token) {tokenWriter.write(token);}

    @Override
    public void write(CloseSquare<?> token) {tokenWriter.write(token);}

    @Override
    public void write(Colon<?> token) {tokenWriter.write(token);}

    @Override
    public void write(Comma<?> token) {tokenWriter.write(token);}

    @Override
    public void write(MLComment<?> token) {tokenWriter.write(token);}

    @Override
    public void write(SLComment<?> token) {tokenWriter.write(token);}

    @Override
    public void write(Whitespace<?> token) {tokenWriter.write(token);}
}
