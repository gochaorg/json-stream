package xyz.cofe.json.stream.token;

import org.junit.jupiter.api.Test;

public class TokenizerTest {
    @Test
    public void test1(){
        var source = "12 'abc' { } [ ] : , null false true xyz /* comment */";
        var parsed = Tokenizer.parse(source);
        parsed.tokens().enumerate().each( te -> {
            System.out.println(""+te.index()+" "+te.value());
        });
    }
}
