package xyz.cofe.json.stream.ast;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.token.IndentTokenWriter;
import xyz.cofe.json.stream.token.SimpleTokenWriter;

import java.io.StringWriter;

public class AstWriterTest {
    @Test
    public void simple(){
        var ast = AstParser.parse("""
            {
                "a": 1,
                b: 2,
                c: [ 4, 5 ]
            }
            """ );
        System.out.println("ast: "+ast);

        System.out.println(AstWriter.toString(ast));
    }

    @Test
    public void indent(){
        var ast = AstParser.parse("""
            {
                "a": 1,
                b: [],
                h: {},
                c: [ 4, 5, 6, 7, 8, 9 ],
                e: {
                  g: 8,
                  h: 9
                },
                f: [ 6, 7 ]
            }
            """ );
        System.out.println("ast: "+ast);

        StringWriter strw = new StringWriter();
        SimpleTokenWriter simpTw = new SimpleTokenWriter(strw);
        IndentTokenWriter identTw = new IndentTokenWriter(simpTw);
        AstWriter.write(identTw, ast);

        System.out.println(strw.toString());
    }

    @Test
    public void construct(){
        var obj = Ast.ObjectAst.create(
            ImList.of(
                Ast.KeyValue.create(Ast.StringAst.create("key"), Ast.StringAst.create("value"))
            )
        );

        System.out.println(AstWriter.toString(obj));
    }
}
