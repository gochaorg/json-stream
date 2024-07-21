package xyz.cofe.json.stream.token;

/**
 * запятая
 */
public record Comma<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
    private static final StringPointer nullStringPointer = new StringPointer("", 0);
    public static final Comma<StringPointer> instance = new Comma<>(nullStringPointer, nullStringPointer);
}
