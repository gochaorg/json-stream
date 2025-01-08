package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.coll.im.iter.Tree;
import xyz.cofe.coll.im.iter.TreePath;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.rec.spi.StdMapperAdHocConfig;
import xyz.cofe.json.stream.rec.spi.StdMapperConfigure;
import xyz.cofe.json.stream.token.DummyCharPointer;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static xyz.cofe.coll.im.Result.error;
import static xyz.cofe.coll.im.Result.ok;

public class StdMapper extends RecMapper {
    protected final ImList<StdMapperAdHocConfig> adHocConfigs;

    public StdMapper() {
        this(true);
    }

    public StdMapper(boolean withPlugins) {
        if (withPlugins) {
            for (StdMapperConfigure provider : ServiceLoader.load(StdMapperConfigure.class)) {
                provider.configure(this);
            }
            adHocConfigs = ImList.from(ServiceLoader.load(StdMapperAdHocConfig.class));
        } else {
            adHocConfigs = ImList.of();
        }
    }

    //region ad hoc
    protected final Set<Class<?>> alreadyConfiguredClasses = new HashSet<>();
    protected final Set<Type> alreadyConfiguredTypes = new HashSet<>();

    protected void adHoc(Type type) {
        if (type == null || adHocConfigs.isEmpty()) return;
        if (type instanceof Class<?> cls) {
            adHoc(cls);
        } else {
            if (!alreadyConfiguredTypes.contains(type)) {
                alreadyConfiguredTypes.add(type);
                adHocConfigs.each(conf -> conf.configure(this, type));
            }
        }
    }

    protected void adHoc(Class<?> cls) {
        if (cls == null || adHocConfigs.isEmpty()) return;
        if (!alreadyConfiguredClasses.contains(cls)) {
            alreadyConfiguredClasses.add(cls);
            adHocConfigs.each(conf -> conf.configure(this, cls));
        }
    }

    @Override
    public Ast<DummyCharPointer> toAst(Object record) {
        if (record != null) {
            adHoc(record.getClass());
        }
        return super.toAst(record);
    }

    @Override
    protected <T> T parse(Ast<?> ast, Type type, ImList<ParseStack> stack) {
        adHoc(type);

        return super.parse(ast, type, stack);
    }

    @Override
    protected <T> Optional<BiFunction<Ast<?>, ImList<ParseStack>, T>> parserOf(Type type, ImList<ParseStack> stack) {
        adHoc(type);
        return super.parserOf(type, stack);
    }
    //endregion

    //region field (de)serialize

    //region field id

    /**
     * Получение id поля, для быстрого поиска в hashmap
     *
     * @param recordComponent поле
     * @return id поля
     */
    protected String fieldIdOf(RecordComponent recordComponent) {
        return fieldIdOf(
            recordComponent.getDeclaringRecord().getName(),
            recordComponent.getName()
        );
    }

    /**
     * Получение id поля, для быстрого поиска в hashmap
     *
     * @param classOwner record/class/interface
     * @param fieldName  поле
     * @return id поля
     */
    protected String fieldIdOf(String classOwner, String fieldName) {
        return classOwner + "/" + fieldName;
    }
    //endregion

    //region serialize
    private final Map<String, FieldWriteConfig> fieldsWriteConfig = new HashMap<>();

    public record FieldWriteConfig(
        ImList<Function<FieldToJson, Optional<FieldToJson>>> override
    ) {}

    @Override
    protected ImList<Ast.KeyValue<DummyCharPointer>> fieldSerialization(FieldToJson fieldToJson) {
        if (fieldToJson == null) throw new RecMapToAstError(new IllegalArgumentException("fieldToJson==null"));
        adHoc(fieldToJson.recordClass());
        adHoc(fieldToJson.recordComponent().getGenericType());

        var fId = fieldIdOf(fieldToJson.recordComponent());
        var fconf = fieldsWriteConfig.get(fId);
        if (fconf == null) return super.fieldSerialization(fieldToJson);

        for (var ovr : fconf.override()) {
            var f2jOpt = ovr.apply(fieldToJson);
            if (f2jOpt.isEmpty()) return ImList.of();

            fieldToJson = f2jOpt.get();
        }

        return super.fieldSerialization(fieldToJson);
    }

