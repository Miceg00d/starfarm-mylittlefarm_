package ru.starfarm.mylittlefarm.menus.animal;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.AnimalType;
import ru.starfarm.mylittlefarm.objects.animals.AnimalInfo;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnimalMainMenu extends InventoryContainer {
    public String name;
    public AnimalType animal;

    public AnimalMainMenu(String name, AnimalType animal) {
        super(name, 5);

        this.name = name;
        this.animal = animal;
    }

    public List<Integer> buySlots = Arrays.asList(10, 11, 12, 19, 20, 21, 28, 29, 30), tradeSlots = Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34);

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        int currentLevel = playerResolver.getAnimalLevel(animal);

        AnimalInfo info = MyLittleFarm.getInstance().getAnimalRegistry().getAnimal(animal, currentLevel + 1);
        boolean isMax = currentLevel >= 5;
        boolean hasMoney = info != null && playerResolver.getBalance() >= info.getPrice();

        ItemStack buy = ApiManager.newItemBuilder(isMax ? Material.BARRIER : (hasMoney ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK))
                .name(isMax ? "&2&lЛИМИТ" : (hasMoney ? "&a&lКупить животное" : "&c&lНедостаточно средств"))
                .lore(isMax ?
                        Collections.singletonList("&7Вы купили максимум животных (5/5)") :
                        Arrays.asList(
                                "&7Количество животных: &e" + currentLevel + "/5",
                                "&7Цена покупки: &a" + (info != null ? StringUtils.formatDouble(info.getPrice()) : "0") + " $"
                        )
                )
                .build();

        buySlots.forEach(slot -> addItem(slot, buy, (__, event) -> {
            if (!isMax && hasMoney) {
                playerResolver.buyAnimal(animal);
                new AnimalMainMenu(name, animal).openInventory(player);
            }
        }));

        ItemStack trade = ApiManager.newItemBuilder(Material.GOLD_BLOCK)
                .name("&6&lТорговля")
                .lore(currentLevel < 1 ? "&cНужно купить хотя бы 1 животное" : "&7Нажми для открытия меню торговли")
                .build();

        tradeSlots.forEach(slot -> addItem(slot, trade, (__, event) -> {
            if (currentLevel >= 1) {
                new AnimalTradeMenu(name, animal).openInventory(player);
            }
        }));
    }

}
