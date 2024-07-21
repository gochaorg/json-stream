package xyz.cofe.json.stream.token;

/**
 * Указатель на символы строки
 * @param <SELF> "Собственный" тип
 */
public sealed interface CharPointer<SELF extends SourcePointer<Character, SELF>> extends SourcePointer<Character,SELF>
    permits StringPointer
{
}
