package xyz.cofe.json.stream.query;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstParser;
import xyz.cofe.json.stream.token.CharPointer;
import xyz.cofe.json.stream.token.StringPointer;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SimplifiableAssertion")
public class QuerySetTest {
    @Test
    public void creating1(){
        var qs = new QuerySetFin<>(
            AstParser.parse("""
                { a: 1
                , b: [ 1, 2 ]
                }
                """)
        );

        System.out.println(qs);
        String json = qs.toString();

        var ast = AstParser.parse(json);
        assertTrue(ast instanceof Ast.ArrayAst<StringPointer> a);

        var arrayAst = (Ast.ArrayAst<StringPointer>) ast;
        assertTrue(arrayAst.values().size()==1);

        var objAst = (Ast.ObjectAst<StringPointer>)arrayAst.values().get(0).get();
        assertTrue( objAst.get("a").map( v -> v.asInt().map( n -> n==1 ).orElse(false)).orElse(false) );

        var arr2Ast = (Ast.ArrayAst<StringPointer>)objAst.get("b").get();
        assertTrue(arr2Ast.values().size()==2);
        assertTrue(arr2Ast.values().get(0).map( a -> (Ast.NumberAst.IntAst<?>) a ).map( n -> n.value() == 1).orElse(false));
        assertTrue(arr2Ast.values().get(1).map( a -> (Ast.NumberAst.IntAst<?>) a ).map( n -> n.value() == 2).orElse(false));
    }

    @Test
    public void creating2(){
        var qs = QuerySetFin.fromJson(
            """
                { a: 1
                , b: [ 1, 2 ]
                }
                """
        );

        System.out.println(qs);
        String json = qs.toString();

        var ast = AstParser.parse(json);
        assertTrue(ast instanceof Ast.ArrayAst<StringPointer> a);

        var arrayAst = (Ast.ArrayAst<StringPointer>) ast;
        assertTrue(arrayAst.values().size()==1);

        var objAst = (Ast.ObjectAst<StringPointer>)arrayAst.values().get(0).get();
        assertTrue( objAst.get("a").map( v -> v.asInt().map( n -> n==1 ).orElse(false)).orElse(false) );

        var arr2Ast = (Ast.ArrayAst<StringPointer>)objAst.get("b").get();
        assertTrue(arr2Ast.values().size()==2);
        assertTrue(arr2Ast.values().get(0).map( a -> (Ast.NumberAst.IntAst<?>) a ).map( n -> n.value() == 1).orElse(false));
        assertTrue(arr2Ast.values().get(1).map( a -> (Ast.NumberAst.IntAst<?>) a ).map( n -> n.value() == 2).orElse(false));
    }

    @Test
    public void get(){
        var qs = QuerySetFin.fromJson(
            """
                { a: 1
                , b: [ 1, 2 ]
                , b: "x"
                }
                """
        );

        qs = qs.get("b");

        var expectJson = Ast.parse(
            """
                [[1,2],"x"]
                """
        ).toJson();

        var actualJson = qs.pretty(false).toString();

        System.out.println(actualJson);
        System.out.println(expectJson);
        assertTrue(actualJson.equals(expectJson));
    }

    @Test
    public void iterate(){
        var qs = QuerySetFin.fromJson(
            """
                { a: 1
                , b: [ 1, 2 ]
                , b: "x"
                }
                """
        );

        var idx = -1;
        for( Ast<?> ast : qs.get("b") ){
            idx++;
            if( idx==0 ) assertTrue( ast.toJson().equals("[1,2]") );
            if( idx==1 ) assertTrue( ast.toJson().equals("\"x\"") );
        }

        assertTrue(idx==1);
    }

    @Test
    public void fmap(){
        var qs = QuerySetFin.fromJson(
            """
                { a: 1
                , b: [ 1, 2 ]
                , b: "x"
                }
                """
        );

        var idx = -1;
        for( Ast<?> ast : qs.get("b").fmap( a -> a instanceof Ast.ArrayAst<StringPointer> arr ? ImList.of(a).iterator() : ImList.<Ast<StringPointer>>of().iterator() ) ){
            idx++;
            if( idx==0 ) assertTrue( ast.toJson().equals("[1,2]") );
        }

        assertTrue(idx==0);
    }

