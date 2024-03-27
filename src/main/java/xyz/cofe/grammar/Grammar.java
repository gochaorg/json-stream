package xyz.cofe.grammar;

import xyz.cofe.coll.im.HTree;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.htree.Nest;
import xyz.cofe.grammar.impl.Visit;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Грамматика
 * @param rules Набор правил грамматики
 */
public record Grammar(
    ImList<Rule> rules
) {
    /**
     * Возвращает правило по имени
     * @param name имя
     * @return правило
     */
    public ImList<Rule> rule(String name){
        if( name==null ) throw new IllegalArgumentException("name==null");
        var lst = DuplicateRuleName.ruleMapOf(this).get(name);
        return lst==null ? ImList.of() : ImList.of(lst);
    }

    /**
     * Правило
     *
     * @param name       имя
     * @param definition определение
     */
    public record Rule(String name, Definition definition) {
        public Rule {
            Objects.requireNonNull(name);
            Objects.requireNonNull(definition);
        }

        /**
         * Возвращает индекс инструкции
         * @param def искомая инструкция
         * @return -1 если не найдена в списке, <br>
         * индекс соответствующий обходу walk()
         */
        public int indexOf(Definition def){
            if( def==null ) throw new IllegalArgumentException("def==null");
            return definition.walk().go().enumerate().find( e -> e.value()==def ).map( e -> (int)e.index() ).orElse(-1);
        }
    }

    /**
     * Определение правила - правая часть вывода
     */
    public sealed interface Definition {
        /**
         * Путь в правиле до инструкции
         * @param directPath путь до инструкции
         */
        record DefPath( ImList<Definition> directPath ) {
            public DefPath {
                Objects.requireNonNull(directPath);
            }

            public static DefPath of(Grammar.Rule rule){
                if( rule==null ) throw new IllegalArgumentException("rule==null");
                return new DefPath(ImList.of(rule.definition));
            }

            public DefPath append(Definition def){
                if( def==null ) throw new IllegalArgumentException("def==null");
                return new DefPath(directPath.append(def));
            }

            /**
             * Определение на которую ссылается путь
             * @return целевая инструкция
             */
            public Definition definition(){
                var d = directPath.last();
                if( d.isEmpty() )throw new IllegalStateException("empty path");
                return d.get();
            }

            /**
             * Поиск инструкции в правиле
             * @param rule правило
             * @param def инструкция
             * @return Расположение
             */
            public static ImList<DefPath> find(Grammar.Rule rule, Definition def){
                Objects.requireNonNull(rule);
                Objects.requireNonNull(def);

                return rule.definition().walk().tree().filter( d -> d.definition()==def );
            }
        }

        /**
         * Обход вложенных узлов
         * @param path путь (0 - корень)
         */
        default void visit(Consumer<DefPath> path){
            if( path==null ) throw new IllegalArgumentException("revPath==null");
            HTree.visit(this,new Object(){
                public void enter(ImList<Nest.PathNode> revPath){
                    if( revPath.head().map(h -> h.pathValue() instanceof Definition).orElse(false) ) {
                        var targetPath = revPath.reverse().fmap(
                            n -> {
                                if (n.pathValue() instanceof Definition d) {
                                    return ImList.of(d);
                                } else {
                                    return ImList.of();
                                }
                            }
                        );
                        path.accept(new DefPath(targetPath));
                    }
                }
            });
        }

        /**
         * Обход вложенных элементов
         * @return Обход
         */
        default Walk walk(){
            return new Walk(this);
        }

        /**
         * Обход вложенных элементов
         */
        public static final class Walk {
            private final Definition root;

            private Walk(Definition root) {
                this.root = root;
            }

            /**
             * Обход вложенных элементов
             * @return элементы
             */
            public ImList<Definition> go(){
                if(Visit.nestedCache.containsKey(root) ){
                    return Visit.nestedCache.get(root);
                }

                var lst = new ArrayList<Definition>();
                root.visit(path -> {
                    lst.add(path.definition());
                });

                var imList = ImList.of(lst);
                Visit.nestedCache.put(root,imList);

                return imList;
            }

            /**
             * Обход вложенных элементов, с информацией о пути доступа
             * @return пути доступа
             */
            public ImList<DefPath> tree(){
                if(Visit.nestedPathCache.containsKey(root) ){
                    return Visit.nestedPathCache.get(root);
                }

                var lst = new ArrayList<DefPath>();
                root.visit(lst::add);

                var imList = ImList.of(lst);
                Visit.nestedPathCache.put(root,imList);

                return imList;
            }
        }
    }

    /**
     * Последовательность частей
     * @param seq последовательность
     */
    public record Sequence(ImList<Definition> seq) implements Definition {
        public Sequence {
            Objects.requireNonNull(seq);
        }
    }

    /**
     * Альтернативные части
     * @param alt части
     */
    public record Alternative(ImList<Definition> alt) implements Definition {
        public Alternative {
            Objects.requireNonNull(alt);
        }
    }

    /**
     * Повтор части 0 или более раз
     * @param def часть
     */
    public record Repeat(Definition def) implements Definition {
        public Repeat {
            Objects.requireNonNull(def);
        }
    }

    /**
     * Терминальная конструкция
     * @param text текст конструкции
     */
    public record Term(String text) implements Definition {
        public Term {
            Objects.requireNonNull(text);
        }
    }

    /**
     * НеТерминал
     * @param name ссылка на правило
     */
    public record Ref(String name) implements Definition {
        public Ref {
            Objects.requireNonNull(name);
        }
    }

    /**
     * Создание грамматики
     * @return билдер для грамматики
     */
    public static GrammarBuilder grammar() {
        return new GrammarBuilder();
    }
}
