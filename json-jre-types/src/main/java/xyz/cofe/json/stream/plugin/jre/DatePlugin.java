package xyz.cofe.json.stream.plugin.jre;

import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class DatePlugin implements StdMapperConfigure {
    public static SimpleDateFormat simpleDateFormat;

    static {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }

    public sealed interface TIME {
        String value();
        Date toJreDate() throws RuntimeException;

        record SomeUtilDate(String value) implements TIME {
            public java.util.Date toJreDate() {
                try {
                    return simpleDateFormat.parse(value);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record BaseUtilDate(String value) implements TIME {
            public java.util.Date toJreDate() {
                try {
                    return simpleDateFormat.parse(value);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record SQLDate(String value) implements TIME {
            public java.sql.Date toJreDate() {
                try {
                    var dt = simpleDateFormat.parse(value);
                    return new java.sql.Date(dt.getTime());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record SQLTime(String value) implements TIME {
            public java.sql.Time toJreDate() {
                try {
                    var dt = simpleDateFormat.parse(value);
                    return new java.sql.Time(dt.getTime());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record SQLTimestamp(String value) implements TIME {
            public java.sql.Timestamp toJreDate() {
                try {
                    var dt = simpleDateFormat.parse(value);
                    return new java.sql.Timestamp(dt.getTime());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        static TIME from(Date date) {
            if (date == null) throw new IllegalArgumentException("dateString==null");
            String str;

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (date) {
                str = simpleDateFormat.format(date);
            }

            if (date.getClass() == java.util.Date.class) {
                return new BaseUtilDate(str);
            } else if (date instanceof java.sql.Date) {
                return new SQLDate(str);
            } else if (date instanceof java.sql.Time) {
                return new SQLTime(str);
            } else if (date instanceof java.sql.Timestamp) {
                return new SQLTimestamp(str);
            } else {
                return new SomeUtilDate(str);
            }
        }
    }

    @Override
    public void configure(StdMapper mapper) {
        if (mapper == null) return;

        conf_java_util_Date(mapper);
        conf_java_sql_Date(mapper);
        conf_java_sql_Time(mapper);
        conf_java_sql_Timestamp(mapper);
    }

    private static void conf_java_sql_Timestamp(StdMapper mapper) {
        mapper.serializerFor(java.sql.Timestamp.class)
            .append(date -> Optional.of(mapper.toAst(TIME.from(date).value())));

        mapper.deserializeFor(java.sql.Timestamp.class)
            .append( (ast,stack) -> mapper.tryParse(ast,String.class,stack)
                .fmap(str -> {
                    try {
                        return Result.ok(new TIME.SQLTimestamp(str).toJreDate());
                    } catch (RuntimeException e){
                        return Result.error(new RecMapParseError(e));
                    }
                }));
    }

    private static void conf_java_sql_Time(StdMapper mapper) {
        mapper.serializerFor(java.sql.Time.class)
            .append(date -> Optional.of(mapper.toAst(TIME.from(date).value())));

        mapper.deserializeFor(java.sql.Time.class)
            .append( (ast,stack) -> mapper.tryParse(ast,String.class,stack)
                .fmap(str -> {
                    try {
                        return Result.ok(new TIME.SQLTime(str).toJreDate());
                    } catch (RuntimeException e){
                        return Result.error(new RecMapParseError(e));
                    }
                }));
    }

    private static void conf_java_sql_Date(StdMapper mapper) {
        mapper.serializerFor(java.sql.Date.class)
            .append(date -> Optional.of(mapper.toAst(TIME.from(date).value())));

        mapper.deserializeFor(java.sql.Date.class)
            .append( (ast,stack) -> mapper.tryParse(ast,String.class,stack)
                .fmap(str -> {
                    try {
                        return Result.ok(new TIME.SQLDate(str).toJreDate());
                    } catch (RuntimeException e){
                        return Result.error(new RecMapParseError(e));
                    }
                }));
    }

    private static void conf_java_util_Date(StdMapper mapper) {
        mapper.serializerFor(Date.class)
            .withSubTypes(true)
            .append(date -> Optional.of(mapper.toAst(TIME.from(date))));

        mapper.deserializeFor(Date.class)
            .append( (ast,stack) -> mapper.tryParse(ast,TIME.class,stack)
                .fmap(time -> {
                    try {
                        return Result.ok(time.toJreDate());
                    } catch (RuntimeException e){
                        return Result.error(new RecMapParseError(e, stack));
                    }
                }));
    }
}
