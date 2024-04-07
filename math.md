Syntax
======================

**expr** ::= assign | sum

**assign** ::= id `=` expr

**sum**  ::= mul { sumOp mul }
       | mul

**sumOp** ::= `+` | `-`

**mul** ::= atom { mulOp atom }
       | atom

**mulOp** ::= `*` | `/`

**atom** ::= num | id | `(` exp `)`

**num** ::= digit { digit }

**digit** ::= `0` .. `9`

**id** ::= letter { letter | digit }