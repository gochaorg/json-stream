package xyz.cofe.json.stream.ast;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.token.BigIntToken;
import xyz.cofe.json.stream.token.CharPointer;
import xyz.cofe.json.stream.token.CloseParentheses;
import xyz.cofe.json.stream.token.CloseSquare;
import xyz.cofe.json.stream.token.Colon;
import xyz.cofe.json.stream.token.Comma;
import xyz.cofe.json.stream.token.DoubleToken;
import xyz.cofe.json.stream.token.FalseToken;
import xyz.cofe.json.stream.token.IdentifierToken;
import xyz.cofe.json.stream.token.IntToken;
import xyz.cofe.json.stream.token.LongToken;
import xyz.cofe.json.stream.token.MLComment;
import xyz.cofe.json.stream.token.NullToken;
import xyz.cofe.json.stream.token.OpenParentheses;
import xyz.cofe.json.stream.token.OpenSquare;
import xyz.cofe.json.stream.token.SLComment;
import xyz.cofe.json.stream.token.StringToken;
import xyz.cofe.json.stream.token.Token;
import xyz.cofe.json.stream.token.TrueToken;
import xyz.cofe.json.stream.token.Whitespace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/*

                   | init   | obj   | obj.k | obj.v | arr   | arr.c
-------------------|--------|-------|-------|-------|-------|------
BigIntToken        | init   | err   | err   | obj.c | arr.c | err
IntToken           | init   | err   | err   | obj.c | arr.c | err
LongToken          | init   | err   | err   | obj.c | arr.c | err
DoubleToken        | init   | err   | err   | obj.c | arr.c | err
StringToken        | init   | obj.k | err   | obj.c | arr.c | err
FalseToken         | init   | err   | err   | obj.c | arr.c | err
TrueToken          | init   | err   | err   | obj.c | arr.c | err
NullToken          | init   | err   | err   | obj.c | arr.c | err
IdentifierToken    | init   | obj.k | err   | obj.c | arr.c | err
OpenParentheses  { | obj    | err   | err   | obj   | obj   | err
CloseParentheses } | err    | err   | err   | init  | err   | err
OpenSquare       [ | arr    | err   | err   | arr   | arr   | err
CloseSquare      ] | err    | err   | err   | err   | init  | init
Colon            : | err    | err   | obj.v | err   | err   | err
Comma            , | err    | err   | err   | obj   | err   | arr
MLComment          | init   | obj   | obj.k | obj.v | arr   | arr.c
SLComment          | init   | obj   | obj.k | obj.v | arr   | arr.c
Whitespace         | init   | obj   | obj.k | obj.v | arr   | arr.c

 */
public sealed interface AstParser<S extends CharPointer<S>> {
    record Parsed<S extends CharPointer<S>>(AstParser<S> parser, Optional<Ast<S>> result) {}

    Parsed<S> input(Token<S> token);

    private static <S extends CharPointer<S>> Parsed<S> ok(AstParser<S> parser, Ast<S> result) {
        return new Parsed<>(parser, Optional.of(result));
    }
    private static <S extends CharPointer<S>> Parsed<S> ok(AstParser<S> parser) {
        return new Parsed<>(parser, Optional.empty());
    }
    private static <S extends CharPointer<S>> Parsed<S> err(String message) {
        return new Parsed<>(new Error<S>(message), Optional.empty());
    }

    public record Init<S extends CharPointer<S>>() implements AstParser<S> {
        @Override
        public Parsed<S> input(Token<S> token) {
            if( token==null ) throw new IllegalArgumentException("token==null");

            switch (token){
                case BigIntToken<S> n -> {
                    return ok(this,new Ast.NumberAst.BigIntAst<>(n));
                }
                case LongToken<S> n -> {
                    return ok(this,new Ast.NumberAst.LongAst<>(n));
                }
                case IntToken<S> n -> {
                    return ok(this,new Ast.NumberAst.IntAst<>(n));
                }
                case DoubleToken<S> n -> {
                    return ok(this,new Ast.NumberAst.DoubleAst<>(n));
                }
                case IdentifierToken<S> n -> {
                    return ok(this,new Ast.IdentAst<>(n));
                }
                case StringToken<S> n -> {
                    return ok(this,new Ast.StringAst<>(n));
                }
                case FalseToken<S> n -> {
                    return ok(this,new Ast.BooleanAst.FalseAst<>(n));
                }
                case TrueToken<S> n -> {
                    return ok(this,new Ast.BooleanAst.TrueAst<>(n));
                }
                case NullToken<S> n -> {
                    return ok(this,new Ast.NullAst<>(n));
                }
                case Whitespace<S> w -> {
                    return ok(this);
                }
                case SLComment<S> s -> {
                    return ok(this);
                }
                case MLComment<S> m -> {
                    return ok(this);
                }
                case Comma<S> t -> { return err("token "+token+" is not valid start"); }
                case Colon<S> t -> { return err("token "+token+" is not valid start"); }
                case CloseParentheses<S> t -> { return err("token "+token+" is not valid start"); }
                case CloseSquare<S> t -> { return err("token "+token+" is not valid start"); }
                case OpenSquare<S> t -> { return ok(new ArrayStart<>(this,t)); }
                case OpenParentheses<S> t -> { return ok(new ObjectStart<>(this,t)); }
            }
        }
    }
    public record Error<S extends CharPointer<S>>(String message) implements AstParser<S> {
        @Override
        public Parsed<S> input(Token<S> token) {
            return ok(this);
        }
    }

