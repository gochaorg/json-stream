package xyz.cofe.json.stream.token;

import java.util.Optional;

/**
 * Указатель на исходник
 * @param <C> Тип элемента в исходнике (символ или лексема)
 * @param <SELF>
 */
public sealed interface SourcePointer<C, SELF extends SourcePointer<C,SELF>> permits CharPointer {
    /**
     * Получение символа/лексемы относительно указателя
     * @param offset смещение относительно указателя
     * @return значение
     */
    Optional<C> get(int offset);

    /**
     * Клонирование и смещение указателя относительно текущий позиции
     * @param offset смещение
     * @return новый указатель
     */
    SELF move(int offset);

    /**
     * Получение разницы между текущий и указанным указателем
     * @param other другой указатель
     * @return 0 - тот же самый указатель
     */
    int subtract(SELF other);
}
