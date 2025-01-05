package xyz.cofe.json.stream.plugin.jre;

import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CharsetPlugin implements StdMapperConfigure {
    @Override
    public void configure(StdMapper mapper) {
        if (mapper == null) return;

        for (var conf : configures) {
            conf.accept(mapper);
        }
    }

    private static final List<Consumer<StdMapper>> configures = new ArrayList<>();

    static {
        configures.add( mapper -> {
            mapper.serializerFor(Charset.class)
                .append( cs -> Optional.of(mapper.toAst(cs.name())));

            mapper.deserializeFor(Charset.class)
                .append( (ast,stack) -> mapper.tryParse(ast, String.class,stack).fmap( str -> {
                    try {
                        return Result.ok(Charset.forName(str));
                    } catch (IllegalCharsetNameException | UnsupportedCharsetException e){
                        return Result.error(new RecMapParseError(e,stack));
                    }
                }) );
        });
    }
}