    public FieldSerialize1 fieldSerialize(Class<?> recordType, String fieldName) {
        if (recordType == null) throw new IllegalArgumentException("recordType==null");
        if (fieldName == null) throw new IllegalArgumentException("fieldName==null");
        return new FieldSerialize1(recordType.getName(), fieldName);
    }

    public class FieldSerialize1 {
        private final String classOwner;
        private final String fieldName;

        public FieldSerialize1(String classOwner, String fieldName) {
            if (classOwner == null) throw new IllegalArgumentException("classOwner==null");
            if (fieldName == null) throw new IllegalArgumentException("fieldName==null");
            this.classOwner = classOwner;
            this.fieldName = fieldName;
        }

        private ImList<Function<FieldToJson, Optional<FieldToJson>>> conf = ImList.of();

        public FieldSerialize1 filter(Predicate<FieldToJson> filter) {
            if (filter == null) throw new IllegalArgumentException("filter==null");
            conf = conf.prepend(f2j -> filter.test(f2j) ? Optional.of(f2j) : Optional.empty());
            return this;
        }

        public FieldSerialize1 skip() {
            conf = conf.prepend(f2j -> Optional.empty());
            return this;
        }

        public FieldSerialize1 rename(String name) {
            if (name == null) throw new IllegalArgumentException("name==null");
            conf = conf.prepend(f2j -> Optional.of(f2j.fieldName(name)));
            return this;
        }

        public FieldSerialize1 valueMapper(Function<Object, Ast<DummyCharPointer>> valueMapper) {
            if (valueMapper == null) throw new IllegalArgumentException("valueMapper==null");
            conf = conf.prepend(f2j -> Optional.of(f2j.valueMapper(valueMapper)));
            return this;
        }

        public FieldSerialize1 keyMapper(Function<String, Ast.Key<DummyCharPointer>> keyMapper) {
            if (keyMapper == null) throw new IllegalArgumentException("keyMapper==null");
            conf = conf.prepend(f2j -> Optional.of(f2j.keyMapper(keyMapper)));
            return this;
        }

        public StdMapper append() {
            fieldsWriteConfig.put(
                fieldIdOf(classOwner, fieldName),
                new FieldWriteConfig(conf.reverse())
            );
            return StdMapper.this;
        }
    }
    //endregion

    //region deSerialize
    public record ResolveField(
        Ast.ObjectAst<?> objectAst,
        RecordComponent field,
        ImList<ParseStack> stack
    ) {}

    public record DefaultFieldValue(
        Ast.ObjectAst<?> objectAst,
        RecordComponent field,
        Optional<RequiredFiled> requiredFiled,
        ImList<ParseStack> stack
    ) {}

    public record FieldReadConfig(
        Optional<Function<ResolveField, Result<? extends Ast<?>, RequiredFiled>>> resolveField,
        Optional<Function<DefaultFieldValue, Result<Object, RecMapParseError>>> defaultValue,
        Optional<CustomDeserializer> customDeserializer
    ) {
        public static FieldReadConfig empty() {
            return new FieldReadConfig(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
            );
        }

        public FieldReadConfig resolveField(Function<ResolveField, Result<? extends Ast<?>, RequiredFiled>> resolveField) {
            if (resolveField == null) throw new IllegalArgumentException("resolveField==null");
            return new FieldReadConfig(Optional.of(resolveField), defaultValue, customDeserializer);
        }

        public FieldReadConfig defaultValue(Function<DefaultFieldValue, Result<Object, RecMapParseError>> defaultValue) {
            if (defaultValue == null) throw new IllegalArgumentException("defaultValue==null");
            return new FieldReadConfig(resolveField, Optional.of(defaultValue), customDeserializer);
        }

        public FieldReadConfig customDeserializer(CustomDeserializer customDeserializer) {
            if (customDeserializer == null) throw new IllegalArgumentException("customDeserializer==null");
            return new FieldReadConfig(resolveField, defaultValue, Optional.of(customDeserializer));
        }
    }

