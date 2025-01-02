package xyz.cofe.json.stream.query;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstParser;
import xyz.cofe.json.stream.token.CharPointer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuerySetTest {
    @Test
    public void test1() {
        var qs = new QuerySetFin<>(
            AstParser.parse("""
                { a: 1
                , b: [ 1, 2 ]
                }
                """)
        );

        System.out.println(qs);

        System.out.println(qs.get("b"));
        System.out.println(qs.get("b").count());
        System.out.println(qs.get("b").array(i -> true));
    }

    @Test
    public void test2() {
        var qs1 = new QuerySetFin<>(
            AstParser.parse("""
                [ 1
                , { a: 1 }
                , { b: 2 }
                , [ 3 ]
                ]
                """)
        );
        var qs2 = new QuerySetFin<>(AstParser.parse("""
            4
            """
        ));

        var qs = qs1.append(qs2).prepend(QuerySetFin.fromJson("true"));
        System.out.println(qs);

        assertTrue( qs.get(0).map( a -> a instanceof Ast.BooleanAst ).orElse(false) );

        System.out.println(qs.array(i -> true));
    }
}
