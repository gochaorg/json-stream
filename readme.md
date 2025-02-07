Библиотека для работы с json в рамках java - record
===================================================

Мотивация
--------------

Текущая реализация популярных библиотек java - json (jackson, gson) имееют свои особенности

- не работают с иммутабельными типами java (sealed interfaces + record)
  - [issues/61](https://github.com/FasterXML/jackson-future-ideas/issues/61) (все еще открыто на дату 2025-01-09)
  - есть [стороння поддержка](https://github.com/sigpwned/jackson-modules-java-17-sealed-classes)
    - Требует проверки работы с иммутабельными типами
- [либо требуют излишнего написания кода](https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization)
  ([но не факт, что сразу заработает](https://github.com/FasterXML/jackson-future-ideas/issues/61))

В частности java 17+ умеет создавать закрытые иммутабельные типы, с развитой иерархией.

Например

```java
sealed interface Node {
    record NodeA() implements Node {}
    record NodeB( String a ) implements Node {}
    record NodeC( String a, Optional<Node> b ) implements Node {}
}
```

Данная структура является самодостаточной и JVM предоставляет всю необходимую информацию (через рефлекцию)

Быстрый старт
-------------

```java
// Пример из unit test
public class StdMapperTest {
    
    // Есть такая иерархия типов
    public record NodeA() implements Node {
    }

    public record NodeB(String a) implements Node {
    }

    public record NodeC(int b, Node c) implements Node {
    }

    public record NodeD(int b, Node c, Node d) implements Node {
    }

    public record NodeE(ImList<Node> nodes) implements Node {
    }

    public record NodeF(Optional<Node> node) implements Node {
    }
    
    // Пример использования
    @Test
    public void nodeSample(){
        // Данный класс умеет преобразовывать record (и не только) в json и обратно
        StdMapper mapper = new StdMapper();

        // Преобразование record в json
        var ast = mapper.toAst(new NodeB("abc"));
        
        var json = AstWriter.toString(ast, true);
        System.out.println(json);
        
        // будет такой json
        // {
        //     "NodeB": {
        //         "a": "abc"
        //     }
        // }

        var node = mapper.parse(ast, Node.class);
        System.out.println(node);
        
        // выведет: NodeB[a=abc]

        //////////

        ast = mapper.toAst(
            new NodeE(
                ImList.of(
                    new NodeB("abc"),
                    new NodeB("def")
                )
            )
        );
        
        json = AstWriter.toString(ast, true);
        System.out.println(json);
        
        // выведет
        // {
        //     "NodeE": {
        //         "nodes": [
        //             {
        //                 "NodeB": {
        //                     "a": "abc"
        //                 }
        //             },
        //             {
        //                 "NodeB": {
        //                     "a": "def"
        //                 }
        //             }
        //         ]
        //     }
        // }

        assertTrue(json.contains("abc"));
        assertTrue(json.contains("def"));

        node = mapper.parse(ast, Node.class);
        System.out.println(node);

        ///////////

        ast = mapper.toAst(
            new NodeF(Optional.empty())
        );

        json = AstWriter.toString(ast, true);
        System.out.println(json);
        
        // выведет
        // {
        //     "NodeF": {}
        // }

        node = mapper.parse(ast, Node.class);
        System.out.println(node);

    }
}
```

Подключить пакет maven
-------------

**Основное ядро**

```xml
<dependency>
    <groupId>xyz.cofe</groupId>
    <artifactId>json-stream-core</artifactId>
    <version>версия</version>
</dependency>
```

**Поддержка аннотаций**

```xml
<dependency>
    <groupId>xyz.cofe</groupId>
    <artifactId>json-stream-ann</artifactId>
    <version>версия</version>
</dependency>
```

**Поддержка java/jre типов (даты,url,...)**

```xml
<dependency>
    <groupId>xyz.cofe</groupId>
    <artifactId>json-jre-types</artifactId>
    <version>версия</version>
</dependency>
```

Возможности
=============================

- Лексический анализатор json
  - [Синтаксис](json-syntax.md) 
  - [unit test](json-stream-core/src/test/java/xyz/cofe/json/stream/token) 
- Парсинг/Генератор Json AST
  - [Синтаксис](json-syntax.md)
  - [Парсер](json-stream-core/src/test/java/xyz/cofe/json/stream/ast/AstParseTest.java)
  - [Генератор](json-stream-core/src/test/java/xyz/cofe/json/stream/ast/AstWriterTest.java)
- [Итераторы по json stream](#итераторы-по-json-stream)
  - Бесконечные (+ ленивые) и конечные итераторы json объектов
  - Бесконечные итераторы json
    - Создание итератора
    - Итерирование
    - Отображение flatMap / map
      - Фильтрация
      - Извлечение вложенных объектов из объектов top-level(по key/value) в json
        - Извлечение по ключу
      - Извлечение вложенных объектов
    - Ограничение выборки
    - Пропуск
    - Конкатенация append / prepend
  - Конечные итераторы
    - Подсчет кол-ва
    - Выборка конечных иммутабльных список json примитивов
    - Разворот порядка следования
    - Сортировка
    - Свертка fold
- Отображение java - record
    - Базовая часть (RecMapper)
        - [Отображение примитивов](https://github.com/gochaorg/json-stream/blob/6228d379d3003178f706de2fb856f25480652a26/json-stream-core/src/test/java/xyz/cofe/json/stream/rec/RecMapTest.java#L137)
        - [Отображение enum](https://github.com/gochaorg/json-stream/blob/6228d379d3003178f706de2fb856f25480652a26/json-stream-core/src/test/java/xyz/cofe/json/stream/rec/RecMapTest.java#L194)
        - Отображение [автономных](#автономные-record) (по иерархии) record
        - Отображение списков с автономными record
        - Отображение иерархических типов (sealed)
        - Опциональные типы (Optional<T>)
        - Иммутальтные списки (ImList<T>)
    - Конфигурация RecMapper
        - Способ указания под-типа
        - Пропуск сериализации полей
        - Значения по умолчанию
        - Переименование полей
        - Переопределение механизма сериализации типов (custom (de)serialize)
    - Конфигурация StdMapper 
      - Использование механизма расширения ServiceLoader (plugin)
        - Конфигурация для StdMapper
        - Конфигурация для StdMapper ad hoc
      - Переопределение механизма сериализации типов (custom (de)serialize)
      - Конфигурация полей
        - Переопределение механизма сериализации для поля 
        - Переименование полей
        - Опциональное значение
    - Расширения
      - Поддержка аннотаций для полей
      - Типы JRE
        - Массивы байтов
        - Кодировки
        - Дата/Время
        - Имена файлов
        - Математические типы (BigDecimal / BigInteger)
        - Ссылки (URI,URL,InetAddress)
        - Регулярные выражения

-----------------

Итераторы по json stream
---------------------------

Одна из базовых возможной (java-stream-core) - работать с бесконечными источниками json.

Итераторы - это объекты реализующие `Iterable<Ast>` - т.е. итерирование по json, 
в данной реализации добавлены дополнительные функции связанные с такими списками объектов.

Итераторы делятся на два класса:

- QuerySet - общий итератор, с типичными методами.
  - QuerySetInf - итератор по бесконечным спискам, не содержит спец методов, кроме унаследованных.
  - QuerySetFin - итератор по конечным спискам. Дополняет спец методы, как сортировка, разворот, ...

Итераторы являются "ленивыми" - т.е. они не хранят данные в памяти, а будут извлечены из источника, по мере чтения.

### Создание итератора

```java
import xyz.cofe.json.stream.query.*;
import xyz.cofe.json.stream.query.ast.*;
    
var qs1 = new QuerySetFin<>(
    AstParser.parse("""
        { "a": 1  // Комментарии тоже допускаются
          // Можно использовать простые идентификаторы,
          // в качестве ключей (полей объекта), а не только строки
        , b: [ 1, 2 ]
        }
    """)
);

// либо

var qs2 = QuerySetFin.fromJson(
    """
    { a: 1
    , b: [ 1, 2 ]
    }
    """
);
```

Выполнение `qs1.toString()` вернет `[{a:1,b:[1,2]}]` - т.к. был передан по сути один объект.

### Объединение итераторов

```java
var qs1 = QuerySetFin.fromJson( "{ a: 1 } " );
var qs2 = QuerySetFin.fromJson( "{ b: [ 1, 2 ] }");
var qs3 = qs1.append( qs2 );
```

В результате qs3 будет содержать

```json5
[ {a: 1}, {b: [1,2]} ]
```

### Фильтрация объектов в json итераторе

```java
qs = qs.filter( ast -> ast instanceof Ast.Number );
```

- `filter( predicate )` - Создает новый итератор, который:
  - Будет применять predicate к json данным
  - Если predicate вернет false, то в результирующую выборку данное значение не попадет
  - predicate будет вызван, только когда будет попытка чтения/итерации
  - predicate - это лямбда вида: `fn( ast: Ast ) : boolean`

### Извлечение значений из Json object

```java
var qs = QuerySetFin.fromJson(
    """
    { a: 1
    , b: [ 1, 2 ]
    , b: "x"
    }
    """
);

qs = qs.get("b");
```

В результате будет:

```json5
[ [1,2]
, "x"
]
```

Автономные record
-------------

Библиотека по умолчанию различает record на
- Те, что в родительских типах - интерфейсах имеют маркер sealed
  - При сохранении указывается тип 
- И автономные
  - При сохранении тип не указывается 

Пример автономных record

```java
record Node() {}
```

Пример не автономных

```java
sealed interface BaseType {
    record NodeA() implements BaseType {}
    record NodeB() implements BaseType {}
} 
```

В данном случае, NodeA - не является автономный, по скольку BaseType - является sealed.

Это различие влияет на способ сохранения/восстановления json

### Автономный

```
mapper.toJson( new Node() )
```

Будет сгенерирован такой json

```json
{}
```

### Не автономный

```
mapper.toJson( new NodeA() )
```

Будет сгенерирован такой json

```json
{
  "NodeA": {}
}
```
