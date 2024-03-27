package xyz.cofe.json.stream.parser.grammar.lexer;

import xyz.cofe.json.stream.parser.grammar.bind.TermBind;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public record TokenParserStaticMethod(
    TermBind[] binds,
    Type tokenType,
    Method method
) implements TokenParser {
    public static Optional<TokenParserStaticMethod> parse(Method method){
        if( method==null ) throw new IllegalArgumentException("method==null");

        if (!Modifier.isStatic(method.getModifiers())) return Optional.empty();

        var terms = method.getAnnotationsByType(TermBind.class);
        if (terms.length == 0) return Optional.empty();

        var argsType = method.getParameterTypes();
        if (argsType.length != 2) return Optional.empty();
        if (argsType[0] != String.class) return Optional.empty();
        if (argsType[1] != int.class) return Optional.empty();

        var retType = method.getGenericReturnType();
        if (!(retType instanceof ParameterizedType pt)) return Optional.empty();
        if (!pt.getRawType().getTypeName().equals(Optional.class.getName())) return Optional.empty();

        var retParamType = pt.getActualTypeArguments()[0];
        if (!(retParamType instanceof ParameterizedType pt2)) return Optional.empty();
        if (!pt2.getRawType().getTypeName().equals(Matched.class.getName())) return Optional.empty();

        var tokenClass = pt2.getActualTypeArguments()[0];
        var tokenParser = new TokenParserStaticMethod(terms, tokenClass, method);

        return Optional.of(tokenParser);
    }

    @SuppressWarnings("unchecked")
    public Optional<Matched<?>> parse(String source, int offset) {
        if (source == null) throw new IllegalArgumentException("source==null");
        if (offset < 0) throw new IllegalArgumentException("offset<0");
        if (offset >= source.length()) throw new IllegalArgumentException("offset>=source.length()");
        try {
            return (Optional<Matched<?>>) method.invoke(null, source, offset);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasSkip() {
        boolean skip = false;
        for (var b : binds) {
            skip = skip || b.skip();
        }
        return skip;
    }
}
