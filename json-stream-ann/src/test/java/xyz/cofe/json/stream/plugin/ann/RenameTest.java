package xyz.cofe.json.stream.plugin.ann;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstWriter;
import xyz.cofe.json.stream.rec.StdMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenameTest {
    public record RenameRec(
        @Rename("aa")
        String a,

        @Rename(value = "bb", otherNames = {"bbb", "bbbb"}, serializeName = "b2")
        String b
    ) {}

    @Test
    public void test(){
        StdMapper mapper = new StdMapper();

        var saveSample = new RenameRec("a","b");
        var ast = mapper.toAst(saveSample);
        var json = AstWriter.toString(ast,true);
        System.out.println(json);

        var aobj = (Ast.ObjectAst<?>)ast;
        assertTrue(aobj.get("b2").isPresent());
        assertTrue(aobj.get("aa").isPresent());

        RenameRec rr1 = mapper.parse(
            """
                {
                    "aa": "a",
                    "bb": "b"
                }
                """,
            RenameRec.class
        );

        RenameRec rr2 = mapper.parse(
            """
                {
                    "aa": "a",
                    "bbb": "b"
                }
                """,
            RenameRec.class
        );
    }
}
