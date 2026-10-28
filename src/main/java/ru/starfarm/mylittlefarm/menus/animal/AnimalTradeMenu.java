package ru.starfarm.mylittlefarm.menus.animal;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.AnimalType;
import ru.starfarm.mylittlefarm.enums.Product;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.player.info.AnimalData;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;

public class AnimalTradeMenu extends InventoryContainer {
    public String name;
    public AnimalType animal;

    public AnimalTradeMenu(String name, AnimalType animal) {
        super(ChatUtil.format("%s | Торговля", name), 4);
        this.name = name;
        this.animal = animal;
        this.addUpdater(20, (player, container) -> {
            drawInventory(player);
        });
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        Product product = animal.getProduct();

        int count = playerResolver.getProductCount(product);
        boolean has = count > 0;

        AnimalData data = playerResolver.getAnimalLog().get(animal);
        int seconds = (data != null) ? data.getSecondsLeft() : 0;
        String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);

        addItem(13, ApiManager.newItemBuilder(product.getMaterial())
                .name(ChatUtil.format("&b&l%s", product.getFormattedName(count)))
                .lore(
                        "&7Максимально соберите ресурсов",
                        "&7",
                        "&fДо получения: &e" + timeStr,
                        "&7",
                        "&7У вас в наличии: &e" + count + " шт."
                )
                .build()
        );

        double priceWithMultiplier = product.getMoney() * playerResolver.getGlobalMoneyMultiplier();

        addItem(20, ApiManager.newItemBuilder(!has ? Material.BARRIER : Material.EMERALD_BLOCK)
                .name((has ? "&a&l" : "&c&l") + "Продать одну штучку")
                .lore(!has
                                ? Collections.singletonList("&7Недостаточно продукта для продажи")
                                : Arrays.asList(
                                "&7Нажми для продажи одного предмета",
                                "",
                                "&7У вас в наличии: &e" + count + " шт.",
                                // 2. Отображаем цену с множителем
                                "&7Цена продажи: &a" + StringUtils.formatDouble(priceWithMultiplier) + " $"
                        )
                )
                .build(), (__, event) -> {
            if (has) {
                playerResolver.addBalance(priceWithMultiplier);
                playerResolver.removeProduct(product, 1);
                drawInventory(player);
            }
        });

        addItem(24, ApiManager.newItemBuilder(Material.CAKE)
                .name("&e&lПереработка")
                .lore("&7Нажми для открытия меню переработки")
                .build(), (__, event) -> new AnimalCraftingMenu(name, animal).openInventory(player));
    }

    @Override
    public void openInventory(Player player) {
        drawInventory(player);
        super.openInventory(player);
    }
}
