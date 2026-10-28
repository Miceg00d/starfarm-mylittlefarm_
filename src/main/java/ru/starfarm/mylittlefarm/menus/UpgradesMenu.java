package ru.starfarm.mylittlefarm.menus;

import lombok.NonNull;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.AnimalType;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.UpgradeType;
import ru.starfarm.mylittlefarm.objects.Upgrade;
import ru.starfarm.mylittlefarm.objects.achievements.GameAchievement;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

public class UpgradesMenu extends InventoryContainer {
    public int currentPage = 1;

    public UpgradesMenu() {
        super("Улучшения", 6);
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        if (currentPage == 1)
            addItem(45,
                    ApiManager.newItemBuilder(Material.PAPER)
                            .name("§cВ главное меню")
                            .build(), (inventory, event) -> new MainMenu().openInventory((Player) event.getWhoClicked())
            );
        else
            addItem(45,
                    ApiManager.newItemBuilder(Material.PAPER)
                            .name("§cПредыдущая страница")
                            .build(), (inventory, event) -> {
                        val clickedPlayer = (Player) event.getWhoClicked();
                        currentPage--;
                        updateInventory(clickedPlayer);
                    }
            );
        if (currentPage < 7)
            addItem(53,
                    ApiManager.newItemBuilder(Material.PAPER)
                            .name("§cСледующая страница")
                            .build(), (inventory, event) -> {
                        val clickedPlayer = (Player) event.getWhoClicked();

                        currentPage++;
                        updateInventory(clickedPlayer);
                    }
            );

        Upgrade.getUpgrades(currentPage).forEach(info -> addItem(info.getSlot(), info.getGuiIcon(playerResolver)));
    }

    public void addItem(int position, @NonNull ItemStack itemStack) {
        addItem(position, itemStack, (inventory, event) -> {
            Player player = (Player) event.getWhoClicked();
            val item = event.getCurrentItem();

            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

            String upgradeName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            val upg = Upgrade.getUpgrade(upgradeName);

            if (upg == null || upg.getPrice() <= 0) return;

            PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

            if (playerResolver.getBalance() < upg.getPrice()) return;
            if (playerResolver.getUpgradeLevel(upg.getGiveStat()) >= upg.getGiveQuantity()) return;

            int currentNeed = (upg.getNeedStat() instanceof CultureType) ?
                    playerResolver.getCultureLevel((CultureType) upg.getNeedStat()) :
                    playerResolver.getAnimalLevel((AnimalType) upg.getNeedStat());

            if (currentNeed < upg.getNeedQuantity()) return;
            if (playerResolver.getUpgradeLevel(upg.getPreviousStat()) < upg.getPreviousQuantity()) return;

            playerResolver.removeBalance(upg.getPrice());
            playerResolver.setUpgradeLevel(upg.getGiveStat(), (int) upg.getGiveQuantity());
            playerResolver.addDataLevel(DataType.UPGRADES, 1);

            if (upg.getGiveQuantity() == 25000 && upg.getGiveStat() != UpgradeType.GRASS_MONEY) {
                playerResolver.profile().giveAchievement(GameAchievement.MLF_MAX);
            }

            Bukkit.getScheduler().runTask(MyLittleFarm.getInstance(), () -> {
                if (!player.isOnline()) return;

                player.sendMessage(ChatUtil.color("&8[&2MyLittleFarm&8] &fВы приобрели улучшение &a" + upg.getId()));
                
                // Выдача Advancement для улучшения травы (или Title если advancement не загружен)
                if (upg.getGiveStat() == UpgradeType.GRASS_MONEY) {
                    try {
                        NamespacedKey key = new NamespacedKey("mlf", "grass_upgrade_1");
                        Advancement advancement = Bukkit.getAdvancement(key);
                        
                        if (advancement != null) {
                            // Отзываем сначала (чтобы окно показалось снова)
                            player.getAdvancementProgress(advancement).revokeCriteria("impossible");
                            // Выдаем
                            player.getAdvancementProgress(advancement).awardCriteria("impossible");
                            MyLittleFarm.getInstance().getLogger().info("Advancement выдан игроку " + player.getName());
                        } else {
                            MyLittleFarm.getInstance().getLogger().warning("Advancement не найден: mlf:grass_upgrade_1");
                            // Fallback: показываем Title
                            player.sendTitle("§a✔ Улучшение травы!", "§7Уровень повышен", 10, 60, 10);
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        }
                    } catch (Exception e) {
                        MyLittleFarm.getInstance().getLogger().warning("Ошибка выдачи advancement: " + e.getMessage());
                        player.sendTitle("§a✔ Улучшение травы!", "§7Уровень повышен", 10, 60, 10);
                    }
                }

                playerResolver.calculateIncome();

                if (upg.getNeedStat() instanceof CultureType && MyLittleFarm.getInstance().getCultureRegistry().getCultures().containsRow((CultureType) upg.getNeedStat())) {
                    MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(player, (CultureType) upg.getNeedStat());
                }

                updateInventory(player);
            });
        });
    }
}