    public static final class ObjectStart<S extends CharPointer<S>> implements AstParser<S> {
        private final AstParser<S> parent;
        private final OpenParentheses<S> begin;
        private final Consumer<Ast<S>> resultConsumer;

        private enum State {
            ExpectKey,
            AfterKey,
            ExpectValue,
            AfterValue
        }

        private final List<Ast.KeyValue<S>> values = new ArrayList<>();
        private Ast.Key key;

        private State state = State.ExpectKey;

        public ObjectStart(AstParser<S> parent, OpenParentheses<S> begin) {
            this.parent = parent;
            this.begin = begin;
            resultConsumer = ignore -> {};
        }

        public ObjectStart(AstParser<S> parent, OpenParentheses<S> begin, Consumer<Ast<S>> resultConsumer) {
            this.parent = parent;
            this.begin = begin;
            this.resultConsumer = resultConsumer;
        }

        @Override
        public Parsed<S> input(Token<S> token) {
            if( token==null ) throw new IllegalArgumentException("token==null");
            switch (state){
                case ExpectKey -> {
                    switch (token) {
                        case StringToken<S> t -> {
                            key = new Ast.StringAst<S>(t);
                            state = State.AfterKey;
                            return ok(this);
                        }
                        case IdentifierToken<S> t -> {
                            key = new Ast.IdentAst<S>(t);
                            state = State.AfterKey;
                            return ok(this);
                        }
                        case MLComment<S> t -> {
                            return ok(this);
                        }
                        case SLComment<S> t -> {
                            return ok(this);
                        }
                        case Whitespace<S> t -> {
                            return ok(this);
                        }
                        default -> {
                            return err("!");
                        }
                    }
                }
                case AfterKey -> {
                    switch (token) {
                        case Colon<S> t -> {
                            state = State.ExpectValue;
                            return ok(this);
                        }
                        case MLComment<S> t -> {
                            return ok(this);
                        }
                        case SLComment<S> t -> {
                            return ok(this);
                        }
                        case Whitespace<S> t -> {
                            return ok(this);
                        }
                        default -> {
                            return err("!");
                        }
                    }
                }
                case ExpectValue -> {
                    switch (token) {
                        case BigIntToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.NumberAst.BigIntAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case LongToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.NumberAst.LongAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case IntToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.NumberAst.IntAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case DoubleToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.NumberAst.DoubleAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case StringToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.StringAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case NullToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.NullAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case TrueToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.BooleanAst.TrueAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case FalseToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.BooleanAst.FalseAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case IdentifierToken<S> t -> {
                            values.add(new Ast.KeyValue<>(key, new Ast.IdentAst<>(t)));
                            state = State.AfterValue;
                            return ok(this);
                        }
                        case OpenParentheses<S> t -> {
                            var kk = key;
                            var obj = new ObjectStart<>(this, t, res -> {
                                values.add(new Ast.KeyValue<>(kk, res));
                            });
                            state = State.AfterValue;
                            return ok(obj);
                        }
                        case OpenSquare<S> t -> {
                            var kk = key;
                            var arr = new ArrayStart<>(this, t, res -> {
                                values.add(new Ast.KeyValue<>(kk,res));
                            });
                            state = State.AfterValue;
                            return ok(arr);
                        }
                        case MLComment<S> t -> {
                            return ok(this);
                        }
                        case SLComment<S> t -> {
                            return ok(this);
                        }
                        case Whitespace<S> t -> {
                            return ok(this);
                        }
                        default -> {
                            return err("!");
                        }
                    }
                }
                case AfterValue -> {
                    switch (token){
                        case MLComment<S> t -> {
                            return ok(this);
                        }
                        case SLComment<S> t -> {
                            return ok(this);
                        }
                        case Whitespace<S> t -> {
                            return ok(this);
                        }
                        case CloseParentheses<S> t -> {
                            var res = new Ast.ObjectAst<>(
                                ImList.of(values),
                                begin,
                                t
                            );
                            resultConsumer.accept(res);
                            return ok(parent, res);
                        }
                        case Comma<S> t -> {
                            state = State.ExpectKey;
                            return ok(this);
                        }
                        default -> {
                            return err("!");
                        }
                    }
                }
            }
            return ok(this);
        }
    }
    public static final class ArrayStart<S extends CharPointer<S>>  implements AstParser<S> {
        private final AstParser<S> parent;
        private final OpenSquare<S> begin;
        private final Consumer<Ast<S>> resultConsumer;

