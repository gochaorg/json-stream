package xyz.cofe.grammar;

import xyz.cofe.coll.im.ImList;

/**
 * Рекурсивный путь
 *
 * @param revPath путь
 */
public record RecursivePath(
    ImList<RecursiveNode> revPath
) {
    public static RecursivePath init(Grammar.Rule rule) {
        if (rule == null) throw new IllegalArgumentException("rule==null");
        return new RecursivePath(
            ImList.of(RecursiveNode.of(rule))
        );
    }

    public RecursivePath add(RecursiveNode node) {
        if (node == null) throw new IllegalArgumentException("node==null");
        return new RecursivePath(revPath.prepend(node));
    }
}
