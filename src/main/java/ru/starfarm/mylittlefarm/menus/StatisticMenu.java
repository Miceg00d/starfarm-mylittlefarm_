package ru.starfarm.mylittlefarm.menus;

import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.profile.Profile;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.number.PercentUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.EventType;
import ru.starfarm.mylittlefarm.enums.UpgradeType;
import ru.starfarm.mylittlefarm.objects.achievements.GameAchievement;
import ru.starfarm.mylittlefarm.objects.modifiers.Modifiers;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.player.enums.Top;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticMenu extends InventoryContainer {
    public StatisticMenu() {
        super("Статистика", 3);
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        addItem(4, ApiManager.newItemBuilder(Material.SKULL_ITEM)
                .name("&a".concat(player.getName()))
                .lore(
                        "&7Баланс: &a" + StringUtils.formatDouble(resolver.getBalance()) + " $",
                        "&7Доход: &a" + StringUtils.formatDouble(resolver.getIncome()) + " $",
                        "  &7(с учётом модификатора: &2" + StringUtils.formatDouble(resolver.getIncome() + PercentUtil.getNumberByPercent(Modifiers.getModifier(), resolver.getIncome())) + " $&7)",
                        "&7Активные события: " + (resolver.getEvents().isEmpty() ? "&cНет" : resolver.getEvents().keySet().stream().map(EventType::getName).collect(Collectors.joining("&7, "))),
                        "&7Всего событий: &a" + resolver.getData(DataType.EVENTS),
                        ChatUtil.format("  &7(%s полож. подряд, %s отриц. подряд)", resolver.getData(DataType.POSITIVE_EVENTS), resolver.getData(DataType.NEGATIVE_EVENTS)),
                        "&7Сломано травы: &a" + StringUtils.formatDouble(resolver.getGrass()),
                        "&7Сломано пшеницы: &a" + StringUtils.formatDouble(resolver.getHays()),
                        "&7Сходили в гости: &a" + resolver.getData(DataType.FARM_VISIT) + " раз(а)",
                        ChatUtil.format("&7Выполнено целей: &a%s из 77", resolver.getData(DataType.ACHIEVEMENTS)),
                        ChatUtil.format("&7Выполнено достижений: &a%s из 19", (int) Arrays.stream(GameAchievement.values()).filter(achievement -> has(resolver.profile(), achievement)).count()),
                        ChatUtil.format("&7Приобретено улучшений: &a%s из 94", resolver.getData(DataType.UPGRADES)),
                        "",
                        "&7Место в топах:",
                        " &f- &7Доход: &a" + MyLittleFarm.getInstance().getGamePlayer().getTopNumber(Top.INCOME, player),
                        " &f- &7Баланс: &a" + MyLittleFarm.getInstance().getGamePlayer().getTopNumber(Top.BALANCE, player),
                        " &f- &7Перерождения: &d" + MyLittleFarm.getInstance().getGamePlayer().getTopNumber(Top.REBIRTH, player)
                )
                .durability(3)
                .skull(player.getName())
                .build()
        );

        addItem(10, ApiManager.newItemBuilder(Material.PUMPKIN)
                .name("&aКультуры")
                .lore(
                        "&7Нажмите, чтобы перейти в",
                        "&7меню информации о культурах."
                )
                .build(), (__, event) -> new StatisticCultureInfoMenu().openInventory(player)
        );

        addItem(13, ApiManager.newItemBuilder(Material.MILK_BUCKET)
                .name("&aЖивотные")
                .lore(
                        "&7Нажмите, чтобы перейти в",
                        "&7меню информации о животных."
                )
                .build()
        );

        addItem(16, ApiManager.newItemBuilder(Material.WATER_BUCKET)
                .name("&aПостройки")
                .lore(
                        "&7Нажмите, чтобы перейти в",
                        "&7меню информации о постройках."
                )
                .build(), (__, event) -> new ConstructionBuildingsMenu(false).openInventory(player)
        );
    }

    public boolean has(Profile profile, GameAchievement achievement) {
        return profile.getAchievements().contains(achievement.name());
    }


    static class StatisticCultureInfoMenu extends InventoryContainer {

        public StatisticCultureInfoMenu() {
            super("Статистика | Культуры", 4);
        }

        @Override
        public void drawInventory(@NotNull Player player) {
            val resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

            Arrays.stream(CultureType.values()).filter(CultureType::isPlant).forEach(info -> {
                val materialInfo = getMaterial().get(info.ordinal());
                int count = resolver.getCultureLevel(info);

                addItem(info.getId(), ApiManager.newItemBuilder(count == 0 ? Material.BARRIER : materialInfo.getMaterial())
                        .durability(materialInfo.getData())
                        .name(ChatUtil.format("&b&l%s &f&l%sшт", info.getName(), resolver.getCultureLevel(info)))
                        .lore(count == 0 ?
                                Collections.singletonList(
                                        ChatUtil.format("%s%s$", resolver.getBalance() < MyLittleFarm.getInstance().getCultureRegistry().getCulture(info, count + 1).price() ?
                                                "§c" : "§a", ChatUtil.format("Стоимость %s", StringUtils.formatDouble(MyLittleFarm.getInstance().getCultureRegistry().getCulture(info, count + 1).price()))
                                        )
                                )
                                :
                                count == 100 ?
                                        Collections.singletonList(ChatUtil.format("&7Производит: &a%s $/сек", StringUtils.formatDouble(((MyLittleFarm.getInstance().getCultureRegistry().getCulture(info, 1).baseIncome() * resolver.getCultureLevel(info) + PercentUtil.getNumberByPercent(resolver.getUpgradeLevel(UpgradeType.valueOf(info.name().concat("_BOOSTER"))), MyLittleFarm.getInstance().getCultureRegistry().getCulture(info, 1).baseIncome() * resolver.getCultureLevel(info))) * (resolver.getCultureLevel(CultureType.FOUNTAIN) > 0 ? resolver.getCultureLevel(CultureType.FOUNTAIN) : 1)))))
                                        :
                                Arrays.asList(
                                        ChatUtil.format("%s%s$", resolver.getBalance() < MyLittleFarm.getInstance().getCultureRegistry().getCulture(info, count + 1).price() ?
                                                "§c" : "§a", ChatUtil.format("Стоимость %s", StringUtils.formatDouble(MyLittleFarm.getInstance().getCultureRegistry().getCulture(info, count + 1).price()))
                                        ),
                                        ChatUtil.format("&7Производит: &a%s $/сек", StringUtils.formatDouble(((MyLittleFarm.getInstance().getCultureRegistry().getCulture(info, 1).baseIncome() * resolver.getCultureLevel(info) + PercentUtil.getNumberByPercent(resolver.getUpgradeLevel(UpgradeType.valueOf(info.name().concat("_BOOSTER"))), MyLittleFarm.getInstance().getCultureRegistry().getCulture(info, 1).baseIncome() * resolver.getCultureLevel(info))) * (resolver.getCultureLevel(CultureType.FOUNTAIN) > 0 ? resolver.getCultureLevel(CultureType.FOUNTAIN) : 1))))
                                )
                        )
                        .build()
                );
            });

            addItem(27, ApiManager.newItemBuilder(Material.PAPER)
                    .name("&aНазад").build(), (__, event) -> new StatisticMenu().openInventory(player));
        }

        public List<BookMenu.MaterialInfo> getMaterial() {
            return Arrays.asList(
                    new BookMenu.MaterialInfo(Material.WHEAT, 0),
                    new BookMenu.MaterialInfo(Material.POTATO_ITEM, 0),
                    new BookMenu.MaterialInfo(Material.CARROT_ITEM, 0),
                    new BookMenu.MaterialInfo(Material.SUGAR_CANE, 0),
                    new BookMenu.MaterialInfo(Material.PUMPKIN, 0),
                    new BookMenu.MaterialInfo(Material.MELON_BLOCK, 0),
                    new BookMenu.MaterialInfo(Material.RED_MUSHROOM, 0),
                    new BookMenu.MaterialInfo(Material.BROWN_MUSHROOM, 0),
                    new BookMenu.MaterialInfo(Material.RED_ROSE, 0),
                    new BookMenu.MaterialInfo(Material.RED_ROSE, 6),
                    new BookMenu.MaterialInfo(Material.DEAD_BUSH, 0),
                    new BookMenu.MaterialInfo(Material.NETHER_STALK, 0),
                    new BookMenu.MaterialInfo(Material.CHORUS_FLOWER, 0)
            );
        }
    }

    static class StatisticAnimalsMenu extends InventoryContainer {

        public StatisticAnimalsMenu() {
            super("Статистика | Животные", 4);
        }

        @Override
        public void drawInventory(@NotNull Player player) {

        }
    }
}
