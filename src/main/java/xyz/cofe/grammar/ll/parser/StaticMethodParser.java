package xyz.cofe.grammar.ll.parser;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.grammar.ll.bind.Rule;
import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.lexer.Lexer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

public record StaticMethodParser(
    Rule rule,
    Method method,
    Class<?> returnType,
    Type[] parametersType
) {
    public ImList<TermBind> termBindOfParameter(int paramIndex){
        if( paramIndex<0 )return ImList.of();
        if( paramIndex>=parametersType.length )return ImList.of();

        var paramAnn2d = method.getParameterAnnotations();
        if( paramIndex>=paramAnn2d.length )return ImList.of();

        var paramAnn1d = paramAnn2d[paramIndex];
        return ImList.of(Arrays.asList(paramAnn1d)).fmap(TermBind.class);
    }

    public static Optional<StaticMethodParser> parse(Method method) {
        if (method == null) throw new IllegalArgumentException("method==null");

        if (!Modifier.isStatic(method.getModifiers())) return Optional.empty();

        var ruleAnn = method.getAnnotation(Rule.class);
        if (ruleAnn == null) return Optional.empty();

        Class<?> retType = method.getReturnType();
        if (retType == Void.class) return Optional.empty();

        var parameters = method.getGenericParameterTypes();
        if (parameters.length == 0) return Optional.empty();

        return Optional.of(new StaticMethodParser(ruleAnn, method, retType, parameters));
    }

    public ImList<Result<Param, String>> inputPattern(SomeParsers astParsers, Lexer lexer) {
        if (astParsers == null) throw new IllegalArgumentException("astParsers==null");
        if (lexer == null) throw new IllegalArgumentException("lexer==null");

        ImList<Result<Param, String>> params = ImList.of();
        var paramIndex = -1;
        var paramsAnns = method.getParameterAnnotations();

        for (var pt : parametersType) {
            paramIndex++;
            var termBinds = ImList.of(Arrays.asList(paramsAnns[paramIndex])).fmap(TermBind.class);

            if (pt instanceof Class<?>) {
                Class<?> pc = (Class<?>) pt;
                var tokenParsers = lexer.parserOfToken(pc, termBinds);
                if (tokenParsers.size() > 0) {
                    var refs = new Param.TermRef(pc, tokenParsers);
                    params = params.prepend(Result.ok(refs));
                } else {
                    var parsers = astParsers.parsersOf(pc);
                    if (parsers.size() == 0) {
                        params = params.prepend(Result.error("unsupported param type " + pt + ", not found lexem or non-term node"));
                    } else {
                        params = params.prepend(Result.ok(new Param.RuleRef(pc, ImList.of(parsers))));
                    }
                }
            } else {
                params = params.prepend(Result.error("unsupported param type " + pt));
            }
        }

        return params.reverse();
    }
}
