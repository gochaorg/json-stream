package xyz.cofe.json.stream.plugin.ann;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.rec.StdMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SimplifiableAssertion")
public class TypeNameTest {
    public sealed interface Node {
        @TypeName()
        record NodeA() implements Node {}
        record NodeB() implements Node {}
        @TypeName(value = {"NC","nc"},writeName = "Nc")
        record NodeC(Node n) implements Node {}
        record NodeD(Node a, Node b) implements Node {}
    }

    @Test
    public void test1(){
        StdMapper mapper = new StdMapper();

        Node sampleWrite = new Node.NodeD(
            new Node.NodeA(),
            new Node.NodeD(
                new Node.NodeB(),
                new Node.NodeC(new Node.NodeA())
            )
        );
        var json = mapper.toJson(sampleWrite,true);

        System.out.println("stored json:");
        System.out.println(json);

        json = json.replace("\"Nc\"", "\"nc\"");
        System.out.println("restore json:");
        System.out.println(json);

        Node sampleRead = mapper.parse(json,Node.class);
        System.out.println(sampleRead);

        assertTrue(sampleRead.equals(sampleWrite));
    }
}
