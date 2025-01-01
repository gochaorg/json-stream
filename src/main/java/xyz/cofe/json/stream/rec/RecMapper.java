package xyz.cofe.json.stream.rec;

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
    //region subClassWriter : SubClassWriter
    private SubClassWriter subClassWriter;

    {
        subClassWriter = SubClassWriter.defaultWriter;
    }

    /**
     * Указывает способ указания подтипа
     *
     * @return способ указания подтипа
     */
    @SuppressWarnings("UnusedReturnValue")
    public SubClassWriter subClassWriter() {
        return subClassWriter;
    }

    /**
     * Указывает способ указания подтипа
     *
     * @param subClassWriter способ указания подтипа
     * @return SELF ссылка
     */
    @SuppressWarnings("UnusedReturnValue")
    public RecMapper subClassWriter(SubClassWriter subClassWriter) {
        if (subClassWriter == null) throw new IllegalArgumentException("subClassWriter==null");
        this.subClassWriter = subClassWriter;
        return this;
    }
    //endregion

    //region subClassResolver : SubClassResolver
    private SubClassResolver subClassResolver;

    {
        subClassResolver = SubClassResolver.defaultResolver();
    }

    /**
     * Указывает способ получения имени/типа экземпляра
     *
     * @return резолвинг подтипа
     */
    @SuppressWarnings("UnusedReturnValue")
    public SubClassResolver subClassResolver() {
        return subClassResolver;
    }

    /**
     * Указывает способ получения имени/типа экземпляра
     *
     * @return резолвинг подтипа
     */
    @SuppressWarnings("UnusedReturnValue")
    public RecMapper subClassResolver(SubClassResolver subClassResolver) {
        if (subClassResolver == null) throw new IllegalArgumentException("subClassResolver==null");
        this.subClassResolver = subClassResolver;
        return this;
    }
    //endregion

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

    public String toJson(Object record, boolean pretty){
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
    }

    private Function<FieldToJson, Optional<Ast.KeyValue<DummyCharPointer>>> fieldSerialization
        = DefaultFieldSerialization;

    public Function<FieldToJson, Optional<Ast.KeyValue<DummyCharPointer>>> fieldSerialization(){
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

    @SuppressWarnings("unchecked")
    public <T> T parse(Ast<?> ast, Type type) {
        if (ast == null) throw new IllegalArgumentException("ast==null");
        if (type == null) throw new IllegalArgumentException("type==null");
        if (type instanceof Class<?> cls) {
            return (T) parse(ast, cls);
        }

        var parser = parserOf(type);
        if (parser.isPresent()) {
            return (T) parser.get().apply(ast);
        }

        throw new RecMapError("unsupported target type: " + type);
    }

    public <T> T parse(String json, Type type) {
        if (json == null) throw new IllegalArgumentException("json==null");
        if (type == null) throw new IllegalArgumentException("type==null");

        var jsnObj = AstParser.parse(json);
        return parse(jsnObj, type);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Optional<Function<Ast<?>, T>> parserOf(Type type) {
        if (type instanceof Class<?> cls) {
            return
                Optional.of((Ast<?> ast) -> (T) parse(ast, cls));
        }

        if (type instanceof ParameterizedType pt) {
            if (pt.getRawType() == ImList.class && pt.getActualTypeArguments().length == 1) {
                var itemParserOpt = parserOf(pt.getActualTypeArguments()[0]);
                if (itemParserOpt.isEmpty()) return Optional.empty();

                Function itemParser = itemParserOpt.get();
                Function parser = (ast) -> {
                    if (!(ast instanceof Ast.ArrayAst arr)) {
                        throw new RecMapError("expect json array (" + Ast.ArrayAst.class.getSimpleName() + "), actual: " + ast.getClass().getSimpleName());
                    }
                    return imListParse(arr, itemParser);
                };

                return Optional.of(parser);
            }

            if (pt.getRawType() == List.class && pt.getActualTypeArguments().length == 1) {
                var itemParserOpt = parserOf(pt.getActualTypeArguments()[0]);
                if (itemParserOpt.isEmpty()) return Optional.empty();

                Function itemParser = itemParserOpt.get();
                Function parser = (ast) -> {
                    if (!(ast instanceof Ast.ArrayAst arr)) {
                        throw new RecMapError("expect json array (" + Ast.ArrayAst.class.getSimpleName() + "), actual: " + ast.getClass().getSimpleName());
                    }
                    return listParse(arr, itemParser);
                };

                return Optional.of(parser);
            }

            if (pt.getRawType() == Optional.class && pt.getActualTypeArguments().length == 1) {
                var itemParserOpt = parserOf(pt.getActualTypeArguments()[0]);
                if (itemParserOpt.isEmpty()) return Optional.empty();

                Function itemParser = itemParserOpt.get();
                Function parser = (ast) -> {
                    if (!(ast instanceof Ast ast1)) {
                        throw new RecMapError("expect json value, actual: " + ast.getClass().getSimpleName());
                    }
                    return optionalParse(ast1, itemParser);
                };

                return Optional.of(parser);
            }
        }

        return Optional.empty();
    }

    private <T> ImList<T> imListParse(Ast.ArrayAst<?> ast, Function<Ast<?>, T> itemParse) {
        return ast.values().map(itemParse::apply);
    }

    private <T> List<T> listParse(Ast.ArrayAst<?> ast, Function<Ast<?>, T> itemParse) {
        return ast.values().map(itemParse::apply).toList();
    }

    private <T> Optional<T> optionalParse(Ast<?> ast, Function<Ast<?>, T> itemParse) {
        if (ast instanceof Ast.NullAst<?> nullAst) {
            return Optional.empty();
        }

        return Optional.of(itemParse.apply(ast));
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(Ast<?> ast, Class<T> cls) {
        if (ast == null) throw new IllegalArgumentException("ast==null");
        if (cls == null) throw new IllegalArgumentException("cls==null");

        if (ast instanceof Ast.NullAst<?>) return null;

        if (cls == String.class) {
            if (ast instanceof Ast.StringAst<?> str) {
                return (T) str.value();
            } else {
                throw new RecMapError("expect string in json");
            }
        } else if (cls == Boolean.class || cls == boolean.class) {
            if (ast instanceof Ast.BooleanAst.TrueAst<?>) {
                return (T) Boolean.TRUE;
            } else if (ast instanceof Ast.BooleanAst.FalseAst<?>) {
                return (T) Boolean.FALSE;
            } else {
                throw new RecMapError("expect boolean in json");
            }
        } else if (cls == Byte.class || cls == byte.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Byte) (byte) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Byte) ((Double) dblValue.value()).byteValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Byte) ((Long) lngValue.value()).byteValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Byte) bigValue.value().byteValue();
            throw new RecMapError("can't convert to short from " + ast.getClass().getSimpleName());
        } else if (cls == Short.class || cls == short.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Short) (short) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Short) ((Double) dblValue.value()).shortValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Short) ((Long) lngValue.value()).shortValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Short) bigValue.value().shortValue();
            throw new RecMapError("can't convert to short from " + ast.getClass().getSimpleName());
        } else if (cls == Integer.class || cls == int.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Integer) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Integer) ((Double) dblValue.value()).intValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Integer) ((Long) lngValue.value()).intValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Integer) bigValue.value().intValue();
            throw new RecMapError("can't convert to int from " + ast.getClass().getSimpleName());
        } else if (cls == Long.class || cls == long.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Long) (long) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Long) ((Double) dblValue.value()).longValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue) return (T) (Long) lngValue.value();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Long) bigValue.value().longValue();
            throw new RecMapError("can't convert to long from " + ast.getClass().getSimpleName());
        } else if (cls == BigInteger.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) BigInteger.valueOf(intValue.value());
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) BigInteger.valueOf(((long) dblValue.value()));
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue) return (T) BigInteger.valueOf(lngValue.value());
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) bigValue.value();
            throw new RecMapError("can't convert to BigInteger from " + ast.getClass().getSimpleName());
        } else if (cls == Float.class || cls == float.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Float) (float) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue) return (T) (Float) (float) dblValue.value();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Float) ((Long) lngValue.value()).floatValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Float) bigValue.value().floatValue();
            throw new RecMapError("can't convert to double from " + ast.getClass().getSimpleName());
        } else if (cls == Double.class || cls == double.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Double) (double) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue) return (T) (Double) dblValue.value();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Double) ((Long) lngValue.value()).doubleValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Double) bigValue.value().doubleValue();
            throw new RecMapError("can't convert to double from " + ast.getClass().getSimpleName());
        } else if (cls == Character.class || cls == char.class) {
            if (ast instanceof Ast.StringAst<?> strValue) {
                if (strValue.value().isEmpty())
                    throw new RecMapError("can't convert to char empty string");

                return (T) (Character) strValue.value().charAt(0);
            }
            throw new RecMapError("can't convert to char from " + ast.getClass().getSimpleName());
        } else if (cls.isSealed() && cls.isInterface()) {
            return parseSealedInterface(ast, cls);
        } else if ( cls.isRecord() && ast instanceof Ast.ObjectAst<?> objAst ){
            return parseRecord( objAst, cls );
        } else if (cls.isEnum()) {
            return parseEnum(ast, cls);
        }

        throw new RecMapError("unsupported " + cls);
    }

    @SuppressWarnings("unchecked")
    private <T> T parseSealedInterface(Ast<?> ast, Class<T> cls) {
        return subClassResolver.resolve(ast, cls.getPermittedSubclasses()).fold(
            resolved -> (T) parseSubclass(resolved.body(), resolved.klass()),
            err -> {
                throw new RecMapError(err);
            }
        );
    }

    private <T> T parseSubclass(Ast<?> ast, Class<T> subClass) {
        if (ast instanceof Ast.ObjectAst<?> objAst) {
            if (subClass.isRecord()) {
                return parseRecord(objAst, subClass);
            } else {
                throw new RecMapError("expect " + subClass + " is record");
            }
        } else {
            throw new RecMapError("expect Ast.ObjectAst");
        }
    }

    public record JsonToField(
        RecordComponent recordComponent,
        Ast.ObjectAst<?> objectAst,
        String fieldName,
        BiFunction<Ast<?>, Type, Result<Object, RecMapError>> valueParse
    ) {
        public JsonToField fieldName(String newName) {
            if (newName == null) throw new IllegalArgumentException("newName==null");
            return new JsonToField(recordComponent, objectAst, newName, valueParse);
        }
    }

    public static final Function<JsonToField, Result<Object, RecMapError>> DefaultFieldDeserialization = jsonToField -> {
        var fieldClass = jsonToField.recordComponent().getType();
        var fieldIsOptional = fieldClass == Optional.class;
        var fieldAstOpt = jsonToField.objectAst().get(jsonToField.fieldName());
        if ((fieldAstOpt.isEmpty() || fieldAstOpt.map(a -> a instanceof Ast.NullAst<?>).orElse(false)) && fieldIsOptional)
            return Result.ok(Optional.empty());

        if (fieldAstOpt.isEmpty())
            return Result.error(new RecMapError("expect field " + jsonToField.fieldName() + " in json"));
        return jsonToField.valueParse().apply(fieldAstOpt.get(), jsonToField.recordComponent().getGenericType());
    };

    private Function<JsonToField, Result<Object, RecMapError>> fieldDeserialization = DefaultFieldDeserialization;

    public Function<JsonToField, Result<Object, RecMapError>> fieldDeserialization() {return fieldDeserialization;}

    public RecMapper fieldDeserialization(Function<JsonToField, Result<Object, RecMapError>> deserialization) {
        if (deserialization == null) throw new IllegalArgumentException("deserialization==null");
        fieldDeserialization = deserialization;
        return this;
    }

    private <T> T parseRecord(Ast.ObjectAst<?> objAst, Class<T> recordClass) {
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
                (ast, type) -> {
                    try {
                        return Result.ok(parse(ast, recType));
                    } catch (RecMapError e) {
                        return Result.error(e);
                    }
                }
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
            throw new RecMapError("can't create instance of " + recordClass, e);
        }
    }

    private <T> T parseEnum(Ast<?> ast, Class<T> enumCls) {
        if (ast instanceof Ast.StringAst<?> strAst) {
            var enumConsts = enumCls.getEnumConstants();
            for (var enumConst : enumConsts) {
                if (strAst.value().equals(((Enum<?>) enumConst).name())) {
                    return enumConst;
                }
            }
            throw new RecMapError("can't convert to enum (" + enumCls + ") from \"" + strAst.value() + "\", expect " + Arrays.stream(enumConsts).map(e -> ((Enum<?>) e).name()).reduce("", (sum, it) -> sum.isBlank() ? it : sum + ", " + it));
        }
        throw new RecMapError("can't convert to enum (" + enumCls + ") from " + ast.getClass().getSimpleName());
    }
}
