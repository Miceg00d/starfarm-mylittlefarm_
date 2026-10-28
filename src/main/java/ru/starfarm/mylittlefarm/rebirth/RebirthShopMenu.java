package ru.starfarm.mylittlefarm.rebirth;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.item.ItemUtil;
import ru.starfarm.core.util.item.ItemBuilder;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import java.util.Arrays;
import java.util.function.Consumer;

public class RebirthShopMenu extends InventoryContainer {

    public RebirthShopMenu() {
        super("Улучшения за Токены", 4);
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver res = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        if (res == null) return;

        addItem(4, ItemUtil.build(Material.DOUBLE_PLANT, (Consumer<ItemBuilder>) b -> b.setName("§dТокены: §l" + res.getRbTokens())));

        setupUpgrade(player, res, 19, Material.IRON_BLOCK, "§bБонус Токенов", DataType.RB_UPGRADE_TOKEN_BONUS, 5.0, 1, 1, 50, " Токен.");
        setupUpgrade(player, res, 21, Material.GOLD_BLOCK, "§6Бустер Денег", DataType.RB_UPGRADE_MONEY_BOOSTER, 35.0, 1, 1, 150, "%");
        setupUpgrade(player, res, 23, Material.EMERALD_BLOCK, "§eСкидка на Культуры", DataType.RB_UPGRADE_CULTURE_DISCOUNT, 1.0, 1, 1, 60, "%");
        setupUpgrade(player, res, 25, Material.DIAMOND_BLOCK, "§3Скидка на Перерождение", DataType.RB_UPGRADE_REBIRTH_DISCOUNT, 2.0, 1, 1, 25, "%");

        addItem(31, ItemUtil.build(Material.ARROW, (Consumer<ItemBuilder>) b -> b.setName("§7Назад")), (c, e) -> new RebirthMainMenu().openInventory(player));
    }

    private void setupUpgrade(Player player, PlayerResolver res, int slot, Material mat, String name, DataType type, double bonusPerLevel, int baseCost, int step, int maxLvl, String unit) {
        int lvl = res.getData(type);
        int cost = baseCost + (lvl * step);


        boolean isDiscount = type == DataType.RB_UPGRADE_CULTURE_DISCOUNT || type == DataType.RB_UPGRADE_REBIRTH_DISCOUNT;
        String sign = isDiscount ? "-" : "+";
        String color = isDiscount ? "§c" : "§a";

        addItem(slot, ItemUtil.build(mat, (Consumer<ItemBuilder>) b -> {
            b.setName(name + " §8[§e" + lvl + "/" + maxLvl + "§8]");
            String currentBonus = String.format("%.0f", (lvl * bonusPerLevel));
            String nextBonus = String.format("%.0f", ((lvl + 1) * bonusPerLevel));
            b.setLore(Arrays.asList(
                    "§7Текущий бонус: " + color + sign + currentBonus + unit,
                    "§7Следующий уровень: " + color + sign + nextBonus + unit,
                    "§7Цена: §d" + cost + " Токен."
            ));
        }), (c, e) -> {
            if (lvl < maxLvl && res.getRbTokens() >= cost) {
                res.setRbTokens(res.getRbTokens() - cost);
                res.setDataLevel(type, lvl + 1);
                res.calculateIncome();

                updateRelevantHolograms(player, res, type);

                updateRebirthMenus(player, res);

                MyLittleFarm.getInstance().getGamePlayer().save(res);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                drawInventory(player);
            }
        });
    }

    private void updateRebirthMenus(Player player, PlayerResolver res) {
        if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null) {
            try {
                String title = player.getOpenInventory().getTitle();
                if (title.contains("Подтверждение") || title.contains("Перерождение")) {
                    player.closeInventory();
                    new RebirthConfirmMenu(new RebirthManager()).openInventory(player);
                }
            } catch (Exception e) {
                try {
                    if (player.getOpenInventory().getTopInventory().getName().contains("Подтверждение")) {
                        player.closeInventory();
                        new RebirthConfirmMenu(new RebirthManager()).openInventory(player);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void updateRelevantHolograms(Player player, PlayerResolver res, DataType upgradeType) {
        if (upgradeType == DataType.RB_UPGRADE_MONEY_BOOSTER) {
            res.getCulturesLog().keySet().stream()
                    .filter(cultureType -> res.getCultureLevel(cultureType) > 0)
                    .forEach(cultureType -> {
                        try {
                            MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(player, cultureType);
                        } catch (Exception ignored) {}
                    });
        } else if (upgradeType == DataType.RB_UPGRADE_CULTURE_DISCOUNT) {
            for (CultureType cultureType : CultureType.values()) {
                try {
                    MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(player, cultureType);
                } catch (Exception ignored) {}
            }
        } else if (upgradeType == DataType.RB_UPGRADE_TOKEN_BONUS) {
            res.getCulturesLog().keySet().stream()
                    .filter(cultureType -> res.getCultureLevel(cultureType) > 0)
                    .forEach(cultureType -> {
                        try {
                            MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(player, cultureType);
                        } catch (Exception ignored) {}
                    });
        } else if (upgradeType == DataType.RB_UPGRADE_REBIRTH_DISCOUNT) {

        }
    }
}