    private final Map<String, FieldReadConfig> fieldsReadConfig = new HashMap<>();

    /**
     * Получение значения
     *
     * @param objectAst json объект
     * @param field     целевое поле
     * @param stack     стек парсинга
     * @return значение
     */
    @Override
    protected Result<? extends Ast<?>, RequiredFiled> resolveFieldOf(
        Ast.ObjectAst<?> objectAst, RecordComponent field, ImList<ParseStack> stack) {

        adHoc( field.getDeclaringRecord() );
        adHoc( field.getGenericType() );

        var fconf = fieldsReadConfig.get(fieldIdOf(field));
        if (fconf != null && fconf.resolveField.isPresent()) {
            return fconf.resolveField.get().apply(
                new ResolveField(
                    objectAst, field, stack
                )
            );
        }
        return super.resolveFieldOf(objectAst, field, stack);
    }

    /**
     * Получение значения по умолчанию, если таковое имеется
     *
     * @param objectAst     json объект
     * @param field         целевое поле
     * @param requiredFiled какое поле в json искалось
     * @param stack         стек парсинга
     * @return значение
     */
    @Override
    protected Result<Object, RecMapParseError> resolveOptionalField(
        Ast.ObjectAst<?> objectAst,
        RecordComponent field,
        Optional<RequiredFiled> requiredFiled,
        ImList<ParseStack> stack
    ) {

        var fconf = fieldsReadConfig.get(fieldIdOf(field));
        if (fconf != null && fconf.defaultValue().isPresent()) {
            return fconf.defaultValue().get().apply(
                new DefaultFieldValue(
                    objectAst, field, requiredFiled, stack
                )
            );
        }

        return super.resolveOptionalField(objectAst, field, requiredFiled, stack);
    }

    @Override
    protected Result<Object, RecMapParseError> fieldDeserialization(
        Ast<?> ast,
        RecordComponent field,
        ImList<ParseStack> stack
    ) {

        var fconf = fieldsReadConfig.get(fieldIdOf(field));
        if (fconf != null && fconf.customDeserializer().isPresent()) {
            return fconf.customDeserializer().get().deserializer().apply(ast, stack);
        }

        return super.fieldDeserialization(ast, field, stack);
    }

    public FieldDeserialize fieldDeserialize(Class<?> recordType, String fieldName) {
        if (recordType == null) throw new IllegalArgumentException("recordType==null");
        if (fieldName == null) throw new IllegalArgumentException("fieldName==null");

        return new FieldDeserialize(recordType.getName(), fieldName);
    }

    public class FieldDeserialize {
        private final String classOwner;
        private final String fieldName;

        public FieldDeserialize(String classOwner, String fieldName) {
            if (classOwner == null) throw new IllegalArgumentException("classOwner==null");
            if (fieldName == null) throw new IllegalArgumentException("fieldName==null");
            this.classOwner = classOwner;
            this.fieldName = fieldName;
        }

        private ImList<Function<FieldReadConfig, FieldReadConfig>> conf = ImList.of();

        public FieldDeserialize name(String firstName, String... secondNames) {
            if (firstName == null) throw new IllegalArgumentException("firstName==null");
            if (secondNames == null) throw new IllegalArgumentException("secondNames==null");

            ImList<String> names = ImList.of(secondNames).prepend(firstName);
            conf = conf.prepend(cfg -> cfg.resolveField(reslvField -> {
                var keys = reslvField.objectAst().values();
                var prefVal = keys.find(kv ->
                    names.contains(kv.key().value())
                ).map(Ast.KeyValue::value);

                return Result.from(
                    prefVal,
                    () -> new RequiredFiled(names)
                );
            }));

            return this;
        }

