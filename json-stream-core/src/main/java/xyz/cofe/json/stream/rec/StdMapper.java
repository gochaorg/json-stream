package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.coll.im.Result;
import xyz.cofe.coll.im.iter.Tree;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.rec.spi.StdMapperProvider;
import xyz.cofe.json.stream.token.DummyCharPointer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class StdMapper extends RecMapper {
    public StdMapper(){
        this(true);
    }

    public StdMapper(boolean withPlugins){
        if( withPlugins ) {
            for (StdMapperProvider provider : ServiceLoader.load(StdMapperProvider.class)) {
                provider.configure(this);
            }
        }
    }

    //////////////////

    public record CustomSerializer(
        Class<?> klass,
        boolean allowChildTypes,
        Function<Object, Optional<Ast<DummyCharPointer>>> serializer
    ) {}

    protected Map<Class<?>, CustomSerializer> serializers = new HashMap<>();
    protected Map<Class<?>, CustomSerializer> genericSerializers = new HashMap<>();

    {
        customObjectSerialize(this::stdCustomSerializer);
    }

    protected Set<Class<?>> defaultSerializer = new HashSet<>();

    public final Optional<Ast<DummyCharPointer>> stdCustomSerializer(Object someObj){
        if( someObj==null )return Optional.empty();

        var cls = someObj.getClass();
        if( defaultSerializer.contains(cls) )return Optional.empty();

        var prefectSer = findPrefectSerializerFor(cls);
        if(prefectSer.isEmpty()){
            defaultSerializer.add(cls);
            return Optional.empty();
        }

        var ser = prefectSer.get();
        return ser.serializer.apply(someObj);
    }

    protected Optional<CustomSerializer> findPrefectSerializerFor( Class<?> cls ){
        var ser = serializers.get(cls);
        if( ser!=null && !ser.allowChildTypes )return Optional.of(ser);

        ser = genericSerializers.get(cls);
        if( ser!=null )return Optional.of(ser);

        var parents = Tree.roots( (Class)cls ).follow( cFrom -> {
            ImList<Class> next = ImList.of();

            var itfs = cFrom.getInterfaces();
            if( itfs!=null && itfs.length>0 ){
                next = next.prepend( ImList.of(itfs) );
            }

            var supCls = cFrom.getSuperclass();
            if( supCls!=null && supCls!=Object.class ){
                next = next.prepend(supCls);
            }

            return next;
        });

        for( Class cls2 : parents.map(tp -> tp.node()) ){
            ser = genericSerializers.get(cls2);
            if( ser!=null ) {
                genericSerializers.put(cls, ser);
                return Optional.of(ser);
            }
        }

        return Optional.empty();
    }

    public class CustomSerialize<T> {
        public CustomSerialize(Class<T> cls){
            if( cls==null ) throw new IllegalArgumentException("cls==null");
            this.serializedClass = cls;
        }

        public final Class<T> serializedClass;

        protected boolean withSubTypes = false;

        public boolean withSubTypes(){ return withSubTypes; }

        public CustomSerialize<T> withSubTypes(boolean allowSubTypes){
            withSubTypes = allowSubTypes;
            return this;
        }

        public StdMapper append( Function<T,Optional<Ast<DummyCharPointer>>> serializer ){
            if( serializer==null ) throw new IllegalArgumentException("serializer==null");
            CustomSerializer cSer = new CustomSerializer(serializedClass, withSubTypes, obj -> {
                return serializer.apply((T)obj);
            });

            serializers.put(serializedClass, cSer);
            if( withSubTypes )genericSerializers.put(serializedClass,cSer);

            return StdMapper.this;
        }
    }

    public <T> CustomSerialize<T> serializerFor(Class<T> cls){
        if( cls==null ) throw new IllegalArgumentException("cls==null");
        return new CustomSerialize<>(cls);
    }

    //////////////////

    {
        fieldDeserialization(this::stdFieldDeserilizer);
    }

    protected Map<Class<?>, CustomDeserializer> deserializers = new HashMap<>();

    public record CustomDeserializer(
        Class<?> klass,
        BiFunction<Ast<?>, ImList<RecMapper.ParseStack>, Result<Object, RecMapError>> deserializer,
        Optional<Supplier<Object>> defaultValue
    ) {}

    public final Result<Object,RecMapError> stdFieldDeserilizer(JsonToField jsonToField){
        if( jsonToField==null )return Result.error(new RecMapError("illegal argument: jsonToField == null"));

        var dser = deserializers.get( jsonToField.recordComponent().getType() );
        if( dser!=null ){
            var fieldAstOpt = jsonToField.objectAst().get(jsonToField.fieldName());
            if( fieldAstOpt.isEmpty() ){
                return Result.from(
                    dser.defaultValue,
                    () -> new RecMapError("can't fetch default value for "+jsonToField)
                ).map(Supplier::get);
            }

            return dser.deserializer.apply(fieldAstOpt.get(), jsonToField.stack());
        }

        return DefaultFieldDeserialization.apply(jsonToField);
    }

    public <T> CustomDeserialize<T> deserializeFor(Class<T> cls){
        if( cls==null ) throw new IllegalArgumentException("cls==null");
        return new CustomDeserialize<>(cls);
    }

    public class CustomDeserialize<T> {
        public final Class<T> deserializedClass;

        public CustomDeserialize(Class<T> cls){
            if( cls==null ) throw new IllegalArgumentException("cls==null");
            deserializedClass = cls;
        }

        private Optional<Supplier<Object>> defaultValue;

        public CustomDeserialize<T> defaultValue( Optional<Supplier<Object>> defValue ){
            this.defaultValue = defValue;
            return this;
        }

        public StdMapper append(BiFunction<Ast<?>, ImList<RecMapper.ParseStack>, Result<T,RecMapError>> deserializer){
            //noinspection unchecked,rawtypes
            var dser = new CustomDeserializer(
                deserializedClass,
                (BiFunction)deserializer,
                defaultValue);

            deserializers.put(deserializedClass, dser);

            return StdMapper.this;
        }
    }
}
