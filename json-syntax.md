Json syntax
===================

Лексическая часть
===================

Лексемы

- Строка
- Число
- Комментарии
- Пробельные символы
- Идентификатор
- Предопределенные слова и спец символы

Идентификатор
-----------------

    id ::= first_letter { second_letter }
    first_letter ::= letter 
                   | '_'
                   | '$'
    second_letter ::= letter
                    | digit
                    | '_'
                    | '$'

Парсинг строки
-----------------
https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Grammar_and_types

    string ::= singe_quoted_string | double_quoted_string
    singe_quoted_string  ::= '\'' { encoded_char } '\''
    double_quoted_string ::= '"' { encoded_char } '"'
    encoded_char ::= escaped_seq | simple_char
    escaped_seq ::= escape_hex | escape_unicode_ext | escape_unicode | escape_oct | escape_simple
    escape_oct ::= '\' oct_char oct_char oct_char
    escape_simple ::= '\' ( '0' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | '\'' | '"' | '\' )
    escape_hex ::= '\x' hex_char hex_char
    escape_unicode ::= '\u' hex_char hex_char hex_char hex_char
    escape_unicode_ext ::= '\u{' hex_char hex_char hex_char hex_char hex_char '}'

Распознавание чисел
------------------------
https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Grammar_and_types

### float point

`[digits].[digits][(E|e)[(+|-)]digits]`

```
3.1415926
.123456789
3.1E+12
.1e-23
```

### integer

```
0, 117, 123456789123456789n             (decimal, base 10)
015, 0001, 0o777777777777n              (octal, base 8)
0x1123, 0x00111, 0x123456789ABCDEFn     (hexadecimal, "hex" or base 16)
0b11, 0b0011, 0b11101001010101010101n   (binary, base 2)
```

### grammar


    number      ::= [ unary_minus ] integer | float

    integer     ::= octal_int | hex_int | bin_int | dec_int
    octal_int   ::= '0' [ 'o' | 'O' ] { octal_digit } [ 'n' ]
      hex_int   ::= '0' ( 'x' | 'X' ) { hex_digit } [ 'n' ]
      bin_int   ::= '0' ( 'b' | 'B' ) { bin_digit } [ 'n' ]
      dec_int   ::= dec_digit { dec_digit } [ 'n' ]

      bin_digit ::= '0' | '1'
    octal_digit ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
      dec_digit ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
      hex_digit ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' 
                  | 'a' | 'b' | 'c' | 'd' | 'e' | 'f'
                  | 'A' | 'B' | 'C' | 'D' | 'E' | 'F'

    float         ::= dec_part '.' fraction_part ( 'e' | 'E' ) [ '-' | '+' ] exponent_part
                    | dec_part '.' fraction_part 
                    | dec_part '.' ( 'e' | 'E' )  [ '-' | '+' ] exponent_part 
                    | dec_part '.'
                    | '.' fraction_part ( 'e' | 'E' )  [ '-' | '+' ] exponent_part
                    | '.' fraction_part 
    
    dec_part      ::= dec_digit { dec_digit }
    fraction_part ::= dec_digit { dec_digit }
    exponent_part ::= dec_digit { dec_digit }

Комментарий js
------------------

    comment             ::= single-line-comment | multi-line-comment
    single-line-comment ::= '/' '/' {any} ( end-of-line | end-of-input )
    end-of-line         ::= '\r' '\n' | '\n'
    multi-line-comment  ::= '/' '*' {any} ( '*' '/' | end-of-input )

Предопределенные слова и спец символы
------------------------------------------

    keyword ::= 'true'
              | 'false'
              | 'null'
              | '['
              | ']'
              | '{'
              | '}'
              | ':'
              | ','

Синтаксическая часть
===============================

- Примитивы
  - Строка
  - Идентификатор
  - null
  - false / true
  - Число
- Объекты
- Массивы

Синтаксис
---------------------------

    ast ::= primitive
          | object
          | array

    primitive ::= string
                | int
                | float
                | `true`
                | `false`
                | `null`

    object ::= `{` [ keyValue { `,` keyValue } ] `}`

    keyValue ::= key `:` ast

    key ::= string
          | id

    array ::= `[` [ ast { `,` ast } ] `]`