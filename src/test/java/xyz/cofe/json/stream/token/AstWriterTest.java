package xyz.cofe.json.stream.token;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.ast.AstParser;
import xyz.cofe.json.stream.ast.AstWriter;

public class AstWriterTest {
    @Test
    public void simple(){
        var ast = AstParser.parse("{ \"a\": 1, b: 2 }" );
        System.out.println("ast: "+ast);

        System.out.println(AstWriter.toString(ast));
    }
}
