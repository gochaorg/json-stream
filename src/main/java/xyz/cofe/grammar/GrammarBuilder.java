package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GrammarBuilder {
    private final List<Grammar.Rule> rules = new ArrayList<>();

    public sealed interface BuildPart {
        public sealed interface AltPart extends BuildPart {}
        ;
        record Ready(Grammar.Definition def) implements BuildPart,
                                                        AltPart {}
        record Alt() implements BuildPart {}
        record Repeat(RuleBuildState nested) implements BuildPart,
                                                        AltPart {}
    }

    public static class RuleBuildState {
        public final String ruleName;
        public final List<BuildPart> buildParts;

        public RuleBuildState(String ruleName, List<BuildPart> buildParts) {
            this.ruleName = ruleName;
            this.buildParts = buildParts;
        }

        public Grammar.Rule build() {
            if (buildParts.isEmpty()) throw new IllegalStateException("empty rule!");

            List<List<BuildPart.AltPart>> altParts = new ArrayList<>();
            List<BuildPart.AltPart> curAltPart = new ArrayList<>();
            for (var part : buildParts) {
                if (part instanceof BuildPart.AltPart a) {
                    curAltPart.add(a);
                } else {
                    if (!curAltPart.isEmpty()) {
                        altParts.add(curAltPart);
                        curAltPart = new ArrayList<>();
                    }
                }
            }
            if (!curAltPart.isEmpty()) altParts.add(curAltPart);

            Grammar.Definition def = null;
            if (altParts.isEmpty()) throw new IllegalStateException("empty!");
            if (altParts.size() == 1) {
                def = build(altParts.get(0));
            } else {
                var alts = ImList.<Grammar.Definition>of();
                for (var part : altParts) {
                    alts = alts.prepend(build(part));
                }
                def = new Grammar.Alternative(alts.reverse());
            }

            return new Grammar.Rule(ruleName, def);
        }

        private Grammar.Definition build(List<BuildPart.AltPart> parts) {
            if (parts.isEmpty()) throw new IllegalArgumentException("parts.isEmpty()");

            Grammar.Definition res = null;

            for (var aPart : parts) {
                Grammar.Definition value = null;
                if( aPart instanceof BuildPart.Repeat a ){
                    value = new Grammar.Repeat(a.nested().build().definition());
                }else if( aPart instanceof BuildPart.Ready a ){
                    value = a.def();
                }

                if( res == null ){
                    res = value;
                }else if( res instanceof Grammar.Sequence gs ){
                    res = new Grammar.Sequence(gs.seq().append(value));
                }else{
                    res = new Grammar.Sequence(ImList.of(res, value));
                }
            }

            return res;
        }
    }

    public static class RuleBuilder {
        private final RuleBuildState state;

        public RuleBuilder(String ruleName, Consumer<Supplier<Grammar.Rule>> buildItConsumer) {
            if (ruleName == null) throw new IllegalArgumentException("ruleName==null");
            if (buildItConsumer == null) throw new IllegalArgumentException("buildItConsumer==null");
            state = new RuleBuildState(ruleName, new ArrayList<>());
            buildItConsumer.accept(this::build);
        }

        private RuleBuilder(RuleBuildState state) {
            this.state = state;
        }

        public RuleBuilder term(String term) {
            if (term == null) throw new IllegalArgumentException("term==null");
            state.buildParts.add(new BuildPart.Ready(
                new Grammar.Term(term)
            ));
            return this;
        }

        public RuleBuilder ref(String refRuleName) {
            if (refRuleName == null) throw new IllegalArgumentException("refRuleName==null");
            state.buildParts.add(new BuildPart.Ready(
                new Grammar.Ref(refRuleName)
            ));
            return this;
        }

        public RuleBuilder repeat(Consumer<RuleBuilder> rb) {
            if (rb == null) throw new IllegalArgumentException("rb==null");
            var st = new RuleBuildState(state.ruleName, new ArrayList<>());
            state.buildParts.add(new BuildPart.Repeat(st));
            rb.accept(new RuleBuilder(st));
            return this;
        }

        public RuleBuilder alt() {
            var st = new RuleBuildState(state.ruleName, new ArrayList<>());
            state.buildParts.add(new BuildPart.Alt());
            return this;
        }

        private Grammar.Rule build() {
            return state.build();
        }
    }

    public GrammarBuilder rule(String name, Consumer<RuleBuilder> rb) {
        if (name == null) throw new IllegalArgumentException("name==null");
        if (rb == null) throw new IllegalArgumentException("rb==null");

        //noinspection unchecked
        Supplier<Grammar.Rule>[] buildItArr = new Supplier[]{null};

        var r = new RuleBuilder(name, buildIt -> {
            buildItArr[0] = buildIt;
        });

        rb.accept(r);

        var buildIt = buildItArr[0];
        if (buildIt == null) throw new IllegalStateException("!");

        rules.add(buildIt.get());
        return this;
    }

    public Grammar build() {
        if (rules.isEmpty()) throw new IllegalStateException("no rules");
        return new Grammar(ImList.from(rules));
    }
}
