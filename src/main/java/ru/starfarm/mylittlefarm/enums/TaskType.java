package ru.starfarm.mylittlefarm.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.util.item.ItemUtil;

import java.util.function.BiFunction;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum TaskType {

    BREAK_GRASS((currentProgress, neededProgress) -> ItemUtil.build(Material.GRASS, itemBuilder -> {
        itemBuilder.setName(currentProgress + " / " + neededProgress);
    })),
    BREAK_HAYS((currentProgress, neededProgress) -> ItemUtil.build(Material.HAY_BLOCK, itemBuilder -> {
        itemBuilder.setName(currentProgress + " / " + neededProgress);
    })),
    ;

    BiFunction<Long, Long, ItemStack> itemStack;
}