    @Test
    public void map(){
        var srcPtr = new StringPointer("",0);

        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                { a: 1
                , b: [ 1, 2 ]
                , b: "x"
                }
                """
        );

        qs = qs.get("b").map( v ->
            v instanceof Ast.ArrayAst<StringPointer>
            ? Ast.NumberAst.IntAst.create( 1, srcPtr, srcPtr )
            : v instanceof Ast.StringAst<StringPointer>
            ? Ast.NumberAst.IntAst.create( 2, srcPtr, srcPtr )
            : v
        );

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[1,2]"));
    }

    @Test
    public void filter(){
        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                { b: 1
                , b: 2
                , b: 3
                , b: 4
                }
                """
        );

        qs = qs.get("b").filter( a -> a.asInt().map(n -> (n % 2)==0 ).orElse(false) );

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[2,4]"));
    }

    @Test
    public void take(){
        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                { b: 1
                , b: 2
                , b: 3
                , b: 4
                }
                """
        );

        qs = qs.get("b").take(2);

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[1,2]"));
    }

    @Test
    public void skip(){
        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                { b: 1
                , b: 2
                , b: 3
                , b: 4
                }
                """
        );

        qs = qs.get("b").skip(2);

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[3,4]"));
    }

    @Test
    public void append(){
        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                { b: 1
                , b: 2
                }
                """
        );

        qs = qs.get("b").append(
            QuerySetFin.fromJson(
                """
                    { b: 3
                    , b: 4
                    }
                    """
            ).get("b")
        );

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[1,2,3,4]"));
    }

    @Test
    public void prepend(){
        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                { b: 1
                , b: 2
                }
                """
        );

        qs = qs.get("b").prepend(
            QuerySetFin.fromJson(
                """
                    { b: 3
                    , b: 4
                    }
                    """
            ).get("b")
        );

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[3,4,1,2]"));
    }

    @Test
    public void keyValueFlatMap(){
        var srcPtr = new StringPointer("",0);

        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                { a: 1
                , b: 2
                }
                """
        );

        qs = qs.append(
            QuerySetFin.fromJson(
                """
                    { c: 3
                    , d: 4
                    }
                    """
            )
        );

        qs = qs.keyValueFlatMap( kv -> {
            if( kv.key().value().equals("a") )return ImList.of( Ast.StringAst.create("aa", srcPtr, srcPtr));
            if( kv.key().value().equals("b") )return ImList.of(
                Ast.StringAst.create("bb1", srcPtr, srcPtr),
                Ast.StringAst.create("bb2", srcPtr, srcPtr)
            );
            return ImList.of(
                Ast.StringAst.create( kv.key().value().repeat( kv.value().asInt().orElse(0) ), srcPtr, srcPtr )
            );
        });

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[\"aa\",\"bb1\",\"bb2\",\"ccc\",\"dddd\"]"));
    }

    @Test
    public void arrayFlatMap(){
        var srcPtr = new StringPointer("",0);

        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                [ 1
                , 2
                , 3
                ]
                """
        );

        qs = qs.arrayFlatMap( (idx,el) -> {
            if( idx==0 )return ImList.of( Ast.StringAst.create("first",srcPtr,srcPtr) );
            if( idx==1 )return ImList.of(
                Ast.StringAst.create("one",srcPtr,srcPtr),
                Ast.StringAst.create("one one",srcPtr,srcPtr)
            );
            return ImList.of();
        });

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[\"first\",\"one\",\"one one\"]"));
    }

    @Test
    public void sort(){
        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                [ 4
                , 2
                , 3
                , 1
                , 5
                ]
                """
        );

        qs = qs.array( idx -> true ).sort( (a,b) -> {
            return a.asInt().flatMap( na -> b.asInt().map( nb -> na - nb ) ).orElse(0);
        });

        System.out.println(qs);

        assertTrue(Ast.parse(qs.toString()).toJson().equals("[1,2,3,4,5]"));
    }

    @Test
    public void fold(){
        QuerySetFin<StringPointer> qs = QuerySetFin.fromJson(
            """
                [ 4
                , 2
                , 3
                , 1
                , 5
                ]
                """
        );

        var n = qs.array( idx -> true ).foldLeft( 0, (sum,it) -> sum + it.asInt().orElse(0) );
        System.out.println(n);

        assertTrue(n == 15);
    }

    @Test
    public void count(){
        var qs = QuerySetFin.fromJson(
            """
                { a: 1
                , b: [ 1, 2 ]
                , b: "x"
                }
                """
        );

        qs = qs.get("b");

        assertTrue(qs.count()==2);
    }

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
