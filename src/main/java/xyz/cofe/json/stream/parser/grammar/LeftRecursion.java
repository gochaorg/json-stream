package xyz.cofe.json.stream.parser.grammar;

import xyz.cofe.coll.im.HTree;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.htree.ImListNest;
import xyz.cofe.coll.im.htree.Nest;
import xyz.cofe.coll.im.htree.OptionalNest;
import xyz.cofe.coll.im.htree.RecordNest;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * Варианты левой рекурсии
 * <p>
 * 1) <pre> A ::= A </pre>
 * <pre>
 * Rule(A){ Ref(A) }
 * </pre>
 * <p>
 * 2)
 * <pre>
 * A ::= B
 * B ::= A
 * </pre>
 * <p>
 * <pre>
 * Rule(A){ Ref(B) }
 * Rule(B){ Ref(A) }
 * </pre>
 * <p>
 * 3)
 * <pre>
 * A ::= B | A
 * </pre>
 * <p>
 * <pre>
 * Rule(A){
 *   Alt(
 *     Ref(B),
 *     Ref(A)
 *   )
 * }
 * </pre>
 * <p>
 * 4)
 * <pre>A ::= { B } A</pre>
 * <pre>
 * Rule(A){
 *   Seq(
 *     Repeat( Ref(B) ),
 *     Ref(A)
 *   )
 * }
 * </pre>
 * 5)
 * <pre>A ::= { A }</pre>
 * <pre>
 * Rule(A){
 *   Repeat(A)
 * }
 * </pre>
 */
public record LeftRecursion(RecursiveRef leftRecursion) {
    public static ImList<LeftRecursion> find(Grammar grammar) {
        Objects.requireNonNull(grammar);

        var recursions = RecursiveRef.find(grammar);

        var r0 = recursions.map(recur -> recur.lastRefNode().map(refPathNode -> {
            var refOwnerRule = refPathNode.rule();
            var ref = refPathNode.ref();

            var refPaths = new ArrayList<ImList<Nest.PathNode>>();

            HTree.visit(
                refOwnerRule.definition(),
                new Object() {
                    void enter(ImList<Nest.PathNode> revPath) {
                        revPath.head().ifPresent(n -> {
                            if (n.pathValue() == ref) {
                                refPaths.add(0,revPath);
                            }
                        });
                    }
                }
            );

            if (refPaths.size() == 1 && tokenOffset(refPaths.get(0), refOwnerRule) == 0) {
                return Optional.of(recur);
            }

            return Optional.<RecursiveRef>empty();
        }));

        var r1 = r0
            .fmap(b -> b.isEmpty() ? ImList.of() : ImList.of(b.get()))
            .fmap(b -> b.isEmpty() ? ImList.of() : ImList.of(b.get()));

        return r1.map(LeftRecursion::new);
    }

    private static int tokenOffset(ImList<Nest.PathNode> revPath, Grammar.Rule refOwnerRule) {
        if( revPath.tail().size()==0 )return 0; // тривиальные случаи
        if( revPath.size()<1 )return -1;

        //noinspection OptionalGetWithoutIsPresent
        Nest.PathNode head = revPath.head().get();

        switch (head){
            case RecordNest.RecordIt it -> {}
            case OptionalNest.OptionalValue it -> {}
            case ImListNest.ImListItValue it -> {
                if( it.value() instanceof Grammar.Definition def ){
                }
            }
            case Nest.RootPathNode it -> {}
            case Nest.NestItValue it -> {}
        }

        return -1;
    }

    private static int minWeightOf(Grammar.Definition def) {
        return switch (def){
            case Grammar.Ref r -> 1;
            case Grammar.Term t -> 1;
            case Grammar.Repeat r -> 0;
            case Grammar.Alternative a ->
                a.alt().size()==0
                    ? 0
                    : a.alt().map(LeftRecursion::minWeightOf).foldLeft( Integer.MAX_VALUE, Math::min);
            case Grammar.Sequence s -> s.seq().map(LeftRecursion::minWeightOf).foldLeft( 0, Integer::sum);
            case null, default -> 0;
        };
    }
}
