package xyz.cofe.json.stream.rec;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.json.stream.ast.Ast;
import xyz.cofe.json.stream.ast.AstParser;
import xyz.cofe.json.stream.ast.AstWriter;
import xyz.cofe.json.stream.token.DummyCharPointer;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RecMapper {
    public Ast.StringAst<DummyCharPointer> toAst(String value) {
        return Ast.StringAst.create(value);
    }

    public Ast.StringAst<DummyCharPointer> toAst(char value){
        return Ast.StringAst.create(""+value);
    }

    public Ast.NumberAst.IntAst<DummyCharPointer> toAst(byte value) {
        return Ast.NumberAst.IntAst.create(0xFF & value);
    }

    public Ast.NumberAst.IntAst<DummyCharPointer> toAst(short value) {
        return Ast.NumberAst.IntAst.create(value);
    }

    public Ast.NumberAst.IntAst<DummyCharPointer> toAst(int value) {
        return Ast.NumberAst.IntAst.create(value);
    }

    public Ast.NumberAst.LongAst<DummyCharPointer> toAst(long value) {
        return Ast.NumberAst.LongAst.create(value);
    }

    public Ast.NumberAst.BigIntAst<DummyCharPointer> toAst(BigInteger value) {
        return Ast.NumberAst.BigIntAst.create(value);
    }

    public Ast.NumberAst.DoubleAst<DummyCharPointer> toAst(float value) {
        return Ast.NumberAst.DoubleAst.create(value);
    }

    public Ast.NumberAst.DoubleAst<DummyCharPointer> toAst(double value) {
        return Ast.NumberAst.DoubleAst.create(value);
    }

    public Ast.BooleanAst<DummyCharPointer> toAst(boolean value) {
        return Ast.NumberAst.BooleanAst.create(value);
    }

    public Ast.NullAst<DummyCharPointer> nullToAst() {
        return Ast.NumberAst.NullAst.create();
    }

    public Ast.IdentAst<DummyCharPointer> identToAst(String value) {
        return Ast.NumberAst.IdentAst.create(value);
    }

    public Ast<DummyCharPointer> toAst(Object record) {
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

    public String toJson(Object record) {
        if (record == null) throw new IllegalArgumentException("record==null");
        return AstWriter.toString(toAst(record));
    }

    protected Ast<DummyCharPointer> recordToAst(Object record, Class<?> cls) {
        var items = ImList.<Ast.KeyValue<DummyCharPointer>>of();
        for (var recCmpt : cls.getRecordComponents()) {
            var recName = recCmpt.getName();
            try {
                var recValue = recCmpt.getAccessor().invoke(record);
                if (recValue == null || (recValue instanceof Optional<?> optVal && optVal.isEmpty())) continue;

                var recJsonValue = toAst(recValue instanceof Optional<?> opt ? opt.get() : recValue);
                var recJsonName = toAst(recName);
                items = items.prepend(Ast.KeyValue.create(recJsonName, recJsonValue));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RecMapError("can't read record component " + recName, e);
            }
        }

        var body = Ast.ObjectAst.create(items.reverse());

        var itfs = Arrays.stream(cls.getInterfaces()).filter(Class::isSealed).toList();
        if (itfs.size() == 1) {
            var name = cls.getSimpleName();
            return Ast.ObjectAst.create(ImList.of(Ast.KeyValue.create(toAst(name), body)));
        } else {
            return body;
        }
    }

    protected Ast<DummyCharPointer> iterableToAst(Iterable<?> iterable) {
        ImList<Ast<DummyCharPointer>> lst = ImList.of();
        for (var it : iterable) {
            var a = toAst(it);
            lst = lst.prepend(a);
        }
        return Ast.ArrayAst.create(lst.reverse());
    }

    protected Ast<DummyCharPointer> arrayToAst(Object array) {
        ImList<Ast<DummyCharPointer>> lst = ImList.of();
        var arrLen = Array.getLength(array);
        for (var ai = 0; ai < arrLen; ai++) {
            lst = lst.prepend(
                toAst(Array.get(array, ai))
            );
        }
        return Ast.ArrayAst.create(lst.reverse());
    }

    protected Ast<DummyCharPointer> enumToAst(Enum<?> value) {
        return Ast.StringAst.create(value.name());
    }

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
    protected <T> Optional<Function<Ast<?>, T>> parserOf(Type type) {
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

    protected <T> ImList<T> imListParse(Ast.ArrayAst<?> ast, Function<Ast<?>, T> itemParse) {
        return ast.values().map(itemParse::apply);
    }

    protected <T> List<T> listParse(Ast.ArrayAst<?> ast, Function<Ast<?>, T> itemParse) {
        return ast.values().map(itemParse::apply).toList();
    }

    protected <T> Optional<T> optionalParse(Ast<?> ast, Function<Ast<?>, T> itemParse) {
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
        } else if (cls == Boolean.class) {
            if (ast instanceof Ast.BooleanAst.TrueAst<?>) {
                return (T) Boolean.TRUE;
            } else if (ast instanceof Ast.BooleanAst.FalseAst<?>) {
                return (T) Boolean.FALSE;
            } else {
                throw new RecMapError("expect boolean in json");
            }
        } else if (cls == Byte.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Byte) (byte) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Byte) ((Double) dblValue.value()).byteValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Byte) ((Long) lngValue.value()).byteValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Byte) bigValue.value().byteValue();
            throw new RecMapError("can't convert to short from " + ast.getClass().getSimpleName());
        } else if (cls == Short.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Short) (short) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Short) ((Double) dblValue.value()).shortValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Short) ((Long) lngValue.value()).shortValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Short) bigValue.value().shortValue();
            throw new RecMapError("can't convert to short from " + ast.getClass().getSimpleName());
        } else if (cls == Integer.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Integer) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue)
                return (T) (Integer) ((Double) dblValue.value()).intValue();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Integer) ((Long) lngValue.value()).intValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Integer) bigValue.value().intValue();
            throw new RecMapError("can't convert to int from " + ast.getClass().getSimpleName());
        } else if (cls == Long.class) {
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
        } else if (cls == Float.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Float) (float) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue) return (T) (Float) (float) dblValue.value();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Float) ((Long) lngValue.value()).floatValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Float) bigValue.value().floatValue();
            throw new RecMapError("can't convert to double from " + ast.getClass().getSimpleName());
        } else if (cls == Double.class) {
            if (ast instanceof Ast.NumberAst.IntAst<?> intValue) return (T) (Double) (double) intValue.value();
            if (ast instanceof Ast.NumberAst.DoubleAst<?> dblValue) return (T) (Double) dblValue.value();
            if (ast instanceof Ast.NumberAst.LongAst<?> lngValue)
                return (T) (Double) ((Long) lngValue.value()).doubleValue();
            if (ast instanceof Ast.NumberAst.BigIntAst<?> bigValue) return (T) (Double) bigValue.value().doubleValue();
            throw new RecMapError("can't convert to double from " + ast.getClass().getSimpleName());
        } else if (cls == Character.class) {
            if (ast instanceof Ast.StringAst<?> strValue) {
                if (strValue.value().isEmpty())
                    throw new RecMapError("can't convert to char empty string");

                return (T) (Character) strValue.value().charAt(0);
            }
            throw new RecMapError("can't convert to char from " + ast.getClass().getSimpleName());
        } else if (cls.isSealed() && cls.isInterface()) {
            return fromJsonToSealedInterface(ast, cls);
        }

        throw new RecMapError("unsupported " + cls);
    }

    @SuppressWarnings("unchecked")
    protected <T> T fromJsonToSealedInterface(Ast<?> ast, Class<T> cls) {
        if (ast instanceof Ast.ObjectAst<?> objAst) {
            var subClasses = cls.getPermittedSubclasses();
            for (var subCls : subClasses) {
                var bodyOpt = objAst.get(subCls.getSimpleName());
                if (bodyOpt.isPresent()) {
                    return (T) fromJsonBodyOfSubclass(bodyOpt.get(), subCls);
                }
            }
            throw new RecMapError("subClass not found, expect follow keys: " + Arrays.stream(subClasses).map(Class::getSimpleName).toList());
        } else {
            throw new RecMapError("expect Ast.ObjectAst");
        }
    }

    protected <T> T fromJsonBodyOfSubclass(Ast<?> ast, Class<T> subClass) {
        if (ast instanceof Ast.ObjectAst<?> objAst) {
            if (subClass.isRecord()) {
                return fromJsonBodyOfRecord(objAst, subClass);
            } else {
                throw new RecMapError("expect " + subClass + " is record");
            }
        } else {
            throw new RecMapError("expect Ast.ObjectAst");
        }
    }

    protected <T> T fromJsonBodyOfRecord(Ast.ObjectAst<?> objAst, Class<T> recordClass) {
        var recComponents = recordClass.getRecordComponents();
        var recComponentClasses = new Class<?>[recComponents.length];
        var recValues = new Object[recComponents.length];

        for (var ri = 0; ri < recComponents.length; ri++) {
            var name = recComponents[ri].getName();

            var recClass = recComponents[ri].getType();
            recComponentClasses[ri] = recClass;

            var recType = recComponents[ri].getGenericType();

            var valueAstOpt = objAst.get(name);
            if ((valueAstOpt.isEmpty() || valueAstOpt.map(a -> a instanceof Ast.NullAst<?>).orElse(false)) && recClass == Optional.class) {
                recValues[ri] = Optional.empty();
                continue;
            }

            if (valueAstOpt.isEmpty()) throw new RecMapError("expect field " + name + " in json");
            recValues[ri] = parse(valueAstOpt.get(), recType);
        }

        try {
            var ctor = recordClass.getConstructor(recComponentClasses);
            return ctor.newInstance(recValues);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RecMapError("can't create instance of " + recordClass, e);
        }
    }
}
