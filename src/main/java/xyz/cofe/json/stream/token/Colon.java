package xyz.cofe.json.stream.token;

/**
 * двоеточие
 */
public record Colon<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
    private static final StringPointer nullStringPointer = new StringPointer("", 0);
    public static final Colon<StringPointer> instance = new Colon<>(nullStringPointer, nullStringPointer);
}
