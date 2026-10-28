package ru.starfarm.mylittlefarm.i18n.item;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface JsonItem {

    String name();

    List<String> lore();

    String material();

    Optional<Integer> slot();

    Optional<Integer> data();

    Optional<Integer> page();

    Optional<Integer> price();

    /**
     * Получение слота для меню.
     *
     * @param slot дополнительное значение слота
     * @return переданный слот, если {@code null} - дефолтное значение из конфигурации
     */
    int menuSlot(@Nullable Integer slot);
}
