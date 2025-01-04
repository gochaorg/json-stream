package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.Fn3;
import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstParser;
import xyz.cofe.json.stream.ast.AstWriter;
import xyz.cofe.json.stream.token.DummyCharPointer;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Сериализация sealed типов
 */
public class RecMapper {
    /** Указывает способ указания подтипа */
    private final SubClassWriter subClassWriter;

    /** Способ получения имени/типа экземпляра */
    private final SubClassResolver subClassResolver;

    public RecMapper(){
        this.subClassWriter = SubClassWriter.defaultWriter;
        this.subClassResolver = SubClassResolver.defaultResolver();
    }

    /**
     * Конструктор
     * @param subClassWriter - способ указания подтипа
     * @param subClassResolver - способ получения имени/типа экземпляра
     */
    public RecMapper(
        SubClassWriter subClassWriter,
        SubClassResolver subClassResolver
    ){
        if( subClassWriter==null ) throw new IllegalArgumentException("subClassWriter==null");
        this.subClassWriter = subClassWriter;

        if( subClassResolver==null ) throw new IllegalArgumentException("subClassResolver==null");
        this.subClassResolver = subClassResolver;
    }

    //region toAst() primitives

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.StringAst<DummyCharPointer> toAst(String value) {
        return Ast.StringAst.create(value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.StringAst<DummyCharPointer> toAst(char value) {
        return Ast.StringAst.create("" + value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.NumberAst.IntAst<DummyCharPointer> toAst(byte value) {
        return Ast.NumberAst.IntAst.create(0xFF & value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.NumberAst.IntAst<DummyCharPointer> toAst(short value) {
        return Ast.NumberAst.IntAst.create(value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.NumberAst.IntAst<DummyCharPointer> toAst(int value) {
        return Ast.NumberAst.IntAst.create(value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.NumberAst.LongAst<DummyCharPointer> toAst(long value) {
        return Ast.NumberAst.LongAst.create(value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.NumberAst.BigIntAst<DummyCharPointer> toAst(BigInteger value) {
        return Ast.NumberAst.BigIntAst.create(value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.NumberAst.DoubleAst<DummyCharPointer> toAst(float value) {
        return Ast.NumberAst.DoubleAst.create(value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.NumberAst.DoubleAst<DummyCharPointer> toAst(double value) {
        return Ast.NumberAst.DoubleAst.create(value);
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.BooleanAst<DummyCharPointer> toAst(boolean value) {
        return Ast.NumberAst.BooleanAst.create(value);
    }

    /**
     * Формирование AST узла
     *
     * @return узел ast
     */
    public Ast.NullAst<DummyCharPointer> nullToAst() {
        return Ast.NumberAst.NullAst.create();
    }

    /**
     * Формирование AST узла
     *
     * @param value значение узла
     * @return узел ast
     */
    public Ast.IdentAst<DummyCharPointer> identToAst(String value) {
        return Ast.NumberAst.IdentAst.create(value);
    }
    //endregion

    //region enumToAst()
    private Ast<DummyCharPointer> enumToAst(Enum<?> value) {
        return Ast.StringAst.create(value.name());
    }
    //endregion

    //region toAst() containers

    private Function<Object, Optional<Ast<DummyCharPointer>>> customObjectSerialize = DefaultObjectSerialize;

    /**
     * Указывает собственный способ кодирования
     *
     * @return собственный способ кодирования
     */
    public Function<Object, Optional<Ast<DummyCharPointer>>> customObjectSerialize() {
        return customObjectSerialize;
    }

    /**
     * Указывает собственный способ кодирования
     *
     * @param ser - функция ( value ) -> Optional &lt;Ast&gt; - если функция возвращает empty, то используется алгоритм по умолчанию
     * @return SELF ссылка
     */
    @SuppressWarnings("UnusedReturnValue")
    public RecMapper customObjectSerialize(Function<Object, Optional<Ast<DummyCharPointer>>> ser) {
        if (ser == null) throw new IllegalArgumentException("ser==null");
        customObjectSerialize = ser;
        return this;
    }

    /**
     * Собственный способ кодирования по умолчанию (empty)
     */
    public static final Function<Object, Optional<Ast<DummyCharPointer>>> DefaultObjectSerialize
        = obj -> Optional.empty();

    /**
     * Формирование Ast
     *
     * @param record значение
     * @return узел ast
     */
    public Ast<DummyCharPointer> toAst(Object record) {
        var custom = customObjectSerialize.apply(record);
        if (custom.isPresent()) {
            return custom.get();
        }

        if (record == null) return nullToAst();

        Class<?> cls = record.getClass();
        if (cls.isRecord()) {
            return recordToAst(record, cls);
        }

        if (record instanceof Iterable<?> iter) {
            return iterableToAst(iter);
        }

        if (cls.isArray()) return arrayToAst(record);

        if (cls.isEnum()) return enumToAst((Enum<?>) record);

        if (record instanceof Boolean)
            return toAst((boolean) (Boolean) record);

        if (record instanceof String)
            return toAst((String) record);

        if (record instanceof Character)
            return toAst("" + (Character) record);

        if (record instanceof Integer)
            return toAst((int) (Integer) record);

        if (record instanceof Byte)
            return toAst(0xFF & ((byte) (Byte) record));

        if (record instanceof Short)
            return toAst((int) (Short) record);

        if (record instanceof Long)
            return toAst((long) (Long) record);

        if (record instanceof Double)
            return toAst((double) (Double) record);

        if (record instanceof Float)
            return toAst((double) (Float) record);

        if (record instanceof BigInteger)
            return toAst((BigInteger) record);

        throw new RecMapError("can't serialize " + cls);
    }

    /**
     * Кодирование значения в json string
     *
     * @param record значение
     * @return json string
     */
    public String toJson(Object record) {
        if (record == null) throw new IllegalArgumentException("record==null");
        return AstWriter.toString(toAst(record));
    }

    public String toJson(Object record, boolean pretty) {
        if (record == null) throw new IllegalArgumentException("record==null");
        return AstWriter.toString(toAst(record), pretty);
    }

    /**
     * Кодирование поля Record
     *
     * @param record          Объект record
     * @param recordClass     Класс объекта record
     * @param fieldName       Имя поля json, берется из имени поля record
     * @param fieldValue      Значение поля record
     * @param recordComponent Описание поля record
     * @param keyMapper       Кодирование ключа по умолчанию
     * @param valueMapper     Кодирование значения по умолчанию
     */
    public record FieldToJson(
        Object record,
        Class<?> recordClass,
        String fieldName,
        Object fieldValue,
        RecordComponent recordComponent,
        Function<String, Ast.Key<DummyCharPointer>> keyMapper,
        Function<Object, Ast<DummyCharPointer>> valueMapper
    ) {
        public FieldToJson fieldName(String name) {
            if (name == null) throw new IllegalArgumentException("name==null");
            return new FieldToJson(record, recordClass, name, fieldValue, recordComponent, keyMapper, valueMapper);
        }

        public FieldToJson keyMapper(Function<String, Ast.Key<DummyCharPointer>> keyMapper){
            if( keyMapper==null ) throw new IllegalArgumentException("keyMapper==null");
            return new FieldToJson(record, recordClass, fieldName, fieldValue, recordComponent, keyMapper, valueMapper);
        }

        public FieldToJson valueMapper(Function<Object, Ast<DummyCharPointer>> valueMapper){
            if( keyMapper==null ) throw new IllegalArgumentException("keyMapper==null");
            return new FieldToJson(record, recordClass, fieldName, fieldValue, recordComponent, keyMapper, valueMapper);
        }
    }

    private Function<FieldToJson, Optional<Ast.KeyValue<DummyCharPointer>>> fieldSerialization
        = DefaultFieldSerialization;

    public Function<FieldToJson, Optional<Ast.KeyValue<DummyCharPointer>>> fieldSerialization() {
        return fieldSerialization;
    }

    @SuppressWarnings("UnusedReturnValue")
    public RecMapper fieldSerialization(Function<FieldToJson, Optional<Ast.KeyValue<DummyCharPointer>>> serialization) {
        if (serialization == null) throw new IllegalArgumentException("serialization==null");
        fieldSerialization = serialization;
        return this;
    }

    @SuppressWarnings({"unused", "OptionalGetWithoutIsPresent"})
    public static final Function<FieldToJson, Optional<Ast.KeyValue<DummyCharPointer>>> DefaultFieldSerialization = fieldToJson -> {
        if (fieldToJson.fieldValue() == null) return Optional.empty();
        if (fieldToJson.fieldValue() instanceof Optional<?> opt && opt.isEmpty()) return Optional.empty();

        var recJsonValue =
            fieldToJson
                .valueMapper().apply(
                    fieldToJson.fieldValue() instanceof Optional<?> opt ? opt.get() : fieldToJson.fieldValue());

        var recJsonName = fieldToJson.keyMapper.apply(fieldToJson.fieldName());

        return Optional.of(Ast.KeyValue.create(recJsonName, recJsonValue));
    };

    private Ast<DummyCharPointer> recordToAst(Object record, Class<?> cls) {
        var items = ImList.<Ast.KeyValue<DummyCharPointer>>of();
        for (var recCmpt : cls.getRecordComponents()) {
            var recName = recCmpt.getName();
            try {
                var recValue = recCmpt.getAccessor().invoke(record);

                var f2j = fieldSerialization.apply(new FieldToJson(
                    record, cls, recName, recValue, recCmpt, this::toAst, this::toAst
                ));
                if (f2j.isEmpty()) continue;
                items = items.prepend(f2j.get());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RecMapError("can't read record component " + recName, e);
            }
        }

        var body = Ast.ObjectAst.create(items.reverse());

        return subClassWriter.write(body, record, this);
    }

    private Ast<DummyCharPointer> iterableToAst(Iterable<?> iterable) {
        ImList<Ast<DummyCharPointer>> lst = ImList.of();
        for (var it : iterable) {
            var a = toAst(it);
            lst = lst.prepend(a);
        }
        return Ast.ArrayAst.create(lst.reverse());
    }

    private Ast<DummyCharPointer> arrayToAst(Object array) {
        ImList<Ast<DummyCharPointer>> lst = ImList.of();
        var arrLen = Array.getLength(array);
        for (var ai = 0; ai < arrLen; ai++) {
            lst = lst.prepend(
                toAst(Array.get(array, ai))
            );
        }
        return Ast.ArrayAst.create(lst.reverse());
    }
    //endregion

    //region parsing part
    /**
     * Стек парсера
     */
    public sealed interface ParseStack {
        record parseAstType(Ast<?> ast, Type type) implements ParseStack {}
        record parseStringType(String json, Type type) implements ParseStack {}
        record imListParse<T>(Ast.ArrayAst<?> ast, BiFunction<Ast<?>, ImList<ParseStack>, T> itemParse)
            implements ParseStack {}
        record listParse<T>(Ast.ArrayAst<?> ast, BiFunction<Ast<?>, ImList<ParseStack>, T> itemParse)
            implements ParseStack {}
        record optionalParse<T>(Ast<?> ast, BiFunction<Ast<?>, ImList<ParseStack>, T> itemParse)
            implements ParseStack {}
        record tryParse<T>(Ast<?> ast, Class<T> cls) implements ParseStack {}
        record parseAstClass<T>(Ast<?> ast, Class<T> cls) implements ParseStack {}
        record parseSealedInterface<T>(Ast<?> ast, Class<T> cls) implements ParseStack {}
        record parseSubclass<T>(Ast<?> ast, Class<T> subClass) implements ParseStack {}
        record parseRecord<T>(Ast.ObjectAst<?> objAst, Class<T> recordClass) implements ParseStack {}
        record parseEnum<T>(Ast<?> ast, Class<T> enumCls) implements ParseStack {}
        record parserOf(Type type) implements ParseStack {}
    }

    /**
     * Парсинг ast
     *
     * @param ast  узел ast
     * @param type Целевой тип данных
     * @param <T>  результат парсинга
     * @return целевой тип данных
     */
    public <T> T parse(Ast<?> ast, Type type) {
        return parse(ast, type, ImList.of());
    }

    @SuppressWarnings("unchecked")
    protected <T> T parse(Ast<?> ast, Type type, ImList<ParseStack> stack) {
        if (ast == null) throw new IllegalArgumentException("ast==null");
        if (type == null) throw new IllegalArgumentException("type==null");

        stack = stack.prepend(new ParseStack.parseAstType(ast, type));

        if (type instanceof Class<?> cls) {
            return (T) parse(ast, cls, stack);
        }

        var parser = parserOf(type, stack);
        if (parser.isPresent()) {
            return (T) parser.get().apply(ast, stack);
        }

        throw new RecMapError("unsupported target type: " + type);
    }

    /**
     * Парсинг json
     *
     * @param json строка json
     * @param type целевой тип
     * @param <T>  результат парсинга
     * @return результат парсинга
     */
    public <T> T parse(String json, Type type) {
        return parse(json, type, ImList.of());
    }

    protected <T> T parse(String json, Type type, ImList<ParseStack> stack) {
        if (json == null) throw new IllegalArgumentException("json==null");
        if (type == null) throw new IllegalArgumentException("type==null");

        stack = stack.prepend(new ParseStack.parseStringType(json, type));

        var jsnObj = AstParser.parse(json);
        return parse(jsnObj, type, stack);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> Optional<BiFunction<Ast<?>, ImList<ParseStack>, T>> parserOf(Type type, ImList<ParseStack> stack) {
        stack = stack.prepend(new ParseStack.parserOf(type));

        if (type instanceof Class<?> cls) {
            return Optional.of(
                (Ast<?> ast, ImList<ParseStack> stack1) -> (T) parse(ast, cls, stack1));
        }

        if (type instanceof ParameterizedType pt) {
            if (pt.getRawType() == ImList.class && pt.getActualTypeArguments().length == 1) {
                var itemParserOpt = parserOf(pt.getActualTypeArguments()[0], stack);
                if (itemParserOpt.isEmpty()) return Optional.empty();

                BiFunction itemParser = itemParserOpt.get();
                BiFunction parser = (ast, stack1) -> {
                    if (!(ast instanceof Ast.ArrayAst arr)) {
                        throw new RecMapParseError("expect json array (" + Ast.ArrayAst.class.getSimpleName() + "), actual: " + ast.getClass().getSimpleName(), (ImList<ParseStack>) stack1);
                    }

                    return imListParse(arr, itemParser, (ImList<ParseStack>) stack1);
                };

                return Optional.of(parser);
            }

            if (pt.getRawType() == List.class && pt.getActualTypeArguments().length == 1) {
                var itemParserOpt = parserOf(pt.getActualTypeArguments()[0], stack);
                if (itemParserOpt.isEmpty()) return Optional.empty();

                BiFunction itemParser = itemParserOpt.get();
                BiFunction parser = (ast, stack1) -> {
                    if (!(ast instanceof Ast.ArrayAst arr)) {
                        throw new RecMapParseError("expect json array (" + Ast.ArrayAst.class.getSimpleName() + "), actual: " + ast.getClass().getSimpleName(), (ImList<ParseStack>) stack1);
                    }
                    return listParse(arr, itemParser, (ImList<ParseStack>) stack1);
                };

                return Optional.of(parser);
            }

            if (pt.getRawType() == Optional.class && pt.getActualTypeArguments().length == 1) {
                var itemParserOpt = parserOf(pt.getActualTypeArguments()[0], stack);
                if (itemParserOpt.isEmpty()) return Optional.empty();

                BiFunction itemParser = itemParserOpt.get();
                BiFunction parser = (ast, stack1) -> {
                    if (!(ast instanceof Ast ast1)) {
                        throw new RecMapParseError("expect json value, actual: " + ast.getClass().getSimpleName(), (ImList<ParseStack>) stack1);
                    }
                    return optionalParse(ast1, itemParser, (ImList<ParseStack>) stack1);
                };

                return Optional.of(parser);
            }
        }

        return Optional.empty();
    }

    protected <T> ImList<T> imListParse(Ast.ArrayAst<?> ast, BiFunction<Ast<?>, ImList<ParseStack>, T> itemParse, ImList<ParseStack> stack) {
        var stack1 = stack.prepend(new ParseStack.imListParse<T>(ast, itemParse));
        return ast.values().map(a -> itemParse.apply(a, stack1));
    }

    protected <T> List<T> listParse(Ast.ArrayAst<?> ast, BiFunction<Ast<?>, ImList<ParseStack>, T> itemParse, ImList<ParseStack> stack) {
        var stack1 = stack.prepend(new ParseStack.listParse<T>(ast, itemParse));
        return ast.values().map(a -> itemParse.apply(a, stack1)).toList();
    }

    protected <T> Optional<T> optionalParse(Ast<?> ast, BiFunction<Ast<?>, ImList<ParseStack>, T> itemParse, ImList<ParseStack> stack) {
        if (ast instanceof Ast.NullAst<?> nullAst) {
            return Optional.empty();
        }

        var stack1 = stack.prepend(new ParseStack.optionalParse<T>(ast, itemParse));
        return Optional.of(itemParse.apply(ast, stack1));
    }

    /**
     * Парсинг ast
     *
     * @param ast json ast дерево
     * @param cls целевой тип
     * @param <T> целевой тип
     * @return результат парсинга
     */
    public <T> Result<T, RecMapError> tryParse(Ast<?> ast, Class<T> cls) {
        return tryParse(ast, cls, ImList.of());
    }

    protected <T> Result<T, RecMapError> tryParse(Ast<?> ast, Class<T> cls, ImList<ParseStack> stack) {
        stack = stack.prepend(new ParseStack.tryParse<T>(ast, cls));

        if (ast == null) return Result.error(new RecMapParseError(new IllegalArgumentException("ast==null"), stack));
        if (cls == null) return Result.error(new RecMapParseError(new IllegalArgumentException("cls==null"), stack));

        try {
            return Result.ok(parse(ast, cls, stack));
        } catch (RecMapError e) {
            return Result.error(e);
        }
    }

    /**
     * Парсинг ast
     *
     * @param ast json ast дерево
     * @param cls целевой тип
     * @param <T> целевой тип
     * @return результат парсинга
     */
    public <T> T parse(Ast<?> ast, Class<T> cls) {
        return parse(ast, cls, ImList.of());
    }

    @SuppressWarnings("unchecked")
    protected <T> T parse(Ast<?> ast, Class<T> cls, ImList<ParseStack> stack) {
        if (ast == null) throw new IllegalArgumentException("ast==null");
        if (cls == null) throw new IllegalArgumentException("cls==null");

        stack = stack.prepend(new ParseStack.parseAstClass<T>(ast, cls));

        if (ast instanceof Ast.NullAst<?>) return null;

        if (cls == String.class) {
            if (ast instanceof Ast.StringAst<?> str) {
                return (T) str.value();
            } else {
                throw new RecMapParseError("expect string in json", stack);
            }
        } else if (cls == Boolean.class || cls == boolean.class) {
            if (ast instanceof Ast.BooleanAst.TrueAst<?>) {
                return (T) Boolean.TRUE;
            } else if (ast instanceof Ast.BooleanAst.FalseAst<?>) {
                return (T) Boolean.FALSE;
            } else {
                throw new RecMapParseError("expect boolean in json", stack);
            }
        } else if (cls == Byte.class || cls == byte.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Byte) (byte) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Byte) ((Double) dblValue.value()).byteValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Byte) ((Long) lngValue.value()).byteValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Byte) bigValue.value().byteValue();
            throw new RecMapParseError("can't convert to short from " + ast.getClass().getSimpleName(), stack);
        } else if (cls == Short.class || cls == short.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Short) (short) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Short) ((Double) dblValue.value()).shortValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Short) ((Long) lngValue.value()).shortValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Short) bigValue.value().shortValue();
            throw new RecMapParseError("can't convert to short from " + ast.getClass().getSimpleName(), stack);
        } else if (cls == Integer.class || cls == int.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Integer) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Integer) ((Double) dblValue.value()).intValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Integer) ((Long) lngValue.value()).intValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Integer) bigValue.value().intValue();
            throw new RecMapParseError("can't convert to int from " + ast.getClass().getSimpleName(), stack);
        } else if (cls == Long.class || cls == long.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Long) (long) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Long) ((Double) dblValue.value()).longValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue) return (T) (Long) lngValue.value();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Long) bigValue.value().longValue();
            throw new RecMapParseError("can't convert to long from " + ast.getClass().getSimpleName(), stack);
        } else if (cls == BigInteger.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) BigInteger.valueOf(intValue.value());
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) BigInteger.valueOf(((long) dblValue.value()));
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue) return (T) BigInteger.valueOf(lngValue.value());
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) bigValue.value();
            throw new RecMapParseError("can't convert to BigInteger from " + ast.getClass().getSimpleName(), stack);
        } else if (cls == Float.class || cls == float.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Float) (float) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue) return (T) (Float) (float) dblValue.value();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Float) ((Long) lngValue.value()).floatValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Float) bigValue.value().floatValue();
            throw new RecMapParseError("can't convert to double from " + ast.getClass().getSimpleName(), stack);
        } else if (cls == Double.class || cls == double.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Double) (double) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue) return (T) (Double) dblValue.value();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Double) ((Long) lngValue.value()).doubleValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Double) bigValue.value().doubleValue();
            throw new RecMapParseError("can't convert to double from " + ast.getClass().getSimpleName(), stack);
        } else if (cls == Character.class || cls == char.class) {
            if (ast instanceof Ast.StringAst<?> strValue) {
                if (strValue.value().isEmpty())
                    throw new RecMapParseError("can't convert to char empty string", stack);

                return (T) (Character) strValue.value().charAt(0);
            }
            throw new RecMapParseError("can't convert to char from " + ast.getClass().getSimpleName(), stack);
        } else if (cls.isSealed() && cls.isInterface()) {
            return parseSealedInterface(ast, cls, stack);
        } else if (cls.isRecord() && ast instanceof Ast.ObjectAst<?> objAst) {
            return parseRecord(objAst, cls, stack);
        } else if (cls.isEnum()) {
            return parseEnum(ast, cls, stack);
        }

        throw new RecMapParseError("unsupported " + cls, stack);
    }

    @SuppressWarnings("unchecked")
    private <T> T parseSealedInterface(Ast<?> ast, Class<T> cls, ImList<ParseStack> stack) {
        var stack1 = stack.prepend(new ParseStack.parseSealedInterface<T>(ast, cls));
        return subClassResolver.resolve(ast, cls.getPermittedSubclasses()).fold(
            resolved -> (T) parseSubclass(resolved.body(), resolved.klass(), stack1),
            err -> {
                throw new RecMapParseError(err, stack1);
            }
        );
    }

    private <T> T parseSubclass(Ast<?> ast, Class<T> subClass, ImList<ParseStack> stack) {
        stack = stack.prepend(new ParseStack.parseSubclass<T>(ast, subClass));
        if (ast instanceof Ast.ObjectAst<?> objAst) {
            if (subClass.isRecord()) {
                return parseRecord(objAst, subClass, stack);
            } else {
                throw new RecMapParseError("expect " + subClass + " is record", stack);
            }
        } else {
            throw new RecMapParseError("expect Ast.ObjectAst", stack);
        }
    }

    /**
     * Парсинг ast в поле record
     *
     * @param recordComponent Компонент record
     * @param objectAst       ast дерево, json object соответствующий record
     * @param fieldName       имя поля в record
     * @param valueParse      функция парсинга по умолчанию
     * @param stack           Стек парсера
     */
    public record JsonToField(
        RecordComponent recordComponent,
        Ast.ObjectAst<?> objectAst,
        String fieldName,
        Fn3<Ast<?>, Type, ImList<ParseStack>, Result<Object, RecMapError>> valueParse,
        ImList<ParseStack> stack
    ) {
        public JsonToField fieldName(String newName) {
            if (newName == null) throw new IllegalArgumentException("newName==null");
            return new JsonToField(recordComponent, objectAst, newName, valueParse, stack);
        }
    }

    public static final Function<JsonToField, Result<Object, RecMapError>> DefaultFieldDeserialization = jsonToField -> {
        var fieldClass = jsonToField.recordComponent().getType();
        var fieldIsOptional = fieldClass == Optional.class;
        var fieldAstOpt = jsonToField.objectAst().get(jsonToField.fieldName());

        if ((fieldAstOpt.isEmpty() || fieldAstOpt.map(a -> a instanceof Ast.NullAst<?>).orElse(false))
            && fieldIsOptional
        ) {
            return Result.ok(Optional.empty());
        }

        if (fieldAstOpt.isEmpty()) {
            return Result.error(
                new RecMapParseError(
                    "expect field " + jsonToField.fieldName() + " in json",
                    jsonToField.stack
                )
            );
        }

        return jsonToField.valueParse().apply(
            fieldAstOpt.get(),
            jsonToField.recordComponent().getGenericType(),
            jsonToField.stack
        );
    };

    private Function<JsonToField, Result<Object, RecMapError>> fieldDeserialization = DefaultFieldDeserialization;

    public Function<JsonToField, Result<Object, RecMapError>> fieldDeserialization() {return fieldDeserialization;}

    public RecMapper fieldDeserialization(Function<JsonToField, Result<Object, RecMapError>> deserialization) {
        if (deserialization == null) throw new IllegalArgumentException("deserialization==null");
        fieldDeserialization = deserialization;
        return this;
    }

    private <T> T parseRecord(Ast.ObjectAst<?> objAst, Class<T> recordClass, ImList<ParseStack> stack) {
        var stack1 = stack.prepend(new ParseStack.parseRecord<T>(objAst, recordClass));

        var recComponents = recordClass.getRecordComponents();
        var recComponentClasses = new Class<?>[recComponents.length];
        var recValues = new Object[recComponents.length];

        for (var ri = 0; ri < recComponents.length; ri++) {
            var name = recComponents[ri].getName();

            var recClass = recComponents[ri].getType();
            recComponentClasses[ri] = recClass;

            var recType = recComponents[ri].getGenericType();

            var jsonToField = new JsonToField(
                recComponents[ri],
                objAst,
                name,
                (ast, type, stack2) -> {
                    try {
                        return Result.ok(parse(ast, recType, stack2));
                    } catch (RecMapError e) {
                        return Result.error(e);
                    }
                },
                stack1
            );

            var res = fieldDeserialization.apply(jsonToField);
            if (res.isError()) {
                //noinspection OptionalGetWithoutIsPresent
                throw res.getError().get();
            }

            recValues[ri] = res.unwrap();
        }

        try {
            var ctor = recordClass.getConstructor(recComponentClasses);
            return ctor.newInstance(recValues);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RecMapParseError("can't create instance of " + recordClass, e, stack);
        }
    }

    private <T> T parseEnum(Ast<?> ast, Class<T> enumCls, ImList<ParseStack> stack) {
        stack = stack.prepend(new ParseStack.parseEnum<T>(ast, enumCls));

        if (ast instanceof Ast.StringAst<?> strAst) {
            var enumConsts = enumCls.getEnumConstants();
            for (var enumConst : enumConsts) {
                if (strAst.value().equals(((Enum<?>) enumConst).name())) {
                    return enumConst;
                }
            }

            throw new RecMapParseError(
                "can't convert to enum (" + enumCls +
                    ") from \"" + strAst.value() +
                    "\", expect " +
                    Arrays.stream(enumConsts).map(
                        e -> ((Enum<?>) e).name()).reduce(
                        "", (sum, it) -> sum.isBlank() ? it : sum + ", " + it
                    ),
                stack
            );
        }

        throw new RecMapParseError("can't convert to enum (" + enumCls + ") from " + ast.getClass().getSimpleName(), stack);
    }
    //endregion
}
