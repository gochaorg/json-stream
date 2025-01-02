package xyz.cofe.json.stream.ast;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.token.StringPointer;
import xyz.cofe.json.stream.token.Tokenizer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AstParseTest {
    @Test
    public void array() {
        var source = "[ 1, true, false, null, [], [ 'abc', 2.5 ] ]";
        var parsed = Tokenizer.parse(source);

        Ast<StringPointer> last = null;
        AstParser<StringPointer> parser = new AstParser.Init<>();
        for (var token : parsed.tokens()) {
            System.out.println("token " + token);

            var astParsed = parser.input(token);
            parser = astParsed.parser();

            astParsed.result().ifPresent(ast -> {
                System.out.println("result " + ast);
            });

            last = astParsed.result().orElse(null);
        }

        assertTrue(last != null);
        assertTrue(last instanceof Ast.ArrayAst<StringPointer>);

        var arr = (Ast.ArrayAst<StringPointer>) last;
        assertTrue(arr.values().size() == 6);
        assertTrue(arr.values().get(0).map(v -> v instanceof Ast.NumberAst.IntAst<StringPointer>).orElse(false));
        assertTrue(arr.values().get(1).map(v -> v instanceof Ast.BooleanAst.TrueAst<StringPointer>).orElse(false));
        assertTrue(arr.values().get(2).map(v -> v instanceof Ast.BooleanAst.FalseAst<StringPointer>).orElse(false));
        assertTrue(arr.values().get(3).map(v -> v instanceof Ast.NullAst<StringPointer>).orElse(false));
        assertTrue(arr.values().get(4).map(v -> v instanceof Ast.ArrayAst<StringPointer>).orElse(false));
        assertTrue(arr.values().get(5).map(v -> v instanceof Ast.ArrayAst<StringPointer>).orElse(false));

        var arr1 = (Ast.ArrayAst<StringPointer>) arr.values().get(4).get();
        assertTrue(arr1.values().size() == 0);

        var arr2 = (Ast.ArrayAst<StringPointer>) arr.values().get(5).get();
        assertTrue(arr2.values().size() == 2);
        assertTrue(arr2.values().get(0).map(v -> v instanceof Ast.StringAst<StringPointer>).orElse(false));
        assertTrue(arr2.values().get(1).map(v -> v instanceof Ast.NumberAst.DoubleAst<StringPointer>).orElse(false));
    }

    @Test
    public void obj() {
        var source = """
            { "a" : 1
            , "b" : { }
            , "c" : [ ]
            , "d" : [ 1 ]
            , "e" : { "f" : true
                    }
            }
            """;
        var parsed = Tokenizer.parse(source);

        Ast<StringPointer> last = null;
        AstParser<StringPointer> parser = new AstParser.Init<>();
        for (var token : parsed.tokens()) {
            System.out.println("token " + token);

            var astParsed = parser.input(token);
            parser = astParsed.parser();

            astParsed.result().ifPresent(ast -> {
                System.out.println("result " + ast);
            });

            if( astParsed.result().isPresent() ){
                last = astParsed.result().orElse(null);
            }
        }

        System.out.println("last = "+last);
    }
}
