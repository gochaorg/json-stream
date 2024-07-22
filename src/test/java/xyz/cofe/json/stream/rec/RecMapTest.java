package xyz.cofe.json.stream.rec;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.AstWriter;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecMapTest {
    public sealed interface Node {
    }

    public record NodeA() implements Node {
    }

    public record NodeB(String a) implements Node {
    }

    public record NodeC(int b, Node c) implements Node {
    }

    public record NodeD(int b, Node c, Node d) implements Node {
    }

    public record NodeE(ImList<Node> nodes) implements Node {
    }

    public record NodeF(Optional<Node> node) implements Node {
    }

    @Test
    public void test1(){
        RecMapper mapper = new RecMapper();

        var ast = mapper.toJson(new NodeB("abc"));
        var json = AstWriter.toString( ast );
        System.out.println(json);

        var node = mapper.fromJson(ast, Node.class);
        System.out.println(node);
    }

    @Test
    public void imListTest(){
        RecMapper mapper = new RecMapper();

        var ast = mapper.toJson(
            new NodeE(
                ImList.of(
                    new NodeB("abc"),
                    new NodeB("def")
                )
            )
        );

        var json = AstWriter.toString( ast );
        System.out.println(json);

        assertTrue(json.contains("abc"));
        assertTrue(json.contains("def"));

        var node = mapper.fromJson(ast, Node.class);
        System.out.println(node);
    }

    @Test
    public void optionalTest(){
        RecMapper mapper = new RecMapper();

        var ast = mapper.toJson(
            new NodeF(Optional.empty())
        );

        var json = AstWriter.toString( ast );
        System.out.println(json);

        var node = mapper.fromJson(ast, Node.class);
        System.out.println(node);
    }
}
