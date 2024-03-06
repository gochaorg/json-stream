package xyz.cofe.json.stream.parser.grammar;

import xyz.cofe.coll.im.HTree;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.htree.Nest;
import xyz.cofe.json.stream.parser.grammar.impl.Visit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
         * Обход вложенных узлов
         * @param path путь (0 - корень)
         */
        default void visit(Consumer<ImList<Definition>> path){
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
                        path.accept(targetPath);
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
                    path.last().ifPresent(lst::add);
                });

                var imList = ImList.of(lst);
                Visit.nestedCache.put(root,imList);

                return imList;
            }

            /**
             * Обход вложенных элементов, с информацией о пути доступа
             * @return пути доступа
             */
            public ImList<ImList<Definition>> tree(){
                if(Visit.nestedPathCache.containsKey(root) ){
                    return Visit.nestedPathCache.get(root);
                }

                var lst = new ArrayList<ImList<Definition>>();
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
    public record Sequence(ImList<Definition> seq) implements Definition {}

    /**
     * Альтернативные части
     * @param alt части
     */
    public record Alternative(ImList<Definition> alt) implements Definition {}

    /**
     * Повтор части 0 или более раз
     * @param def часть
     */
    public record Repeat(Definition def) implements Definition {}

    /**
     * Терминальная конструкция
     * @param text текст конструкции
     */
    public record Term(String text) implements Definition {}

    /**
     * НеТерминал
     * @param name ссылка на правило
     */
    public record Ref(String name) implements Definition {}

    /**
     * Создание грамматики
     * @return билдер для грамматики
     */
    public static GrammarBuilder grammar() {
        return new GrammarBuilder();
    }
}
