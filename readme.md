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

```xml
<dependency>
    <groupId>xyz.cofe</groupId>
    <artifactId>json-stream-core</artifactId>
    <version>версия</version>
</dependency>
```

Возможности
--------------------

- Лексический анализатор json
  - [Синтаксис](json-syntax.md) 
  - [unit test](json-stream-core/src/test/java/xyz/cofe/json/stream/token) 
- Парсинг/Генератор Json AST
  - [Синтаксис](json-syntax.md)
  - [Парсер](json-stream-core/src/test/java/xyz/cofe/json/stream/ast/AstParseTest.java)
  - [Генератор](json-stream-core/src/test/java/xyz/cofe/json/stream/ast/AstWriterTest.java)
- Итераторы по json stream
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
        - Отображение примитивов
        - Отображение enum
        - Отображение автономных (по иерархии) record
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