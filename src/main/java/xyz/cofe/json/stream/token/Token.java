package xyz.cofe.json.stream.token;

/**
 * Лексемы json
 * @param <S> тип исходника
 */
public sealed interface Token<S extends CharPointer<S>>
    permits BigIntToken,
            IntToken,
            LongToken,
            DoubleToken,      // Число плавающее
            StringToken,      // Строка
            FalseToken,       // false
            TrueToken,        // true
            NullToken,        // null
            IdentifierToken,  // идентификатор
            OpenParentheses,  // {
            CloseParentheses, // }
            OpenSquare,       // [
            CloseSquare,      // ]
            Colon,            // :
            Comma,            // ,
            MLComment,        // /* */
            SLComment,        // //
            Whitespace        // Пробельный символ
{
    /**
     * Возвращает указатель на начало в исходнике
     * @return начало
     */
    S begin();

    /**
     * Возвращает указатель на конец в исходнике
     * @return конец
     */
    S end();
}