        public FieldDeserialize defaults(Function<DefaultFieldValue, Result<Object, RecMapParseError>> defValue){
            if( defValue==null ) throw new IllegalArgumentException("defValue==null");
            conf = conf.prepend( cfg -> cfg.defaultValue(defValue) );
            return this;
        }

        public FieldDeserialize defaults(Result<Object, RecMapParseError> defValue){
            if( defValue==null ) throw new IllegalArgumentException("defValue==null");
            conf = conf.prepend( cfg -> cfg.defaultValue( ignore -> defValue) );
            return this;
        }

        public FieldDeserialize defaultValue(Object defValue){
            if( defValue==null ) throw new IllegalArgumentException("defValue==null");
            conf = conf.prepend( cfg -> cfg.defaultValue( ignore -> ok(defValue)) );
            return this;
        }

        public FieldDeserialize deserialize( CustomDeserializer deSer ){
            if( deSer==null ) throw new IllegalArgumentException("deSer==null");
            conf = conf.prepend( cfg -> cfg.customDeserializer(deSer) );
            return this;
        }

        public FieldDeserialize deserialize( BiFunction<Ast<?>, ImList<RecMapper.ParseStack>, Result<Object, RecMapParseError>> deserializer ){
            if( deserializer==null ) throw new IllegalArgumentException("deserializer==null");
            conf = conf.prepend( cfg -> cfg.customDeserializer(new CustomDeserializer(deserializer)) );
            return this;
        }

        public FieldDeserialize parser( BiFunction<Ast<?>,ImList<RecMapper.ParseStack>, Object> parser ){
            return deserialize( (ast, stack) -> {
                try {
                    return ok( parser.apply(ast,stack) );
                } catch (Throwable err){
                    if( err instanceof RecMapParseError e ){
                        return error(e);
                    }
                    return error( new RecMapParseError(err, stack) );
                }
            });
        }

        public FieldDeserialize parser( Function<Ast<?>, Object> parser ){
            return deserialize( (ast, stack) -> {
                try {
                    return ok( parser.apply(ast) );
                } catch (Throwable err){
                    if( err instanceof RecMapParseError e ){
                        if( e.getParseStack().isEmpty() && stack.isNonEmpty() ){
                            return error(new RecMapParseError(e, stack));
                        }
                        return error(e);
                    }
                    return error( new RecMapParseError(err, stack) );
                }
            });
        }

        public StdMapper append() {
            var fId = fieldIdOf(classOwner, fieldName);
            var fconf = fieldsReadConfig.getOrDefault(
                fId,
                FieldReadConfig.empty()
            );

            for (var f : conf.reverse()) {
                fconf = f.apply(fconf);
            }

            fieldsReadConfig.put(
                fId,
                fconf
            );

            return StdMapper.this;
        }
    }
    //endregion

    //endregion

    //region custom serialize
    public record CustomSerializer(
        Class<?> klass,
        boolean allowChildTypes,
        Function<Object, Optional<Ast<DummyCharPointer>>> serializer
    ) {}

    protected Map<Class<?>, CustomSerializer> serializers = new HashMap<>();
    protected Map<Class<?>, CustomSerializer> genericSerializers = new HashMap<>();

    protected Set<Class<?>> defaultSerializer = new HashSet<>();

    @Override
    protected Optional<Ast<DummyCharPointer>> customObjectSerialize(Object someObj) {
        if (someObj == null) return Optional.empty();

        var cls = someObj.getClass();
        if (defaultSerializer.contains(cls)) return Optional.empty();

        var prefectSer = findPrefectSerializerFor(cls);
        if (prefectSer.isEmpty()) {
            defaultSerializer.add(cls);
            return Optional.empty();
        }

        var ser = prefectSer.get();
        return ser.serializer.apply(someObj);
    }

