package xyz.cofe.json.stream.token;

import org.junit.jupiter.api.Test;

public class TokenizerTest {
    @Test
    public void test1(){
//        var source = "12 'abc' { } [ ] : , null false true xyz /* comment */";
        var source = "[ 1, true, false, null, [], [ 'abc', 2.5 ] ]";
//        var source = "2.5";
        var parsed = Tokenizer.parse(source);
        parsed.tokens().enumerate().each( te -> {
            System.out.println(""+te.index()+" "+te.value());
        });
    }
}
