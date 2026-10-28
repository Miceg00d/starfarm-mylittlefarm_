package ru.starfarm.mylittlefarm.menus.animal;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.number.NumberUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.AnimalType;
import ru.starfarm.mylittlefarm.enums.Product;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnimalSellingMenu extends InventoryContainer {
    public String name;
    public Product product;
    public AnimalType animal;

    public AnimalSellingMenu(String name, Product product, AnimalType animal) {
        super(ChatUtil.format("%s | Переработка", name), 5);

        this.name = name;
        this.product = product;
        this.animal = animal;
        this.addUpdater(20, (player, container) -> drawInventory(player));
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        int amount = playerResolver.getProductCount(product);

        if (amount > 0) {
            addItem(11, ApiManager.newItemBuilder(Material.SIGN)
                    .name("&a&lПродать 1 шт.")
                    .lore(
                            "&7Нажми для продажи предмета",
                            "&7Цена: &a" + StringUtils.formatDouble(product.getMoney() * playerResolver.getGlobalMoneyMultiplier()) + " $",
                            "",
                            "&7У вас имеется: &e" + amount + " шт."
                    )
                    .build(), (__, event) -> {
                if (playerResolver.getProductCount(product) > 0) {
                    playerResolver.removeProduct(product, 1);
                    double finalPrice = product.getMoney() * playerResolver.getGlobalMoneyMultiplier();
                    playerResolver.addBalance(finalPrice);

                    ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &aВы успешно продали &e" + product.name.toLowerCase() + "&a!");

                    drawInventory(player);
                }
            });
        } else {
            addItem(11, new org.bukkit.inventory.ItemStack(Material.AIR));
            getInventory().setItem(11, null);
        }

        var productInfo = playerResolver.getProductInfo(product);
        int queueSize = (productInfo != null && productInfo.queue != null) ? productInfo.queue.size() : 0;

        String craftStatus;
        if (queueSize > 0) {
            int firstId = productInfo.queue.keySet().iterator().next();
            int timeLeft = productInfo.queue.get(firstId);
            craftStatus = String.format("&a&lГотовится: &e%02d:%02d &7(В очереди: &f%d шт.&7)",
                    timeLeft / 60, timeLeft % 60, queueSize);
        } else {
            craftStatus = product.canCrafting(playerResolver) ? "&aНажми для создания" : "&cУ вас не хватает ресурсов";
        }

        Material icon = (queueSize > 0) ? Material.WATCH : Material.ANVIL;

        addItem(13, ApiManager.newItemBuilder(icon)
                .name("&e&lСоздать " + product.name)
                .lore(
                        "&7Время крафта: &f" + NumberUtil.getTime(product.getRecipe().getTime()),
                        "",
                        craftStatus
                )
                .amount(queueSize > 1 ? Math.min(queueSize, 64) : 1)
                .build(), (__, event) -> {

            if (product.canCrafting(playerResolver)) {
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &aНачато создание продукта: &e%s&a.", product.name.toLowerCase());

                product.getRecipe().getIngredients().forEach(playerResolver::removeProduct);
                playerResolver.getProductInfo(product).addQueue(product.getRecipe().getTime());
                drawInventory(player);
            } else {
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &cУ вас не хватает ресурсов для создания &e%s&c.", product.name.toLowerCase());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        });

        addItem(36, ApiManager.newItemBuilder(Material.PAPER)
                .name("&aНазад")
                .build(), (__, event) -> new AnimalCraftingMenu(name, animal).openInventory(player)
        );

        int size = product.getRecipe().getIngredients().size();
        List<Integer> slots = size == 1 ? Collections.singletonList(31) : size == 2 ? Arrays.asList(30, 32) : Arrays.asList(29, 31, 33);

        AtomicInteger slotIdx = new AtomicInteger(0);
        product.getRecipe().getIngredients().forEach((ingredient, count) -> {
            int playerHas = playerResolver.getProductCount(ingredient);
            addItem(slots.get(slotIdx.getAndIncrement()), ApiManager.newItemBuilder(ingredient.material)
                    .name("&b&l" + ingredient.name)
                    .lore(
                            "&7Требуется: &f" + count + " шт.",
                            "&7У вас имеется: " + (playerHas >= count ? "&a" : "&c") + playerHas + " шт."
                    )
                    .amount(count)
                    .build()
            );
        });
    }
}
