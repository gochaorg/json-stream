package xyz.cofe.json.stream.token;

import xyz.cofe.json.stream.ast.Ast;

import java.math.BigInteger;

public record BigIntToken<S extends CharPointer<S>>(
    BigInteger value,
    S begin,
    S end
) implements Token<S> {
}
