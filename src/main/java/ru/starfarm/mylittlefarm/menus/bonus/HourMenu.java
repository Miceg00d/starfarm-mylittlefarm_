package ru.starfarm.mylittlefarm.menus.bonus;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.time.Duration;

public class HourMenu extends InventoryContainer implements Listener {

    private static final int TICKS_PER_HOUR = 20 * 60 * 60;

    public HourMenu() {
        super("Каждый час получает бонус", 5);
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        addItem(40,
                ApiManager.newItemBuilder(Material.PAPER)
                        .name("§aНазад")
                        .build(), (baseInventory, event) -> new BonusMenu().openInventory((Player) event.getWhoClicked())
        );
        boolean isReceivedBonus = playerResolver.getLongData(DataType.RECEIVED_BONUS) > System.currentTimeMillis();
        int playedHours = player.getStatistic(Statistic.PLAY_ONE_TICK) / TICKS_PER_HOUR;

        addItem(21, getBonusItem(playedHours >= 1, isReceivedBonus, "1 час", 1, playedHours), (baseInventory, event) -> {
            if (!isReceivedBonus && playedHours >= 1) {
                playerResolver.addBalance(playerResolver.getIncome() * 8000 / 100);
                playerResolver.setLongDataLevel(DataType.RECEIVED_BONUS, (System.currentTimeMillis() + Duration.ofHours(1).toMillis()));
                player.closeInventory();

            }

        });

        addItem(22, getBonusItem(playedHours >= 8, isReceivedBonus, "8 часов", 8, playedHours), (baseInventory, event) -> {
            if (!isReceivedBonus && playedHours >= 8) {
                playerResolver.addBalance(playerResolver.getIncome() * 10000 / 100);
                playerResolver.setLongDataLevel(DataType.RECEIVED_BONUS, (System.currentTimeMillis() + Duration.ofHours(1).toMillis()));
                player.closeInventory();

            }

        });

        addItem(23, getBonusItem(playedHours >= 24, isReceivedBonus, "24 часа", 24, playedHours), (baseInventory, event) -> {
            if (!isReceivedBonus && playedHours >= 24) {
                playerResolver.addBalance(playerResolver.getIncome() * 18000 / 100);
                playerResolver.setLongDataLevel(DataType.RECEIVED_BONUS, (System.currentTimeMillis() + Duration.ofHours(1).toMillis()));
                player.setStatistic(Statistic.PLAY_ONE_TICK, 0);
                player.closeInventory();
            }
        });
    }

    private ItemStack getBonusItem(boolean isEligible, boolean received, String timeLabel, int neededHours, int playedHours) {
        short durability = isEligible && !received ? (short) 7 : (short) 14;
        return ApiManager.newItemBuilder(Material.CONCRETE)
                .durability(durability)
                .name("§aБонус за " + timeLabel)
                .lore(received ? "§eПриходи позже" : isEligible ? "§aКликните для получения бонуса" : " ")
                .build();
    }
}