    protected Optional<CustomSerializer> findPrefectSerializerFor(Class<?> cls) {
        var ser = serializers.get(cls);
        if (ser != null && !ser.allowChildTypes) return Optional.of(ser);

        ser = genericSerializers.get(cls);
        if (ser != null) return Optional.of(ser);

        //noinspection rawtypes
        var parents = Tree.roots((Class) cls).follow(cFrom -> {
            //noinspection rawtypes
            ImList<Class> next = ImList.of();

            var itfs = cFrom.getInterfaces();

            //noinspection ConstantValue
            if (itfs != null && itfs.length > 0) {
                next = next.prepend(ImList.of(itfs));
            }

            var supCls = cFrom.getSuperclass();
            if (supCls != null && supCls != Object.class) {
                next = next.prepend(supCls);
            }

            return next;
        });

        //noinspection rawtypes
        for (Class cls2 : parents.map(TreePath::node)) {
            ser = genericSerializers.get(cls2);
            if (ser != null) {
                genericSerializers.put(cls, ser);
                return Optional.of(ser);
            }
        }

        return Optional.empty();
    }

    public class CustomSerialize<T> {
        public CustomSerialize(Class<T> cls) {
            if (cls == null) throw new IllegalArgumentException("cls==null");
            this.serializedClass = cls;
        }

        public final Class<T> serializedClass;

        protected boolean withSubTypes = false;

        @SuppressWarnings("unused")
        public boolean withSubTypes() {return withSubTypes;}

        @SuppressWarnings("unused")
        public CustomSerialize<T> withSubTypes(boolean allowSubTypes) {
            withSubTypes = allowSubTypes;
            return this;
        }

        @SuppressWarnings("unchecked")
        public StdMapper append(Function<T, Optional<Ast<DummyCharPointer>>> serializer) {
            if (serializer == null) throw new IllegalArgumentException("serializer==null");

            CustomSerializer cSer = new CustomSerializer(
                serializedClass, withSubTypes, obj -> serializer.apply((T) obj));

            serializers.put(serializedClass, cSer);
            if (withSubTypes) genericSerializers.put(serializedClass, cSer);

            return StdMapper.this;
        }
    }

    public <T> CustomSerialize<T> serializerFor(Class<T> cls) {
        if (cls == null) throw new IllegalArgumentException("cls==null");
        return new CustomSerialize<>(cls);
    }
    //endregion

    //region custom deserialize
    protected Map<Class<?>, CustomDeserializer> deserializers = new HashMap<>();

    @Override
    public <T> Result<T, RecMapParseError> tryParse(Ast<?> ast, Type type, ImList<ParseStack> stack) {
        if( ast!=null && type!=null ) {
            //noinspection SuspiciousMethodCalls
            var dser = deserializers.get(type);
            if( dser!=null )//noinspection unchecked
                return dser.deserializer.apply(ast,stack).map( v -> (T)v );
        }

        return super.tryParse(ast, type, stack);
    }

    @Override
    public <T> Result<T, RecMapParseError> tryParse(Ast<?> ast, Class<T> cls, ImList<ParseStack> stack) {
        if( ast!=null && cls!=null ) {
            var dser = deserializers.get(cls);
            if( dser!=null )//noinspection unchecked
                return dser.deserializer.apply(ast,stack).map( v -> (T)v );
        }

        return super.tryParse(ast, cls, stack);
    }

    public record CustomDeserializer(
        BiFunction<Ast<?>, ImList<RecMapper.ParseStack>, Result<Object, RecMapParseError>> deserializer
    ) {}

    public <T> CustomDeserialize<T> deserializeFor(Class<T> cls) {
        if (cls == null) throw new IllegalArgumentException("cls==null");
        return new CustomDeserialize<>(cls);
    }

    public class CustomDeserialize<T> {
        public final Class<T> deserializedClass;

        public CustomDeserialize(Class<T> cls) {
            if (cls == null) throw new IllegalArgumentException("cls==null");
            deserializedClass = cls;
        }

        public StdMapper append(BiFunction<Ast<?>, ImList<RecMapper.ParseStack>, Result<T, RecMapParseError>> deserializer) {
            //noinspection unchecked,rawtypes
            var dser = new CustomDeserializer(
                (BiFunction) deserializer
            );

            deserializers.put(deserializedClass, dser);

            return StdMapper.this;
        }
    }
    //endregion
}
