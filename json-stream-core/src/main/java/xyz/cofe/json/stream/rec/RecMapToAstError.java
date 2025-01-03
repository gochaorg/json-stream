package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.ImList;

public class RecMapToAstError extends RecMapError {
    private final ImList<RecMapper.ParseStack> stack;

    public ImList<RecMapper.ParseStack> getParseStack(){
        return stack;
    }

    public RecMapToAstError(String message) {
        super(message);
        stack = ImList.of();
    }

    public RecMapToAstError(String message, Throwable cause) {
        super(message, cause);
        stack = ImList.of();
    }

    public RecMapToAstError(Throwable cause) {
        super(cause);
        stack = ImList.of();
    }

    public RecMapToAstError(String message, ImList<RecMapper.ParseStack> stack) {
        super(message);
        this.stack = stack==null ? ImList.of() : stack;
    }

    public RecMapToAstError(String message, Throwable cause, ImList<RecMapper.ParseStack> stack) {
        super(message, cause);
        this.stack = stack==null ? ImList.of() : stack;
    }

    public RecMapToAstError(Throwable cause, ImList<RecMapper.ParseStack> stack) {
        super(cause);
        this.stack = stack==null ? ImList.of() : stack;
    }
}
