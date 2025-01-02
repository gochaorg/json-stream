package xyz.cofe.json.stream.rec;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.ast.AstWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.cofe.coll.im.Result.error;
import static xyz.cofe.coll.im.Result.ok;

public class StdMapperTest {
    public record Custom(LocalDateTime date, long num){
    }

    @Test
    public void customSerialize(){
        StdMapper mapper = new StdMapper();

        mapper.serializerFor(LocalDateTime.class)
            .append(ldt -> Optional.of(mapper.toAst(ldt.toString())));

        mapper.deserializeFor(LocalDateTime.class)
            .append(ast ->
                mapper.tryParse(ast, String.class)
                    .fmap( str -> {
                        try {
                            return ok(LocalDateTime.parse(str));
                        }catch (DateTimeParseException e){
                            return error(new RecMapError(e));
                        }
                    })
            );

        var sampleWrite = new Custom(LocalDateTime.now(), 1);
        System.out.println("sample write:\n"+sampleWrite);

        var ast = mapper.toAst(sampleWrite);

        var json = AstWriter.toString(ast,true);
        System.out.println("json:\n"+json);

        var sampleRead = mapper.parse(ast, Custom.class);
        System.out.println("sample read:\n"+sampleRead);

        boolean match = sampleWrite.equals(sampleRead);
        System.out.println("matched: "+match);
        assertTrue(match);
    }
}
