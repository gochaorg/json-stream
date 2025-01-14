package xyz.cofe.json.stream.token;

import java.util.Optional;

/**
 * Указатель на исходник - строку
 */
public final class StringPointer implements CharPointer<StringPointer> {
    /**
     * Исходник - строка
     */
    public final String source;

    /**
     * Смещение относительно начала строки
     */
    public final int offset;

    /**
     * Конструктор
     * @param source Исходник - строка
     * @param offset Смещение относительно начала строки
     */
    public StringPointer(String source, int offset) {
        this.source = source;
        this.offset = offset;
    }

    @Override
    public Optional<Character> get(int offset) {
        int t = offset + this.offset;
        if (t < 0 || t >= source.length())
            return Optional.empty();
        return Optional.of(source.charAt(t));
    }

    @Override
    public StringPointer move(int offset) {
        return offset == 0 ? this : new StringPointer(source, offset + this.offset);
    }

    @Override
    public int subtract(StringPointer other) {
        if (other == null) throw new IllegalArgumentException("other==null");
        if (!other.source.equals(source))
            throw new IllegalArgumentException("other has different source");

        return offset - other.offset;
    }
}
