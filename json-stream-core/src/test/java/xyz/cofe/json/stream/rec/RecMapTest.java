package xyz.cofe.json.stream.rec;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstParser;
import xyz.cofe.json.stream.ast.AstWriter;
import xyz.cofe.json.stream.query.QuerySetFin;
import xyz.cofe.json.stream.token.DummyCharPointer;

import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.cofe.coll.im.Result.ok;

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
    public void test2() {
        RecMapper mapper = new RecMapper();

        var json = mapper.toJson(new NodeC(1, new NodeB("b")));
        System.out.println(json);

        Node restored = mapper.parse(json, Node.class);
        System.out.println(restored);
    }

    @Test
    public void test1() {
        RecMapper mapper = new RecMapper();

        var ast = mapper.toAst(new NodeB("abc"));
        var json = AstWriter.toString(ast);
        System.out.println(json);

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);
    }

    @Test
    public void typeProperty() {
        var propName = "@type";
        RecMapper mapper = new RecMapper(
            SubClassWriter.typeProperty(propName),
            SubClassResolver.typeProperty(propName)
        );

        var ast = mapper.toAst(new NodeB("abc"));
        var json = AstWriter.toString(ast);
        System.out.println(json);

        assertTrue(json.contains("\"@type\""));

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);
    }

    @Test
    public void imListTest() {
        RecMapper mapper = new RecMapper();

        var ast = mapper.toAst(
            new NodeE(
                ImList.of(
                    new NodeB("abc"),
                    new NodeB("def")
                )
            )
        );

        var json = AstWriter.toString(ast);
        System.out.println(json);

        assertTrue(json.contains("abc"));
        assertTrue(json.contains("def"));

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);
    }

    @Test
    public void optional1Test() {
        RecMapper mapper = new RecMapper();

        var ast = mapper.toAst(
            new NodeF(Optional.empty())
        );

        var json = AstWriter.toString(ast);
        System.out.println(json);

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);
    }

    @SuppressWarnings("SimplifiableAssertion")
    @Test
    public void optional2Test() {
        RecMapper mapper = new RecMapper();

        var ast = mapper.toAst(
            new NodeF(Optional.of(new NodeB("xyzAsd")))
        );

        var json = AstWriter.toString(ast);
        System.out.println(json);
        assertTrue(json.contains("xyzAsd"));

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);

        assertTrue(node instanceof NodeF nodeF);

        NodeF nodeF = (NodeF) node;
        assertTrue(nodeF.node().isPresent());
        assertTrue(nodeF.node().map(n -> n instanceof NodeB).orElse(false));
        assertTrue(nodeF.node().map(n -> (NodeB) n).map(n -> n.a().equals("xyzAsd")).orElse(false));
    }

    @Test
    public void primitives() {
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
        assertTrue(Objects.equals(byte_dst, byte_src));

        short short_src = (short) 140;
        var short_str = mapper.toJson(short_src);
        System.out.println(short_str);
        Short short_dst = mapper.parse(short_str, Short.class);
        assertTrue(Objects.equals(short_dst, short_src));

        int int_src = (int) 140;
        var int_str = mapper.toJson(int_src);
        System.out.println(int_str);
        Integer int_dst = mapper.parse(int_str, Integer.class);
        assertTrue(Objects.equals(int_dst, int_src));

        long long_src = (long) 14056;
        var long_str = mapper.toJson(long_src);
        System.out.println(long_str);
        Long long_dst = mapper.parse(long_str, Long.class);
        assertTrue(Objects.equals(long_dst, long_src));

        float float_src = (float) 12.34;
        var float_str = mapper.toJson(float_src);
        System.out.println(float_str);
        Float float_dst = mapper.parse(float_str, Float.class);
        assertTrue(Objects.equals(float_dst, float_src));

        double double_src = (double) 123.34;
        var double_str = mapper.toJson(double_src);
        System.out.println(double_str);
        Double double_dst = mapper.parse(double_str, Double.class);
        assertTrue(Objects.equals(double_dst, double_src));

        char char_src = (char) 'a';
        var char_str = mapper.toJson(char_src);
        System.out.println(char_str);
        Character char_dst = mapper.parse(char_str, Character.class);
        assertTrue(Objects.equals(char_dst, char_src));
    }

    public static enum EnumTest {
        A, B, C;
    }

    @Test
    public void enumTest() {
        RecMapper mapper = new RecMapper();

        var src = EnumTest.A;

        var jsonString = mapper.toJson(src);
        System.out.println(jsonString);

        EnumTest dst = mapper.parse(jsonString, EnumTest.class);
        System.out.println(dst);

        assertTrue(Objects.equals(src, dst));
    }

    @Test
    public void skipFieldSerialization() {
        RecMapper mapper = new RecMapper() {
            @Override
            protected ImList<Ast.KeyValue<DummyCharPointer>> fieldSerialization(FieldToJson fld, ImList<ToAstStack> stack) {
                return
                    fld.fieldName().equals("c") && fld.recordClass() == NodeC.class
                        ? ImList.of()
                        : super.fieldSerialization(fld, stack);
            }
        };

        var src = new NodeC(1, new NodeA());

        var jsonString = mapper.toJson(src);
        System.out.println(jsonString);

        var ast = AstParser.parse(jsonString);
        var qs = new QuerySetFin<>(ast);

        qs = qs.get("NodeC").get("c");
        System.out.println(qs);
        System.out.println(qs.count());
        assertTrue(qs.count() == 0);
    }

    @Test
    public void restoreDefaultFieldSerialization() {
        RecMapper mapper = new RecMapper() {
            @Override
            protected Result<Object, RecMapParseError> resolveOptionalField(
                Ast.ObjectAst<?> objectAst,
                RecordComponent field,
                Optional<RequiredFiled> requiredFiled,
                ImList<ParseStack> stack
            ) {
                if (field.getName().equals("a") &&
                    field.getDeclaringRecord() == NodeB.class
                ) {
                    return ok("aa");
                }

                return super.resolveOptionalField(objectAst, field, requiredFiled, stack);
            }
        };

        Node node =
            mapper.parse(
                """
                    { "NodeB": {} }
                    """, Node.class);

        assertTrue(node instanceof NodeB);
        NodeB nb = (NodeB) node;

        assertTrue(nb.a().equals("aa"));
    }

    @Test
    public void renameFieldSerialization() {
        RecMapper mapper = new RecMapper() {
            @Override
            protected ImList<Ast.KeyValue<DummyCharPointer>> fieldSerialization(FieldToJson fld, ImList<ToAstStack> stack) {
                return super.fieldSerialization(
                    fld.fieldName().equals("c") && fld.recordClass() == NodeC.class
                        ? fld.fieldName("cc")
                        : fld
                    , stack
                );
            }
        };

        var src = new NodeC(1, new NodeA());

        var jsonString = mapper.toJson(src);
        System.out.println(jsonString);

        var ast = AstParser.parse(jsonString);
        var qs = new QuerySetFin<>(ast);

        qs = qs.get("NodeC").get("cc");
        System.out.println(qs);
        System.out.println(qs.count());
        assertTrue(qs.count() == 1);
    }

    @Test
    public void renameFieldDeserialization() {
        var json = """
            {"NodeC":{"b":1,"cc":{"NodeA":{}}}}
            """;

        RecMapper mapper = new RecMapper() {
            @Override
            protected Result<? extends Ast<?>, RequiredFiled> resolveFieldOf(
                Ast.ObjectAst<?> objectAst,
                RecordComponent recordComponent,
                ImList<ParseStack> stack
            ) {
                if (recordComponent.getName().equals("c")
                    && recordComponent.getDeclaringRecord() == NodeC.class
                ) {
                    return Result.from(
                        objectAst.get("cc"),
                        () -> RequiredFiled.of("cc")
                    );
                }

                return super.resolveFieldOf(objectAst, recordComponent, stack);
            }
        };

        Node node = mapper.parse(json, Node.class);
        System.out.println(node);
        assertTrue(node instanceof NodeC);
    }

    public record Custom(LocalDateTime date, long num) {
    }

    @Test
    public void customSerialize() {
        var src = new Custom(LocalDateTime.now(), 12345);
        RecMapper mapper = new RecMapper(
            SubClassWriter.defaultWriter,
            SubClassResolver.defaultResolver()
        ) {
            @Override
            protected Optional<Ast<DummyCharPointer>> customObjectSerialize(Object obj, ImList<ToAstStack> stacks) {
                return obj instanceof LocalDateTime ld
                    ? Optional.of(toAst(ld.toString()))
                    : Optional.empty();
            }

            @Override
            protected Result<Object, RecMapParseError> fieldDeserialization(
                Ast<?> ast,
                RecordComponent field,
                ImList<ParseStack> stack
            ) {
                if (field.getType() == LocalDateTime.class) {
                    return tryParse(ast, String.class)
                        .map(LocalDateTime::parse)
                        .map(v -> (Object) v);
                }

                return super.fieldDeserialization(ast, field, stack);
            }
        };

        String json = mapper.toJson(src);
        System.out.println(json);

        Custom restored = mapper.parse(json, Custom.class);
        System.out.println(restored);

        assertTrue(restored.date.toEpochSecond(ZoneOffset.UTC) == src.date.toEpochSecond(ZoneOffset.UTC));
    }

    public record LstOfStr(ImList<String> lst) {}

    @Test
    public void serializeList1() {
        RecMapper mapper = new RecMapper();

        ImList<String> lst = ImList.of("a", "b");
        var ast = mapper.toAst(lst);
        var json = AstWriter.toString(ast, true);
        System.out.println(json);

        try {
            var lst2 = mapper.parse(ast, ImList.class);
            System.out.println(lst2);
        } catch (RecMapParseError e) {
            assertTrue(e.getMessage().contains("unsupported"));
        }
    }

    @Test
    public void serializeList2() {
        RecMapper mapper = new RecMapper();

        ImList<String> lst = ImList.of("a", "b");
        var ast = mapper.toAst(new LstOfStr(lst));
        var json = AstWriter.toString(ast, true);
        System.out.println(json);

        var lst2 = mapper.parse(ast, LstOfStr.class);
        System.out.println(lst2);
    }

    public record Autonom(String a, int b) {}

    @Test
    public void autonom1() {
        RecMapper mapper = new RecMapper();

        var sample1 = new Autonom("a", 1);
        var ast = mapper.toAst(sample1);
        System.out.println(AstWriter.toString(ast, true));

        var sample2 = mapper.parse(ast, Autonom.class);
        assertTrue(sample2.equals(sample1));
    }

    public record AutonomList(ImList<Autonom> lst) {}

    @Test
    public void autonom2() {
        RecMapper mapper = new RecMapper();

        var sample1 =
            new AutonomList(
                ImList.of(
                    new Autonom("a", 1),
                    new Autonom("b", 2),
                    new Autonom("c", 3)
                )
            );
        var ast = mapper.toAst(sample1);
        System.out.println(AstWriter.toString(ast, true));

        var sample2 = mapper.parse(ast, AutonomList.class);
        assertTrue(sample2.lst.size() == (sample1.lst.size()));
        assertTrue(
            sample2.lst.zip(sample1.lst)
                .foldLeft(true, (sum, it) -> sum && it.map((a, b) -> a.equals(b)))
        );
    }
}
