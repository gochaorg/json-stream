package xyz.cofe.json.stream.rec.test;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstWriter;
import xyz.cofe.json.stream.rec.RecMapError;
import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.RecMapTest;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.SubClassResolver;
import xyz.cofe.json.stream.rec.SubClassWriter;
import xyz.cofe.json.stream.token.CharPointer;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.cofe.coll.im.Result.error;
import static xyz.cofe.coll.im.Result.ok;

@SuppressWarnings("SimplifiableAssertion")
public class StdMapperTest {
    public record Custom(LocalDateTime date, long num){
    }

    @Test
    public void customSerialize(){
        StdMapper mapper = new StdMapper();

        mapper.serializerFor(LocalDateTime.class)
            .append(ldt -> Optional.of(mapper.toAst(ldt.toString())));

        mapper.deserializeFor(LocalDateTime.class)
            .append((ast,stack) ->
                mapper.tryParse(ast, String.class)
                    .fmap( str -> {
                        try {
                            return ok(LocalDateTime.parse(str));
                        }catch (DateTimeParseException e){
                            return error(new RecMapParseError(e));
                        }
                    })
            );

        var sampleWrite = new Custom(LocalDateTime.now(), 1);
        System.out.println("sample write:\n"+sampleWrite);

        var ast = mapper.toAst(sampleWrite);

        var json = AstWriter.toString(ast,true);
        System.out.println("json:\n"+json);

        var sampleRead = mapper.parse(ast, Custom.class);
        System.out.println("sample read:\n"+sampleRead);

        boolean match = sampleWrite.equals(sampleRead);
        System.out.println("matched: "+match);
        assertTrue(match);
    }

    public record ReName(String a, String b) {}

    @Test
    public void renameFields(){
        StdMapper mapper = new StdMapper();

        mapper
            .fieldSerialize(ReName.class, "a")
            .rename("aa")
            .append();

        mapper.fieldDeserialize(ReName.class,"a")
            .name("aa")
            .append();

        var sampleWrite = new ReName("1","2");

        var ast = mapper.toAst(sampleWrite);
        var astObj = (Ast.ObjectAst<?>) ast;
        assertTrue( astObj.get("aa").isPresent() );

        var json = AstWriter.toString(ast,true);
        System.out.println(json);

        var sampleRead = mapper.parse(ast, ReName.class);
        assertTrue(sampleRead.equals(sampleWrite));
    }

    public record CustomField(String a, int b){
    }

    @Test
    public void customField(){
        StdMapper mapper = new StdMapper();

        mapper.fieldSerialize(CustomField.class, "b")
            .rename("bb")
            .valueMapper( (v, stack) -> mapper.toAst(v.toString()) )
            .append();

        mapper.fieldDeserialize(CustomField.class, "b")
            .name("bb")
            .parser( ast -> Integer.parseInt(mapper.parse(ast, String.class)) )
            .append();

        var sampleWrite = new CustomField("abc", 1);

        var ast = mapper.toAst(sampleWrite);
        var astObj = (Ast.ObjectAst<?>) ast;
        var json = AstWriter.toString(ast,true);
        System.out.println(json);

        assertTrue( astObj.get("bb").isPresent() );
        assertTrue( astObj.get("bb").map( a -> a instanceof Ast.StringAst<? extends CharPointer<?>>).orElse(false) );

        var sampleRead = mapper.parse(ast, CustomField.class);
        assertTrue(sampleRead.equals(sampleWrite));
    }

    @Test
    public void skipField(){
        StdMapper mapper = new StdMapper();

        mapper.fieldSerialize(CustomField.class, "b")
            .skip()
            .append();

        mapper.fieldDeserialize(CustomField.class, "b")
            .defaultValue( 2 )
            .append();

        var sampleWrite = new CustomField("abc", 1);

        var ast = mapper.toAst(sampleWrite);
        var astObj = (Ast.ObjectAst<?>) ast;
        var json = AstWriter.toString(ast,true);
        System.out.println(json);

        assertTrue( astObj.get("b").isEmpty() );

        var sampleRead = mapper.parse(ast, CustomField.class);
        assertTrue(sampleRead.b()==2);
    }

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
    public void nodeSample(){
        StdMapper mapper = new StdMapper();

        var ast = mapper.toAst(new NodeB("abc"));
        var json = AstWriter.toString(ast, true);
        System.out.println(json);

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);

        //////////

        ast = mapper.toAst(
            new NodeE(
                ImList.of(
                    new NodeB("abc"),
                    new NodeB("def")
                )
            )
        );

        json = AstWriter.toString(ast, true);
        System.out.println(json);

        assertTrue(json.contains("abc"));
        assertTrue(json.contains("def"));

        node = mapper.parse(ast, Node.class);
        System.out.println(node);

        ///////////

        ast = mapper.toAst(
            new NodeF(Optional.empty())
        );

        json = AstWriter.toString(ast, true);
        System.out.println(json);

        node = mapper.parse(ast, Node.class);
        System.out.println(node);

    }

    @Test
    public void subTypeWrite(){
        StdMapper mapper = new StdMapper();

        mapper.subTypeWriter(NodeB.class)
            .writer(SubClassWriter.simpleClassName( ignr -> "node-b"))
            .append();

        mapper.subTypeResolve(Node.class)
            .resolver(SubClassResolver.defaultResolver(
                Map.of(
                    "NodeD",NodeD.class,
                    "NodeA",NodeA.class,
                    "node-b", NodeB.class
                )
            ))
            .append();

        Node sampleWrite = new NodeD(1, new NodeB("a"), new NodeA());

        var json = mapper.toJson(sampleWrite, true);
        System.out.println(json);

        Node sampleRead = mapper.parse( json, Node.class);
        System.out.println(sampleRead);

        assertTrue(sampleRead.equals(sampleWrite));
    }
}
