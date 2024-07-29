package xyz.cofe.json.stream.rec;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.AstWriter;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SimplifiableAssertion")
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

        var ast = mapper.toAst(new NodeB("abc"));
        var json = AstWriter.toString( ast );
        System.out.println(json);

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);
    }

    @Test
    public void imListTest(){
        RecMapper mapper = new RecMapper();

        var ast = mapper.toAst(
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

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);
    }

    @Test
    public void optional1Test(){
        RecMapper mapper = new RecMapper();

        var ast = mapper.toAst(
            new NodeF(Optional.empty())
        );

        var json = AstWriter.toString( ast );
        System.out.println(json);

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);
    }

    @SuppressWarnings("SimplifiableAssertion")
    @Test
    public void optional2Test(){
        RecMapper mapper = new RecMapper();

        var ast = mapper.toAst(
            new NodeF(Optional.of(new NodeB("xyzAsd")))
        );

        var json = AstWriter.toString( ast );
        System.out.println(json);
        assertTrue(json.contains("xyzAsd"));

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);

        assertTrue(node instanceof NodeF nodeF);

        NodeF nodeF = (NodeF)node;
        assertTrue(nodeF.node().isPresent());
        assertTrue(nodeF.node().map( n -> n instanceof NodeB).orElse(false));
        assertTrue(nodeF.node().map( n -> (NodeB)n ).map( n -> n.a().equals("xyzAsd")).orElse(false));
    }

    @Test
    public void primitives(){
        RecMapper mapper = new RecMapper();

        boolean bool_src = true;
        var bool_str = mapper.toJson(bool_src);
        System.out.println(bool_str);
        Boolean bool_dst = mapper.parse(bool_str, Boolean.class);
        assertTrue(bool_dst);

        byte byte_src = (byte) 140;
        var byte_str = mapper.toJson(byte_src);
        System.out.println(byte_str);
        Byte byte_dst = mapper.parse(byte_str, Byte.class);
        assertTrue(Objects.equals(byte_dst,byte_src));

        short short_src = (short) 140;
        var short_str = mapper.toJson(short_src);
        System.out.println(short_str);
        Short short_dst = mapper.parse(short_str, Short.class);
        assertTrue(Objects.equals(short_dst,short_src));

        int int_src = (int) 140;
        var int_str = mapper.toJson(int_src);
        System.out.println(int_str);
        Integer int_dst = mapper.parse(int_str, Integer.class);
        assertTrue(Objects.equals(int_dst,int_src));

        long long_src = (long) 14056;
        var long_str = mapper.toJson(long_src);
        System.out.println(long_str);
        Long long_dst = mapper.parse(long_str, Long.class);
        assertTrue(Objects.equals(long_dst,long_src));

        float float_src = (float) 12.34;
        var float_str = mapper.toJson(float_src);
        System.out.println(float_str);
        Float float_dst = mapper.parse(float_str, Float.class);
        assertTrue(Objects.equals(float_dst,float_src));

        double double_src = (double) 123.34;
        var double_str = mapper.toJson(double_src);
        System.out.println(double_str);
        Double double_dst = mapper.parse(double_str, Double.class);
        assertTrue(Objects.equals(double_dst,double_src));

        char char_src = (char) 'a';
        var char_str = mapper.toJson(char_src);
        System.out.println(char_str);
        Character char_dst = mapper.parse(char_str, Character.class);
        assertTrue(Objects.equals(char_dst,char_src));
    }

    public static enum EnumTest {
        A, B, C;
    }

    @Test
    public void enumTest(){
        RecMapper mapper = new RecMapper();

        var src = EnumTest.A;

        var jsonString = mapper.toJson(src);
        System.out.println(jsonString);

        EnumTest dst = mapper.parse(jsonString, EnumTest.class);
        System.out.println(dst);

        assertTrue(Objects.equals(src,dst));
    }
}
