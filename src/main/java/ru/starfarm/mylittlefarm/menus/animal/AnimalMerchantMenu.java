package ru.starfarm.mylittlefarm.menus.animal;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.item.ItemUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.AnimalType;
import ru.starfarm.mylittlefarm.objects.animals.AnimalInfo;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.util.Collections;

public class AnimalMerchantMenu extends InventoryContainer {

    private final Table<AnimalType, Integer, AnimalInfo> animals;

    public AnimalMerchantMenu() {
        super("Торговец", 5);
        this.animals = MyLittleFarm.instance.getAnimalRegistry().getAnimals();
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        for (AnimalType type : AnimalType.values()) {
            int currentLevel = playerResolver.getAnimalLevel(type);

            addItem(type.getSlot(), ItemUtil.build(type.getItemStack(), itemBuilder -> {
                itemBuilder.setName(type.getName());

                if (currentLevel >= 5) {
                    itemBuilder.setLore(Collections.singletonList("§7Вы купили максимальное количество животных"));
                } else {
                    itemBuilder.setLore(
                            Lists.newArrayList(
                                    "§7Это уникальное животное, которое дает ресурсы",
                                    "§7Ресурсы можно перерабатывать, чтобы зарабатывать больше денег",
                                    "§7Количество животных: §e" + currentLevel + "/5",
                                    "",
                                    "§7Нажмите для взаимодействия."
                            ));
                }
            }), (container, event) -> {
                new AnimalMainMenu("Торговец", type).openInventory(player);
            });
        }
    }
}
