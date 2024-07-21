package xyz.cofe.json.stream.token;

import xyz.cofe.json.stream.ast.Ast;

import java.math.BigInteger;

/**
 * Число, большое
 * @param value число
 * @param begin токен начала
 * @param end токен конца
 * @param <S> Тип источника данных
 */
public record BigIntToken<S extends CharPointer<S>>(
    BigInteger value,
    S begin,
    S end
) implements Token<S> {
}
