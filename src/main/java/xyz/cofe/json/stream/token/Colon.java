package xyz.cofe.json.stream.token;

/**
 * двоеточие
 *
 * @param begin начало лексемы
 * @param end   конец лексемы
 * @param <S>   Указатель на символы строки
 */
public record Colon<S extends CharPointer<S>>(
    S begin,
    S end
) implements Token<S> {
    private static final StringPointer nullStringPointer = new StringPointer("", 0);

    /**
     * Экземпляр лексемы
     */
    public static final Colon<StringPointer> instance = new Colon<>(nullStringPointer, nullStringPointer);
}
