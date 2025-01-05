package xyz.cofe.json.stream.plugin.jre;

import xyz.cofe.coll.im.Fn1;
import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static xyz.cofe.coll.im.Result.error;
import static xyz.cofe.coll.im.Result.ok;

@SuppressWarnings("rawtypes")
public class ArraysPlugin implements StdMapperConfigure {
    @Override
    public void configure(StdMapper mapper) {
        if (mapper == null) return;

        for (var conf : configures) {
            conf.accept(mapper);
        }
    }

    private static final List<Consumer<StdMapper>> configures = new ArrayList<>();
    private static final String BASE64_PREFIX = "base64:";

    static {
        configures.add( mapper -> {
            var byteArr = new byte[0];
            var cls = byteArr.getClass();

            mapper.serializerFor(cls).append( ba -> {
                StringBuilder sb = new StringBuilder();

                var enc = Base64.getEncoder();

                sb.append("base64:");
                sb.append(enc.encodeToString(ba));

                return Optional.of(mapper.toAst(sb.toString()));
            });

            mapper.deserializeFor(cls).append( (ast,stack) -> {
                //noinspection unchecked
                return mapper.tryParse(ast,String.class,stack).fmap( (Fn1) base64str -> {
                    var base64str0 = (String)base64str;
                    if( !base64str0.startsWith(BASE64_PREFIX) ){
                        return error(new RecMapParseError("expect prefix: "+BASE64_PREFIX,stack));
                    }

                    var dec = Base64.getDecoder();
                    try {
                        var arr = dec.decode(base64str0.substring(BASE64_PREFIX.length()));
                        return ok(arr);
                    } catch (IllegalArgumentException e){
                        return error(new RecMapParseError(e,stack));
                    }
                });
            });
        });
    }
}
