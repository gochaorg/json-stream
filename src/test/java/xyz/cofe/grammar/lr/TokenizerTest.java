package xyz.cofe.grammar.lr;

import org.junit.jupiter.api.Test;
import xyz.cofe.coll.im.ImList;

public class TokenizerTest {
    @Test
    public void try1(){
        var tokenizer = new Tokenizer(
            new Whitespace(),
            new IntNum(),
            new Keyword.Parser()
        );

        tokenizer.parse("1 + 2 * 3 / ( 4 - 5 )")
            .enumerate()
            .each( en -> {
                System.out.println("["+en.index()+"] "+en.value().token());
            });
    }
}
