package xyz.cofe.json.stream.plugin.jre;

import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.rec.RecMapParseError;
import xyz.cofe.json.stream.rec.StdMapper;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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

        for (var conf : configures) {
            conf.accept(mapper);
        }
    }

    private static final List<Consumer<StdMapper>> configures = new ArrayList<>();
    //region java util date

    static {configures.add(DatePlugin::conf_java_sql_Timestamp);}

    private static void conf_java_sql_Timestamp(StdMapper mapper) {
        mapper.serializerFor(java.sql.Timestamp.class)
            .append(date -> Optional.of(mapper.toAst(TIME.from(date).value())));

        mapper.deserializeFor(java.sql.Timestamp.class)
            .append((ast, stack) -> mapper.tryParse(ast, String.class, stack)
                .fmap(str -> {
                    try {
                        return Result.ok(new TIME.SQLTimestamp(str).toJreDate());
                    } catch (RuntimeException e) {
                        return Result.error(new RecMapParseError(e,stack));
                    }
                }));
    }

    static {configures.add(DatePlugin::conf_java_sql_Time);}

    private static void conf_java_sql_Time(StdMapper mapper) {
        mapper.serializerFor(java.sql.Time.class)
            .append(date -> Optional.of(mapper.toAst(TIME.from(date).value())));

        mapper.deserializeFor(java.sql.Time.class)
            .append((ast, stack) -> mapper.tryParse(ast, String.class, stack)
                .fmap(str -> {
                    try {
                        return Result.ok(new TIME.SQLTime(str).toJreDate());
                    } catch (RuntimeException e) {
                        return Result.error(new RecMapParseError(e,stack));
                    }
                }));
    }

    static {configures.add(DatePlugin::conf_java_sql_Date);}

    private static void conf_java_sql_Date(StdMapper mapper) {
        mapper.serializerFor(java.sql.Date.class)
            .append(date -> Optional.of(mapper.toAst(TIME.from(date).value())));

        mapper.deserializeFor(java.sql.Date.class)
            .append((ast, stack) -> mapper.tryParse(ast, String.class, stack)
                .fmap(str -> {
                    try {
                        return Result.ok(new TIME.SQLDate(str).toJreDate());
                    } catch (RuntimeException e) {
                        return Result.error(new RecMapParseError(e,stack));
                    }
                }));
    }

    static {configures.add(DatePlugin::conf_java_util_Date);}

    private static void conf_java_util_Date(StdMapper mapper) {
        mapper.serializerFor(Date.class)
            .withSubTypes(true)
            .append(date -> Optional.of(mapper.toAst(TIME.from(date))));

        mapper.deserializeFor(Date.class)
            .append((ast, stack) -> mapper.tryParse(ast, TIME.class, stack)
                .fmap(time -> {
                    try {
                        return Result.ok(time.toJreDate());
                    } catch (RuntimeException e) {
                        return Result.error(new RecMapParseError(e, stack));
                    }
                }));
    }

    static { configures.add(DatePlugin::conf_GregorianCalendar); }

    private static void conf_GregorianCalendar(StdMapper mapper) {
        mapper.serializerFor(GregorianCalendar.class)
            .append(gcal -> Optional.of(mapper.toAst(
                simpleDateFormat.format(gcal.getTime())
            )));

        mapper.deserializeFor(GregorianCalendar.class)
            .append(((ast, parseStacks) ->
                mapper.tryParse(ast, String.class, parseStacks).fmap(str -> {
                    try {
                        var dt = simpleDateFormat.parse(str);
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(dt);
                        return Result.ok(gc);
                    } catch (ParseException e) {
                        return Result.error(new RecMapParseError(e,parseStacks));
                    }
                })
            ));
    }
    //endregion

    static { configures.add(DatePlugin::conf_instant); }

    private static void conf_instant(StdMapper mapper){
        mapper.serializerFor(Instant.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(Instant.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(Instant.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_LocalDateTime); }

    private static void conf_LocalDateTime(StdMapper mapper){
        mapper.serializerFor(LocalDateTime.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(LocalDateTime.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(LocalDateTime.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_LocalDate); }

    private static void conf_LocalDate(StdMapper mapper){
        mapper.serializerFor(LocalDate.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(LocalDate.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(LocalDate.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_LocalTime); }

    private static void conf_LocalTime(StdMapper mapper){
        mapper.serializerFor(LocalTime.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(LocalTime.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(LocalTime.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_Duration); }

    private static void conf_Duration(StdMapper mapper){
        mapper.serializerFor(Duration.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(Duration.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(Duration.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_MonthDay); }

    private static void conf_MonthDay(StdMapper mapper){
        mapper.serializerFor(MonthDay.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(MonthDay.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(MonthDay.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_Period); }

    private static void conf_Period(StdMapper mapper){
        mapper.serializerFor(Period.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(Period.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(Period.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_OffsetDateTime); }

    private static void conf_OffsetDateTime(StdMapper mapper){
        mapper.serializerFor(OffsetDateTime.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(OffsetDateTime.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(OffsetDateTime.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_OffsetTime); }

    private static void conf_OffsetTime(StdMapper mapper){
        mapper.serializerFor(OffsetTime.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(OffsetTime.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(OffsetTime.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_Year); }

    private static void conf_Year(StdMapper mapper){
        mapper.serializerFor(Year.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(Year.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(Year.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_YearMonth); }

    private static void conf_YearMonth(StdMapper mapper){
        mapper.serializerFor(YearMonth.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(YearMonth.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(YearMonth.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_ZonedDateTime); }

    private static void conf_ZonedDateTime(StdMapper mapper){
        mapper.serializerFor(ZonedDateTime.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(ZonedDateTime.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(ZonedDateTime.parse(str));
                } catch (DateTimeParseException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_ZoneId); }

    private static void conf_ZoneId(StdMapper mapper){
        mapper.serializerFor(ZoneId.class)
            .append(t -> Optional.of(mapper.toAst(t.getId())));

        mapper.deserializeFor(ZoneId.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(ZoneId.of(str));
                } catch (DateTimeException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_ZoneOffset); }

    private static void conf_ZoneOffset(StdMapper mapper){
        mapper.serializerFor(ZoneOffset.class)
            .append(t -> Optional.of(mapper.toAst(t.toString())));

        mapper.deserializeFor(ZoneOffset.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class,stack).fmap(str -> {
                try {
                    return Result.ok(ZoneOffset.of(str));
                } catch (DateTimeException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_DayOfWeek); }

    private static void conf_DayOfWeek(StdMapper mapper){
        mapper.serializerFor(DayOfWeek.class)
            .append(t -> Optional.of(mapper.toAst(t.name())));

        mapper.deserializeFor(DayOfWeek.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class, stack).fmap(str -> {
                try {
                    return Result.ok(DayOfWeek.valueOf(str));
                } catch (IllegalArgumentException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }

    static { configures.add(DatePlugin::conf_Month); }

    private static void conf_Month(StdMapper mapper){
        mapper.serializerFor(Month.class)
            .append(t -> Optional.of(mapper.toAst(t.name())));

        mapper.deserializeFor(Month.class)
            .append((ast,stack) -> mapper.tryParse(ast,String.class, stack).fmap(str -> {
                try {
                    return Result.ok(Month.valueOf(str));
                } catch (IllegalArgumentException e){
                    return Result.error(new RecMapParseError(e,stack));
                }
            }) );
    }
}
