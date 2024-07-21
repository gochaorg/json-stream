package xyz.cofe.json.stream.token;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.ast.AstParser;
import xyz.cofe.json.stream.ast.AstWriter;

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
                c: [ 4, 5 ],
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
}
