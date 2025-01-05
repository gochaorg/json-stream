package xyz.cofe.json.stream.plugin.jre;

import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FileNamesPlugin implements StdMapperConfigure {
    @Override
    public void configure(StdMapper mapper) {
        if (mapper == null) return;

        for (var conf : configures) {
            conf.accept(mapper);
        }
    }

    private static final List<Consumer<StdMapper>> configures = new ArrayList<>();

    static {
        configures.add(mapper -> {
            mapper.serializerFor(File.class)
                .append(fname -> Optional.of(mapper.toAst(fname.toString())));

            mapper.deserializeFor(File.class)
                .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap( str -> {
                    return Result.ok(new File(str));
                }));
        });
    }

    static {
        configures.add(mapper -> {
            mapper.serializerFor(Path.class)
                .append(fname -> Optional.of(mapper.toAst(fname.toString())));

            mapper.deserializeFor(Path.class)
                .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap( str -> {
                    try {
                        return Result.ok(Path.of(str));
                    } catch (InvalidPathException e){
                        return Result.error(new RecMapParseError(e,stack));
                    }
                }));
        });
    }
}
