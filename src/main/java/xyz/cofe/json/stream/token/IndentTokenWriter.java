package xyz.cofe.json.stream.token;

import java.util.LinkedList;

public class IndentTokenWriter implements TokenWriter {
    private final TokenWriter output;

    public IndentTokenWriter(TokenWriter output) {
        this.output = output;
    }

    private int level = 0;

    public interface State {
        int getItemsCount();
        void incItemsCount();
    }
    public static class ArrState implements State {
        public int itemsCount = 0;

        @Override
        public int getItemsCount() {
            return itemsCount;
        }

        public void incItemsCount(){
            itemsCount++;
        }
    }
    public static class ObjState implements State {
        public int itemsCount = 0;

        @Override
        public int getItemsCount() {
            return itemsCount;
        }

        public void incItemsCount(){
            itemsCount++;
        }
    }

    private final LinkedList<State> state = new LinkedList<>();

    private static final StringPointer emptyPtr = new StringPointer("",0);
    private static final Whitespace newLine;
    static {
        newLine = new Whitespace<>("\n", emptyPtr, emptyPtr);
    }

    private void writeNewLine(){
        output.write(newLine);
    }
    private void writeIndent(int level){
        if( level>0 )output.write(indentWhitespace(level));
    }
    private void writeIndent(){
        writeIndent(state.size());
    }
    private void writeWhiteSpace(int count){
        if( count>0 )output.write(whitespace(count));
    }

    @SuppressWarnings({"rawtypes"})
    private Whitespace indentWhitespace(int level){
        return whitespace(level*2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Whitespace whitespace(int count){
        if( count<=0 )return new Whitespace("", emptyPtr, emptyPtr);
        return new Whitespace(" ".repeat(count), emptyPtr, emptyPtr);
    }

    @Override
    public void write(OpenParentheses<?> token) {
        incItems();

        var level = state.size();
        state.push(new ObjState());

        output.write(token);
        writeNewLine();
    }

    @Override
    public void write(CloseParentheses<?> token) {
        state.poll();

        writeNewLine();
        writeIndent();

        output.write(token);
    }

    @Override
    public void write(OpenSquare<?> token) {
        incItems();

        state.push(new ArrState());

        output.write(token);
        writeNewLine();
    }

    @Override
    public void write(CloseSquare<?> token) {
        state.poll();

        writeNewLine();
        writeIndent();

        output.write(token);
    }

    private void incItems(){
        var st = state.poll();
        if( st!=null ) {
            state.push(st);
            st.incItemsCount();
            var cnt = st.getItemsCount();
            if( st instanceof ObjState && (cnt % 2 == 1) ){
                writeIndent(state.size());
            }else if( st instanceof ArrState ){
                writeIndent(state.size());
            }
        }
    }

    @Override
    public void write(BigIntToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(LongToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(IntToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(DoubleToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(StringToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(FalseToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(TrueToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(NullToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(IdentifierToken<?> token) {
        incItems();
        output.write(token);
    }

    @Override
    public void write(Colon<?> token) {
        output.write(token);
        writeWhiteSpace(1);
    }

    @Override
    public void write(Comma<?> token) {
        output.write(token);
        writeNewLine();
    }

    @Override
    public void write(MLComment<?> token) {output.write(token);}

    @Override
    public void write(SLComment<?> token) {output.write(token);}

    @Override
    public void write(Whitespace<?> token) {output.write(token);}
}
