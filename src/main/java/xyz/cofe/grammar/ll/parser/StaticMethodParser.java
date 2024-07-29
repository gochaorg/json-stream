package xyz.cofe.grammar.ll.parser;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.grammar.ll.bind.Rule;
import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.lexer.Lexer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Парсер сопоставление распознанной последовательности согласно правилу
 * @param rule правило
 * @param method
 * @param ruleClass
 * @param inputRawPattern
 */
public record StaticMethodParser(
    Rule rule,
    Method method,
    Class<?> ruleClass,
    Type[] inputRawPattern
) {
    public ImList<TermBind> termBindOfParameter(int paramIndex){
        if( paramIndex<0 )return ImList.of();
        if( paramIndex>= inputRawPattern.length )return ImList.of();

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

//        retType =
//            ruleAnn.name() == Rule.class
//            || ruleAnn.name() == Object.class
//            ? retType
//                : ruleAnn.name();

        var parameters = method.getGenericParameterTypes();
        if (parameters.length == 0) return Optional.empty();

        return Optional.of(new StaticMethodParser(ruleAnn, method, retType, parameters));
    }

    /**
     * Валидация функций сопоставления парсеров
     * @param astParsers парсеры
     * @param lexer лексер
     * @return результат
     */
    public ImList<Result<? extends Param, String>> inputPattern(SomeParsers astParsers, Lexer lexer) {
        if (astParsers == null) throw new IllegalArgumentException("astParsers==null");
        if (lexer == null) throw new IllegalArgumentException("lexer==null");

        ImList<Result<? extends Param, String>> params = ImList.of();
        var paramIndex = -1;
        var paramsAnns = method.getParameterAnnotations();

        for (var paramType : inputRawPattern) {
            paramIndex++;
            ImList<TermBind> termBinds = ImList.of(Arrays.asList(paramsAnns[paramIndex])).fmap(TermBind.class);

            if( paramType instanceof Class<?> pc ) {
                params = params.prepend( resolveParam(termBinds,pc,astParsers,lexer) );
            } else if( paramType instanceof ParameterizedType pzt && pzt.getRawType() instanceof Class<?> rt ) {
                if( rt==List.class && pzt.getActualTypeArguments().length==1 && pzt.getActualTypeArguments()[0] instanceof Class<?> pc ){
                    params = params.prepend(
                        resolveParam(termBinds,pc,astParsers,lexer).map(Param.Repeat::new)
                    );
                }else{
                    params = params.prepend(Result.error("unsupported param type " + paramType));
                }
            } else {
                params = params.prepend(Result.error("unsupported param type " + paramType));
            }
        }

        return params.reverse();
    }

    private Result<Param.RepeatableParam, String> resolveParam( ImList<TermBind> termBinds, Class<?> pc, SomeParsers astParsers, Lexer lexer ){
        var tokenParsers = lexer.parserOfToken(pc, termBinds);
        if (tokenParsers.size() > 0) {
            var refs = new Param.TermRef(pc, tokenParsers);
            return Result.ok(refs);
        } else {
            var parsers = astParsers.parsersOf(pc);
            if (parsers.size() == 0) {
                return Result.error("unsupported param type " + pc + ", not found lexem or non-term node");
            } else {
                return Result.ok(new Param.RuleRef(pc, ImList.from(parsers)));
            }
        }
    }
}
