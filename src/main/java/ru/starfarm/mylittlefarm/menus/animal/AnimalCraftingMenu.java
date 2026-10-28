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

import java.util.Arrays;

public class AnimalCraftingMenu extends InventoryContainer {
    public String name;
    public AnimalType animal;

    public AnimalCraftingMenu(String name, AnimalType animal) {
        super(ChatUtil.format("%s | Переработка", name), 6);

        this.name = name;
        this.animal = animal;
        this.addUpdater(20, (player, container) -> {
            drawInventory(player);
        });
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        for (Product product : Product.valuesProducts()) {
            AnimalType producer = Arrays.stream(AnimalType.values())
                    .filter(a -> a.getProduct() == product)
                    .findFirst().orElse(null);

            String timeInfo = "&7Этот ресурс нельзя получить переработкой.";
            if (producer != null) {
                AnimalData data = playerResolver.getAnimalLog().get(producer);
                if (data != null && data.getCount() > 0) {
                    int sec = data.getSecondsLeft();
                    timeInfo = String.format("&fДо получения: &e%02d:%02d", sec / 60, sec % 60);
                } else {
                    timeInfo = "&cЖивотное не куплено";
                }
            }

            addItem(product.slot, ApiManager.newItemBuilder(product.material)
                    .name(ChatUtil.format("&b&l%s &f%sшт", product.name, playerResolver.getProductCount(product)))
                    .lore(
                            "&7",
                            timeInfo
                    )
                    .build()
            );
        }

        for (Product product : Product.valuesRecipes()) {
            var productInfo = playerResolver.getProductInfo(product);
            String status;
            int queueSize = (productInfo != null) ? productInfo.getQueue().size() : 0;

            if (queueSize > 0) {
                int timeLeft = productInfo.getQueue().values().stream().findFirst().orElse(0);
                status = String.format("&a&lГотовится: &e%02d:%02d &7(В очереди: &f%d&7)",
                        timeLeft / 60, timeLeft % 60, queueSize);
            } else {
                status = product.canCrafting(playerResolver) ? "&aМожно создать" : "&cНедостаточно ресурсов";
            }

            addItem(product.slot, ApiManager.newItemBuilder(product.material)
                    .name(ChatUtil.format("&b&l%s &f%sшт", product.name, playerResolver.getProductCount(product)))
                    .lore(
                            "&7",
                            status,
                            "&7",
                            "&eНажми, чтобы перейти в меню",
                            "&eуправления этим ресурсом"
                    )
                    .build(), (__, event) -> {
                new AnimalSellingMenu(name, product, animal).openInventory(player);
            });
        }
    }
}