        private final List<Ast<S>> values = new ArrayList<>();

        private enum State { ExpectValue, AfterValue }
        private State state = State.ExpectValue;

        public ArrayStart( AstParser<S> parent, OpenSquare<S> begin ) {
            this.begin = begin;
            this.parent = parent;
            this.resultConsumer = ignore -> {};
        }

        public ArrayStart( AstParser<S> parent, OpenSquare<S> begin, Consumer<Ast<S>> resultConsumer ) {
            this.begin = begin;
            this.parent = parent;
            this.resultConsumer = resultConsumer;
        }

        @Override
        public Parsed<S> input(Token<S> token) {
            if( token==null ) throw new IllegalArgumentException("token==null");

            switch (state) {
                case ExpectValue -> {
                    switch (token) {
                        case MLComment<S> t -> {return ok(this);}
                        case SLComment<S> t -> {return ok(this);}
                        case Whitespace<S> t -> {return ok(this);}
                        case BigIntToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.NumberAst.BigIntAst<>(t));
                            return ok(this);
                        }
                        case LongToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.NumberAst.LongAst<>(t));
                            return ok(this);
                        }
                        case IntToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.NumberAst.IntAst<>(t));
                            return ok(this);
                        }
                        case DoubleToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.NumberAst.DoubleAst<>(t));
                            return ok(this);
                        }
                        case NullToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.NullAst<>(t));
                            return ok(this);
                        }
                        case FalseToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.BooleanAst.FalseAst<>(t));
                            return ok(this);
                        }
                        case TrueToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.BooleanAst.TrueAst<>(t));
                            return ok(this);
                        }
                        case StringToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.StringAst<>(t));
                            return ok(this);
                        }
                        case IdentifierToken<S> t -> {
                            state = State.AfterValue;
                            values.add(new Ast.IdentAst<>(t));
                            return ok(this);
                        }
                        case OpenSquare<S> t -> {
                            state = State.AfterValue;
                            return ok(new ArrayStart<S>(this, t, values::add));
                        }
                        case OpenParentheses<S> t -> {
                            state = State.AfterValue;
                            return ok(new ObjectStart<S>(this, t, values::add));
                        }
                        case CloseSquare<S> t -> {
                            var arr = new Ast.ArrayAst<>(
                                ImList.of(values),
                                begin,
                                t
                            );

                            resultConsumer.accept(arr);
                            state = State.AfterValue;
                            return AstParser.ok(parent,arr);
                        }
                        case CloseParentheses<S> t -> { return err("!!"); }
                        case Colon<S> t -> { return err("!!"); }
                        case Comma<S> t -> { return err("!!"); }
                    }
                }
                case AfterValue -> {
                    switch (token) {
                        case CloseSquare<S> t -> {
                            var arr = new Ast.ArrayAst<>(
                                ImList.of(values),
                                begin,
                                t
                            );

                            resultConsumer.accept(arr);
                            state = State.AfterValue;
                            return AstParser.ok(parent,arr);
                        }
                        case Comma<S> t -> {
                            state = State.ExpectValue;
                            return ok(this);
                        }
                        case MLComment<S> t -> {return ok(this);}
                        case SLComment<S> t -> {return ok(this);}
                        case Whitespace<S> t -> {return ok(this);}
                        default -> { return err("!!"); }
                    }
                }
            };

            return err("!!");
        }
    }
}
