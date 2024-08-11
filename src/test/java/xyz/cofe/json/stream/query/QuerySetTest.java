package xyz.cofe.json.stream.query;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.ast.AstParser;

public class QuerySetTest {
    @Test
    public void test1(){
        var qs = new QuerySetFin<>(
            AstParser.parse("""
                { a: 1
                , b: [ 1, 2 ]
                }
                """)
        );

        System.out.println(qs);

        System.out.println( qs.get("b") );
        System.out.println( qs.get("b").count() );
        System.out.println( qs.get("b").array(i -> true) );
    }
}
