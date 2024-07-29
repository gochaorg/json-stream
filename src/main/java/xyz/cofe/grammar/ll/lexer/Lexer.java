package xyz.cofe.grammar.ll.lexer;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Tuple2;
import xyz.cofe.grammar.ll.bind.TermBind;
import xyz.cofe.grammar.ll.bind.Terms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Лексический анализатор
 *
 * <p></p> При построении лексического анализатора {@link #build(Class)} производится
 *
 * <ul>
 *     <li>
 *         Поиск парсеров в указанном классе, вложенных классах и классах указанных в {@link Terms}
 *     </li>
 *     <li>
 *         Каждый парсер - это статический метода вида:
 * <pre>
 * public static Optional&lt;Matched&lt;<i>имя класса лексемы</i>&gt;&gt; parse(String source, int begin)
 * </pre>
 *         Имя метода не имеет значения
 *     </li>
 *     <li>
 *         Для простых лексем можно использовать enum.
 *         <br>
 *         Пример:
 *         <pre>
 * enum SomeTokens {
 *     &#x40;TermBind("==")
 *     EQUALS,
 *
 *     &#x40;TermBind("&gt;")
 *     MORE
 * }
 *         </pre>
 *     </li>
 * </ul>
 */
public class Lexer {
    private Lexer(ImList<TokenParser> tokenParsers) {
        if (tokenParsers == null) throw new IllegalArgumentException("tokenParsers==null");
        this.tokenParsers = tokenParsers;
    }

    private final ImList<TokenParser> tokenParsers;
    public ImList<TokenParser> getTokenParsers() {return tokenParsers;}

    private Map<Type, ImList<TokenParser>> tokenParsersByType;
    private Map<Type, ImList<TokenParser>> tokenParsersByType(){
        if( tokenParsersByType!=null )return tokenParsersByType;
        synchronized (this) {
            if (tokenParsersByType != null) return tokenParsersByType;
            Map<Type, ImList<TokenParser>> m = new HashMap<>();

            for( var tp: tokenParsers ){
                m.put(
                    tp.tokenType(),
                    m.getOrDefault(tp.tokenType(), ImList.of()).prepend( tp )
                );
            }

            Map<Type, ImList<TokenParser>> m2 = new HashMap<>();
            m.forEach( (k,v) -> {
                m2.put(k, v.reverse());
            });

            tokenParsersByType = m2;
            return tokenParsersByType;
        }
    }

    private ImList<Type> tokenTypes;
    public ImList<Type> tokenTypes(){
        if( tokenTypes!=null )return tokenTypes;
        synchronized (this) {
            if (tokenTypes != null) return tokenTypes;
            tokenTypes = ImList.from(tokenParsersByType().keySet());
            return tokenTypes;
        }
    }

    private ImList<Type> visibleTokenTypes;

    @SuppressWarnings("Convert2MethodRef")
    public ImList<Type> visibleTokenTypes(){
        if( visibleTokenTypes!=null )return visibleTokenTypes;
        synchronized (this) {
            if (visibleTokenTypes != null) return visibleTokenTypes;
            visibleTokenTypes =
                ImList.from(tokenParsersByType().entrySet())
                    .map( e ->
                        Tuple2.of(
                            e.getKey(),
                            e.getValue()
                                .map(v -> v.hasSkip())
                                .foldLeft(0, (acc,it) -> acc + (it ? 1 : 0))
                        )
                    )
                    .filter( e -> e._2()>0 )
                    .map( e -> e._1() )
                ;
            return visibleTokenTypes;
        }
    }

    public ImList<TokenParser> parserOfToken(Type type){
        if( type==null ) throw new IllegalArgumentException("type==null");
        return tokenParsersByType().getOrDefault(type, ImList.of());
    }

    public ImList<TokenParser> parserOfToken(Type type, ImList<TermBind> binds){
        if( type==null ) throw new IllegalArgumentException("type==null");
        if( binds==null ) throw new IllegalArgumentException("binds==null");

        var parsers = tokenParsersByType().getOrDefault(type, ImList.of());
        if( binds.size()==0 )return parsers;

        return parsers.filter( tp -> {
            for( var b1 : tp.binds() ){
                for( var b2 : binds ){
                    if( b1.value().equals(b2.value()) ){
                        return true;
                    }
                }
            }

            return false;
        });
    }

    /**
     * Создание лексера
     * @param grammarRoot класс содержащий парсеры лексем
     * @return Лексический анализатор
     */
    public static Lexer build(Class<?> grammarRoot) {
        if (grammarRoot == null) throw new IllegalArgumentException("grammarRoot==null");

        List<Class<?>> workSet = new ArrayList<>();
        workSet.add(grammarRoot);

        Arrays.stream(grammarRoot.getAnnotations()).forEach( a -> {
            if( a instanceof Terms terms ){
                for(var term : terms.value()){
                    if( !workSet.contains(term) ){
                        workSet.add(term);
                    }
                }
            }
        });

        List<TokenParser> tokenParsers = new ArrayList<>();
        Set<Class<?>> visited = new HashSet<>();

        List<TokenParser> foundExplicitParsers = new ArrayList<>();

        while (!workSet.isEmpty()) {
            var cls = workSet.remove(0);
            if (visited.contains(cls)) continue;
            visited.add(cls);

            var follow = new ArrayList<>(Arrays.asList(cls.getNestMembers()));
            follow.removeAll(visited);
            workSet.addAll(follow);

            foundExplicitParsers.clear();
            for (var mth : cls.getMethods()) {
                var tokenParserOpt = TokenParserStaticMethod.parse(mth);

                if( tokenParserOpt.isPresent() ) {
                    var tokenParser = tokenParserOpt.get();
                    tokenParsers.add(tokenParser);
                    foundExplicitParsers.add(tokenParser);
                }
            }

            if( foundExplicitParsers.isEmpty() && cls.isEnum() ){
                Object[] consts = cls.getEnumConstants();
                try {
                    Method nameMth = cls.getMethod("name");
                    for( var constValue : consts ) {
                        try {
                            var nameObj = nameMth.invoke(constValue);
                            var nameStr = nameObj.toString();

                            var fld = cls.getField(nameStr);
                            var annotations = fld.getAnnotations();

                            var termBinds = new ArrayList<TermBind>();
                            for( var ann : annotations ){
                                if( ann instanceof TermBind t ){
                                    termBinds.add(t);
                                }
                            }

                            if( !termBinds.isEmpty() ){
                                var tokenParser = new TokenParserEnumValue(termBinds.toArray(new TermBind[0]), cls, constValue);
                                tokenParsers.add(tokenParser);
                            }
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        Terms terms = grammarRoot.getAnnotation(Terms.class);
        Map<Class<?>, Integer> termIndexes = new HashMap<>();
        var termIndex = -1;
        for (var t : terms.value()) {
            termIndex++;
            termIndexes.put(t, termIndex);
        }

        tokenParsers.sort((ta, tb) -> {
            //noinspection SuspiciousMethodCalls
            Integer ia = termIndexes.getOrDefault(ta.tokenType(), Integer.MAX_VALUE);
            //noinspection SuspiciousMethodCalls
            Integer ib = termIndexes.getOrDefault(tb.tokenType(), Integer.MAX_VALUE);
            return ia.compareTo(ib);
        });

        return new Lexer(ImList.from(tokenParsers));
    }

    public ImList<Matched<?>> parse(String source, int offset) {
        if (source == null) throw new IllegalArgumentException("source==null");
        if (offset < 0) throw new IllegalArgumentException("offset<0");
        if (offset >= source.length()) throw new IllegalArgumentException("offset>=source.length()");

        int off = offset;
        ImList<Matched<?>> tokens = ImList.of();

        while (off < source.length()) {
            boolean parsed = false;
            for (var parser : getTokenParsers()) {
                var resOpt = parser.parse(source, off);
                if (resOpt.isPresent()) {
                    var res = resOpt.get();
                    if (res.end() <= off) {
                        throw new ParserImplError(parser, source, offset, res);
                    }
                    if (!parser.hasSkip()) {
                        tokens = tokens.prepend(res);
                    }
                    off = res.end();
                    parsed = true;
                    break;
                }
            }
            if (!parsed) {
                throw new UnrecognizedTokenError(tokens.reverse(), source, off);
            }
        }

        return tokens.reverse();
    }
}
