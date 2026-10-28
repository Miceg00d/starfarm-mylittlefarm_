package ru.starfarm.mylittlefarm.menus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.number.NumberUtil;
import ru.starfarm.core.util.number.PercentUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.EventType;
import ru.starfarm.mylittlefarm.objects.cultures.CultureInfo;
import ru.starfarm.mylittlefarm.objects.modifiers.Modifiers;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BookMenu extends InventoryContainer {

    public BookMenu() {
        super("Бухгалтерия", 6);
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        // Получаем резолвер через твой GamePlayer
        val playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        // Защита от Null: если данные еще не загружены асинхронно, не открываем меню
        if (playerResolver == null) {
            player.closeInventory();
            ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &cВаши данные еще загружаются. Подождите пару секунд...");
            return;
        }

        updateItems(playerResolver);

        // Используем Collectors.toList(), чтобы IDE на Java 8 не выдавала ошибку
        List<CultureType> price = Arrays.stream(CultureType.values())
                .filter(culture -> culture != CultureType.FOUNTAIN && culture != CultureType.OTHER)
                .filter(culture -> playerResolver.getCultureLevel(culture) < 100)
                .sorted(Comparator.comparingDouble(culture -> {
                    int next = playerResolver.getCultureLevel(culture) + 1;
                    CultureInfo info = MyLittleFarm.getInstance().getCultureRegistry().getCulture(culture, next);

                    double cultPrice = info.price();
                    double income = Math.max(info.baseIncome(), 0.1);

                    double playerIncome = playerResolver.getIncome() + PercentUtil.getNumberByPercent(Modifiers.getModifier(), playerResolver.getIncome());
                    // Защита от деления на 0, если у игрока еще нет дохода
                    playerIncome = Math.max(playerIncome, 0.01);

                    double incPrice = cultPrice / income;
                    double buyTime = cultPrice / playerIncome;

                    return incPrice + buyTime;
                }))
                .collect(Collectors.toList());

        // Отрисовка рекомендаций (безопасно работаем со списком через размер)
        drawRecommendation(price, 0, 48, "самой выгодной");
        drawRecommendation(price, 1, 49, "довольно выгодной");
        drawRecommendation(price, 2, 50, "немного выгодной");

        // Обновление инвентаря раз в секунду
        addUpdater(20, (p, __) -> {
            val resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(p);
            if (resolver != null) {
                updateItems(resolver);
            }
        });
    }

    private void drawRecommendation(List<CultureType> priceList, int index, int slot, String benefitText) {
        if (priceList.size() <= index) {
            addItem(slot, ApiManager.newItemBuilder(Material.BARRIER)
                    .name(ChatUtil.format("&cМы не знаем что можно вам предложить :("))
                    .build()
            );
        } else {
            CultureType type = priceList.get(index);
            List<MaterialInfo> materials = getMaterial();

            // Проверка, чтобы не выйти за пределы списка материалов
            if (type.ordinal() < materials.size()) {
                MaterialInfo mat = materials.get(type.ordinal());
                addItem(slot, ApiManager.newItemBuilder(mat.getMaterial(), (short) mat.getData())
                        .name(ChatUtil.format("&b&l%s", type.getName()))
                        .lore(
                                "§7По нашему мнению, покупка",
                                "§7именно этой культуры будет",
                                "§7" + benefitText + " для вас",
                                "§7на данный момент§8."
                        )
                        .build()
                );
            }
        }
    }

    void updateItems(PlayerResolver playerResolver) {
        if (playerResolver == null) return;

        Arrays.stream(CultureType.values())
                .filter(CultureType::isPlant)
                .forEach(cultureType -> {
                    List<MaterialInfo> materials = getMaterial();
                    if (cultureType.ordinal() >= materials.size()) return;

                    val itemInfo = materials.get(cultureType.ordinal());
                    val itemBuilder = ApiManager.newItemBuilder(itemInfo.getMaterial(), (short) itemInfo.getData());

                    itemBuilder.setName(ChatUtil.format("&b&l%s", cultureType.getName()));
                    itemBuilder.addLore(ChatUtil.format("&7Куплено: &e%s &7шт.", playerResolver.getCultureLevel(cultureType)));

                    if (playerResolver.getCultureLevel(cultureType) > 0) {
                        itemBuilder.addLore(ChatUtil.format("&7Производит: &a%s $/сек", StringUtils.formatDouble(playerResolver.getCultureIncome(cultureType).income())));
                    }

                    if (playerResolver.getCultureLevel(cultureType) < 100) {
                        double nextPrice = MyLittleFarm.getInstance().getCultureRegistry().getCulture(cultureType, playerResolver.getCultureLevel(cultureType) + 1).price();
                        itemBuilder.addLore(ChatUtil.format(playerResolver.getCultureLevel(cultureType) > 0 ? "&7Стоимость следующего: %s%s $" : "&7Купить за: %s%s $", (playerResolver.getBalance() < nextPrice ? "§c" : "§a"), StringUtils.formatDouble(nextPrice)));

                        if (playerResolver.getBalance() < nextPrice && playerResolver.getCultureLevel(cultureType) > 0) {
                            double totalIncome = playerResolver.getIncome() + PercentUtil.getNumberByPercent(Modifiers.getModifier(), playerResolver.getIncome());
                            if (totalIncome > 0) {
                                int seconds = (int) ((nextPrice - playerResolver.getBalance()) / totalIncome);
                                itemBuilder.addLore(ChatUtil.format("&7Не хватает: &c%s", StringUtils.formatDouble(nextPrice - playerResolver.getBalance())));
                                itemBuilder.addLore(ChatUtil.format("&7До следующего: &e%s", NumberUtil.getTime(seconds)));
                            }
                        }
                    }

                    addItem(cultureType.getId(), itemBuilder.build(), (baseInventory, event) -> {
                        val clickedPlayer = (Player) event.getWhoClicked();
                        val resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(clickedPlayer);

                        if (resolver == null) return;

                        if (resolver.hasActiveEvent(EventType.DIRTY)) {
                            ChatUtil.sendMessage(clickedPlayer, "&fО-ге-ге! Сначала помойся, потом живность покупай!");
                            clickedPlayer.closeInventory();
                            return;
                        }
                        resolver.buyCulture(cultureType, false);
                        updateInventory(clickedPlayer);
                    });
                });
    }

    public List<MaterialInfo> getMaterial() {
        return Arrays.asList(
                new MaterialInfo(Material.WHEAT, 0),
                new MaterialInfo(Material.POTATO_ITEM, 0),
                new MaterialInfo(Material.CARROT_ITEM, 0),
                new MaterialInfo(Material.SUGAR_CANE, 0),
                new MaterialInfo(Material.PUMPKIN, 0),
                new MaterialInfo(Material.MELON_BLOCK, 0),
                new MaterialInfo(Material.RED_MUSHROOM, 0),
                new MaterialInfo(Material.BROWN_MUSHROOM, 0),
                new MaterialInfo(Material.RED_ROSE, 0),
                new MaterialInfo(Material.RED_ROSE, 6),
                new MaterialInfo(Material.DEAD_BUSH, 0),
                new MaterialInfo(Material.NETHER_STALK, 0),
                new MaterialInfo(Material.CHORUS_FLOWER, 0)
        );
    }

    @Getter
    @AllArgsConstructor
    public static class MaterialInfo {
        private final Material material;
        private final int data;
    }
}
