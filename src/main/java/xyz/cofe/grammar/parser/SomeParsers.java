package xyz.cofe.grammar.parser;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.grammar.lexer.Lexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SomeParsers {
    public final ImList<StaticMethodParser> parsers;

    SomeParsers(ImList<StaticMethodParser> parsers) {
        this.parsers = parsers;
    }

    public static SomeParsers parse(Class<?> grammar) {
        if (grammar == null) throw new IllegalArgumentException("grammar==null");

        ImList<StaticMethodParser> parsers = ImList.of();

        Set<Class<?>> workSet = new HashSet<>();
        workSet.add(grammar);

        Set<Class<?>> visited = new HashSet<>();

        while (!workSet.isEmpty()) {
            Class<?> cls = workSet.iterator().next();
            workSet.remove(cls);

            if (visited.contains(cls)) continue;
            visited.add(cls);

            for (var nested : cls.getNestMembers()) {
                if (visited.contains(nested)) continue;
                workSet.add(nested);
            }

            for (var mth : cls.getMethods()) {
                var parserOpt = StaticMethodParser.parse(mth);
                if (parserOpt.isEmpty()) continue;
                parsers = parsers.prepend(parserOpt.get());
            }
        }

        return new SomeParsers(parsers.reverse());
    }

    //region parsersOf(), nonTermTypes()
    private Map<Class<?>, ImList<StaticMethodParser>> byNode;

    private Map<Class<?>, ImList<StaticMethodParser>> getByNode() {
        if (byNode != null) return byNode;
        synchronized (this) {
            if (byNode != null) return byNode;
            Map<Class<?>, ImList<StaticMethodParser>> m = new HashMap<>();

            parsers.each(p -> {
                m.put(
                    p.returnType(),
                    m.getOrDefault(p.returnType(), ImList.of()).prepend(p)
                );
            });

            m.replaceAll((k, v) -> v.reverse());

            byNode = m;
            return byNode;
        }
    }

    private ImList<Class<?>> nonTermTypes;

    public ImList<Class<?>> nonTermTypes() {
        if (nonTermTypes != null) return nonTermTypes;
        synchronized (this) {
            if (nonTermTypes != null) return nonTermTypes;
            nonTermTypes = ImList.of(getByNode().keySet());
            return nonTermTypes;
        }
    }

    public ImList<StaticMethodParser> parsersOf(Class<?> nonTerm) {
        return getByNode().getOrDefault(nonTerm, ImList.of());
    }
    //endregion

    public Result<ImList<ValidatedParser>, String> validate(Lexer lexer) {
        if (lexer == null) throw new IllegalArgumentException("lexer==null");

        var validParsers = ImList.<ValidatedParser>of();
        for (var someParser : parsers) {
            if (someParser.parametersType().length == 0)
                return Result.error("no params for " + someParser);

            var inputPattern = someParser.inputPattern(this, lexer);
            var undefInputCount = inputPattern.foldLeft(0, (acc, it) -> acc + (it.isError() ? 1 : 0));
            if (undefInputCount > 0) {
                return Result.error("undefined input for " + someParser + "\nparameters: " + inputPattern);
            }

            //noinspection OptionalGetWithoutIsPresent
            ImList<Param> validInput = inputPattern.map(i -> i.getOk().get());
            validParsers = validParsers.prepend(new ValidatedParser(
                someParser.returnType(),
                validInput,
                someParser
            ));
        }

        return Result.ok(validParsers.reverse());
    }
}
