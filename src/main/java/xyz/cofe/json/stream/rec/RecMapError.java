package xyz.cofe.json.stream.rec;

public class RecMapError extends Error {
    public RecMapError(String message) {
        super(message);
    }

    public RecMapError(String message, Throwable cause) {
        super(message, cause);
    }
}
