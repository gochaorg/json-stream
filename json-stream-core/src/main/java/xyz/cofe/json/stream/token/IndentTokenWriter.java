package xyz.cofe.json.stream.token;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Записывает лексемы с учетом отступа/вложенности объектов
 * аля Pretty
 */
public class IndentTokenWriter implements TokenWriter {
    private final TokenWriter output;

    public IndentTokenWriter(TokenWriter output) {
        this.output = output;
    }

    private enum ItemType {
        JsObj,
        JsArr,
        BigInt,
        Long,
        Int,
        Double,
        String,
        Bool,
        Null,
        Ident
    }

    private static abstract class BaseState {
        public int itemsCount = 0;
        public int jsObjCount = 0;
        public int jsArrCount = 0;
        public int bigIntCount = 0;
        public int longCount = 0;
        public int intCount = 0;
        public int doubleCount = 0;
        public int stringCount = 0;
        public int boolCount = 0;
        public int nullCount = 0;
        public int identCount = 0;
        public int nestedWriteCount = 0;

        public int compoundCount() {
            return jsArrCount + jsObjCount;
        }

        public void addItem(ItemType type) {
            itemsCount++;
            switch (type) {
                case Int -> intCount++;
                case Bool -> boolCount++;
                case Long -> longCount++;
                case Null -> nullCount++;
                case Ident -> identCount++;
                case JsArr -> jsArrCount++;
                case JsObj -> jsObjCount++;
                case BigInt -> bigIntCount++;
                case Double -> doubleCount++;
                case String -> stringCount++;
            }
        }
    }
    private static class ArrState extends BaseState {
    }
    private static class ObjState extends BaseState {
    }

    private final LinkedList<BaseState> state = new LinkedList<>();

    private static final StringPointer emptyPtr = new StringPointer("", 0);

    @SuppressWarnings("rawtypes")
    private static final Whitespace NewLine;

    static {
        NewLine = new Whitespace<>("\n", emptyPtr, emptyPtr);
    }

    private void writeNewLine() {
        output.write(NewLine);
    }

    //region indent : String
    private String indent = "    ";

    public String indent() {return indent;}

    public IndentTokenWriter indent(String value) {
        if (value == null) throw new IllegalArgumentException("value==null");
        this.indent = value;
        return this;
    }
    //endregion

    private void writeIndent(int level) {
        if (level > 0) {
            output.write(new Whitespace<>(indent.repeat(level), emptyPtr, emptyPtr));
        }
    }

    private void writeIndent() {
        writeIndent(state.size());
    }

    @SuppressWarnings({"SameParameterValue"})
    private void writeWhiteSpace(int count) {
        if (count > 0) {
            output.write(new Whitespace<>(" ".repeat(count), emptyPtr, emptyPtr));
        }
    }

    //////////////////////////////

    private record WriteOptions() {}
    private final Queue<Consumer<WriteOptions>> writingQueue = new LinkedList<>();

    private void flush() {
        var wo = new WriteOptions();
        while (true) {
            var q = writingQueue.poll();
            if (q == null) break;
            q.accept(wo);
        }
    }

    private void flushIfTop() {
        if (state.isEmpty()) {
            flush();
        }
    }

    //////////////////////////////

    @Override
    public void write(OpenParentheses<?> token) {
        addNestedItem(ItemType.JsObj);

        var obj = new ObjState();
        state.push(obj);

        writingQueue.add(wo -> output.write(token));
        writingQueue.add(wo -> {
            if (obj.itemsCount > 0) {
                writeNewLine();
            }
        });
    }

    @Override
    public void write(CloseParentheses<?> token) {
        var st = state.poll();

        int level = state.size();

        writingQueue.add(wo -> {
            if (st instanceof ObjState obj && obj.itemsCount > 0) {
                writeNewLine();
                writeIndent(level);
            }
        });
        writingQueue.add(wo -> output.write(token));

        flush();
    }

    @Override
    public void write(OpenSquare<?> token) {
        addNestedItem(ItemType.JsArr);

        var arr = new ArrState();
        state.push(arr);

        writingQueue.add(wo -> output.write(token));
        writingQueue.add(wo -> {
            if (arr.itemsCount > 0) writeNewLine();
        });
    }

    @Override
    public void write(CloseSquare<?> token) {
        var st = state.poll();

        int level = state.size();
        writingQueue.add(wo -> {
            if (st instanceof ArrState arr && arr.itemsCount > 0) {
                writeNewLine();
                writeIndent(level);
            }
        });
        writingQueue.add(wo -> output.write(token));

        flush();
    }

    private int newLineEach = 1;

    private void addNestedItem(ItemType itemType) {
        var st = state.poll();
        if (st == null) return;

        state.push(st);
        st.addItem(itemType);
        var cnt = st.itemsCount;

        int level = state.size();

        if (
            (st instanceof ObjState && (cnt % 2 == 1)) ||
                (st instanceof ArrState)
        ) {
            writingQueue.add(wo -> {
                if (newLineEach > 1) {
                    if (st.compoundCount() > 0
                        || st.nestedWriteCount % newLineEach == 0
                        || st instanceof ObjState
                    ) {
                        if( st.nestedWriteCount>0 ){
                            writeNewLine();
                        }
                        writeIndent(level);
                    }
                    st.nestedWriteCount++;
                } else {
                    writeIndent(level);
                }
            });
        }
    }

    @Override
    public void write(BigIntToken<?> token) {
        addNestedItem(ItemType.BigInt);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(LongToken<?> token) {
        addNestedItem(ItemType.Long);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(IntToken<?> token) {
        addNestedItem(ItemType.Int);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(DoubleToken<?> token) {
        addNestedItem(ItemType.Double);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(StringToken<?> token) {
        addNestedItem(ItemType.String);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(FalseToken<?> token) {
        addNestedItem(ItemType.Bool);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(TrueToken<?> token) {
        addNestedItem(ItemType.Bool);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(NullToken<?> token) {
        addNestedItem(ItemType.Null);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(IdentifierToken<?> token) {
        addNestedItem(ItemType.Ident);
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(Colon<?> token) {
        writingQueue.add(wo -> output.write(token));
        writingQueue.add(wo -> writeWhiteSpace(1));
    }

    @Override
    public void write(Comma<?> token) {
        writingQueue.add(wo -> output.write(token));

        var st = state.peek();
        writingQueue.add(wo -> {
            if (newLineEach > 1) {
                if (st != null && (
                    st.compoundCount() > 0
                        || st instanceof ObjState
                )) {
                    writeNewLine();
                }
            } else {
                writeNewLine();
            }
        });
    }

    @Override
    public void write(MLComment<?> token) {
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(SLComment<?> token) {
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }

    @Override
    public void write(Whitespace<?> token) {
        writingQueue.add(wo -> output.write(token));
        flushIfTop();
    }
}
