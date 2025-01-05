package xyz.cofe.json.stream.plugin.jre;

import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static xyz.cofe.coll.im.Result.error;
import static xyz.cofe.coll.im.Result.ok;

public class MathPlugin implements StdMapperConfigure {
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
            mapper.serializerFor(BigInteger.class)
                .append(n -> Optional.of(mapper.toAst(n.toString())));

            mapper.deserializeFor(BigInteger.class)
                .append((ast, stack) -> mapper.tryParse(ast, String.class, stack).fmap(str -> {
                    try {
                        return ok(new BigInteger(str));
                    } catch (NumberFormatException e) {
                        return error(new RecMapParseError(e, stack));
                    }
                }));
        });
    }

    static {
        configures.add(mapper -> {
            mapper.serializerFor(BigDecimal.class)
                .append(n -> Optional.of(mapper.toAst(n.toString())));

            mapper.deserializeFor(BigDecimal.class)
                .append((ast, stack) -> mapper.tryParse(ast, String.class, stack).fmap(str -> {
                    try {
                        return ok(new BigDecimal(str));
                    } catch (NumberFormatException e) {
                        return error(new RecMapParseError(e, stack));
                    }
                }));
        });
    }

    public record MathCtx(int precision, RoundingMode mode) {}

    static {
        configures.add(mapper -> {
            mapper.serializerFor(MathContext.class)
                .append(ctx -> Optional.of(mapper.toAst(new MathCtx(ctx.getPrecision(), ctx.getRoundingMode()))));

            mapper.deserializeFor(MathContext.class)
                .append((ast, stack) -> mapper.tryParse(ast, MathCtx.class, stack)
                    .map(ctx -> new MathContext(ctx.precision(), ctx.mode())));
        });
    }
}
