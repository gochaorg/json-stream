package xyz.cofe.json.stream.token;

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
    S begin();

    S end();
}
