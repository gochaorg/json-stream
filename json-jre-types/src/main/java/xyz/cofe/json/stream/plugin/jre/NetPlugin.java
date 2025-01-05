package xyz.cofe.json.stream.plugin.jre;

import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static xyz.cofe.coll.im.Result.error;
import static xyz.cofe.coll.im.Result.ok;

public class NetPlugin implements StdMapperConfigure {
    @Override
    public void configure(StdMapper mapper) {
        if (mapper == null) return;

        for (var conf : configures) {
            conf.accept(mapper);
        }
    }

    private static final List<Consumer<StdMapper>> configures = new ArrayList<>();

    //region URI
    static {
        configures.add(mapper -> {
            mapper.serializerFor(URI.class)
                .append(uri -> Optional.of(mapper.toAst(uri.toString())));

            mapper.deserializeFor(URI.class)
                .append((ast, stack) -> {
                    return mapper.tryParse(ast, String.class, stack).fmap(str -> {
                        try {
                            return ok(URI.create(str));
                        } catch (IllegalArgumentException e) {
                            return error(new RecMapParseError(e, stack));
                        }
                    });
                });
        });
    }
    //endregion

    //region URL
    static {
        configures.add(mapper -> {
            mapper.serializerFor(URL.class)
                .append(uri -> Optional.of(mapper.toAst(uri.toString())));

            mapper.deserializeFor(URL.class)
                .append((ast, stack) -> {
                    return mapper.tryParse(ast, String.class, stack).fmap(str -> {
                        try {
                            return ok(new URL(str));
                        } catch (MalformedURLException e) {
                            return error(new RecMapParseError(e, stack));
                        }
                    });
                });
        });
    }
    //endregion

    //region InetAddress
    private volatile static Boolean storeIpAsName;

    public static boolean storeIpAsName() {
        if (storeIpAsName != null) return storeIpAsName;
        synchronized (NetPlugin.class) {
            if (storeIpAsName != null) return storeIpAsName;
            storeIpAsName =
                "true".equalsIgnoreCase(
                    System.getProperties().getProperty(NetPlugin.class.getName() + ".storeIpAsName", "true")
                );

            return storeIpAsName;
        }
    }

    public static void storeIpAsName(boolean resolveName) {
        storeIpAsName = resolveName;
    }

    static {
        configures.add(mapper -> {
            mapper.serializerFor(InetAddress.class)
                .append(addr -> {
                    if (storeIpAsName()) {
                        return Optional.of(mapper.toAst(addr.getHostName()));
                    } else {
                        return Optional.of(mapper.toAst(addr.getHostAddress()));
                    }
                });

            mapper.deserializeFor(InetAddress.class)
                .append((ast,stack) -> {
                    return mapper.tryParse(ast, String.class, stack).fmap( str -> {
                        try {
                            return ok(InetAddress.getByName(str));
                        } catch (UnknownHostException e) {
                            return error(new RecMapParseError(e,stack));
                        }
                    });
                });
        });
    }
    //endregion
}
