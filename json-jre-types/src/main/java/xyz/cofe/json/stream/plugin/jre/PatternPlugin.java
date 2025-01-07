package xyz.cofe.json.stream.plugin.jre;

import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static xyz.cofe.coll.im.Result.error;
import static xyz.cofe.coll.im.Result.ok;

public class PatternPlugin implements StdMapperConfigure {
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
            mapper.serializerFor( Pattern.class )
                .append( pattern -> Optional.of( mapper.toAst(pattern.pattern()) ));

            mapper.deserializeFor( Pattern.class )
                .append( (ast, stacks) -> mapper.tryParse(ast, String.class).fmap(str -> {
                    try {
                        return ok(Pattern.compile(str));
                    } catch (PatternSyntaxException e){
                        return error(new RecMapParseError(e,stacks));
                    }
                }));
        });
    }
}
