package xyz.cofe.grammar.ll.parser;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.grammar.ll.Pointer;
import xyz.cofe.grammar.ll.lexer.Lexer;
import xyz.cofe.grammar.ll.lexer.Matched;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Парсер Ast дерева
 */
public class AstParser {
    public final ImList<ValidatedParser> parsers;
    public final Lexer lexer;

    public AstParser(ImList<ValidatedParser> parsers, Lexer lexer) {
        if (parsers == null) throw new IllegalArgumentException("parsers==null");
        if (lexer == null) throw new IllegalArgumentException("lexer==null");

        this.lexer = lexer;

        var lst = parsers.toList();
        lst.sort(Comparator.comparingInt(a -> a.resultMapper().rule().order()));
        this.parsers = ImList.of(lst);
    }

    private Map<Type, ImList<ValidatedParser>> parsersByResult;

    private Map<Type, ImList<ValidatedParser>> parsersByResult() {
        if (parsersByResult != null) return parsersByResult;
        synchronized (this) {
            if (parsersByResult != null) return parsersByResult;
            Map<Type, ImList<ValidatedParser>> m = new HashMap<>();

            for (var p : parsers) {
                m.put(
                    p.returnType(),
                    m.getOrDefault(p.returnType(), ImList.of()).append(p)
                );
            }

            m.replaceAll((k, ps) -> {
                var psLst = ps.toList();
                psLst.sort(
                    Comparator.comparingInt(a -> a.resultMapper().rule().order()));
                return ImList.of(psLst);
            });

            parsersByResult = m;
            return parsersByResult;
        }
    }

    public ImList<ValidatedParser> parsersOf(Type type) {
        return parsersByResult().getOrDefault(type, ImList.of());
    }

    public record Parsed<R, T>(R value, Pointer<T> next) {}

    private int level = 0;

    public <R, T> Optional<Parsed<R, T>> parse(Class<R> klass, Pointer<T> pointer) {
        if (klass == null) throw new IllegalArgumentException("klass==null");
        if (pointer == null) throw new IllegalArgumentException("pointer==null");
        if (pointer.eof()) return Optional.empty();

        int storedLevel = level;
        try {
            level++;

            var parsers = parsersOf(klass);
            if( parsers.size()==0 ){
                var tokenParsers = lexer.parserOfToken(klass);
                if( tokenParsers.size()>0 ){
                    Optional<T> tokenMatch = pointer.get();
                    if( tokenMatch.isPresent() ){
                        Object v = tokenMatch.get();
                        v = v instanceof Matched<?> m ? m.result() : v;
                        if( klass.isAssignableFrom(v.getClass()) ) {
                            //noinspection unchecked
                            return Optional.of(
                                new Parsed<>((R) v, pointer.move(1))
                            );
                        }
                    }
                }
            }

            if (parsers.size() == 0) return Optional.empty();

            Optional<Parsed<R, T>> result = Optional.empty();
            for (var parser : parsers) {
                result = parse(parser, pointer);
                if (result.isPresent()) break;
            }
            return result;
        } finally {
            level = storedLevel;
        }
    }

    private <R, T> Optional<Parsed<R, T>> parse(ValidatedParser parser, Pointer<T> pointer) {
        int storedLevel = level;

        try {
            level++;

            var values = new ArrayList<>();
            for (var param : parser.inputPattern()) {
                switch (param) {
                    case Param.TermRef term -> {
                        var opt = parse(term, pointer);
                        if (opt.isEmpty()) return Optional.empty();

                        values.add(opt.get().value());
                        pointer = opt.get().next();
                    }
                    case Param.RuleRef rule -> {
                        var opt = parse(rule, pointer);
                        if (opt.isEmpty()) return Optional.empty();

                        values.add(opt.get().value());
                        pointer = opt.get().next();
                    }
                }
            }

            try {
                var result = parser.resultMapper().method().invoke(null, values.toArray());

                //noinspection unchecked
                return Optional.of(
                    new Parsed<>(
                        (R) result,
                        pointer
                    ));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } finally {
            level = storedLevel;
        }
    }

    private <R, T> Optional<Parsed<R, T>> parse(Param.TermRef term, Pointer<T> pointer) {
        var tokOpt = pointer.get();
        if (tokOpt.isEmpty()) return Optional.empty();

        Object tok = tokOpt.get();
        if (tok instanceof Matched<?>) {
            tok = ((Matched<?>) tok).result();
        }

        if (!term.node().isAssignableFrom(tok.getClass())) return Optional.empty();

        return Optional.of(new Parsed<>(
            (R) tok,
            pointer.move(1))
        );
    }

    private <R, T> Optional<Parsed<R, T>> parse(Param.RuleRef rule, Pointer<T> pointer) {
        var values = new ArrayList<>();
        StaticMethodParser staticMethodParser = null;
        int expectParams = 0;
        var ptr = pointer;

        for (var parser : rule.parsers()) {
            var params = parser.parametersType();
            expectParams = params.length;
            staticMethodParser = parser;
            ptr = pointer;
            values.clear();

            for (var param : params) {
                if (param instanceof Class<?> ct) {
                    var parsedOpt = parse(ct, ptr);
                    if (parsedOpt.isEmpty()) break;

                    var parsed = parsedOpt.get();
                    values.add(parsed.value());

                    ptr = parsed.next();
                } else {
                    throw new RuntimeException("!! bug !!");
                }
            }

            if (expectParams == values.size()) break;
        }

        if (staticMethodParser != null && expectParams == values.size()) {
            try {
                var result = staticMethodParser.method().invoke(null, values.toArray());
                return Optional.of(
                    new Parsed<>(
                        (R) result,
                        ptr
                    )
                );
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.empty();
    }
}
