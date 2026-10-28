package ru.starfarm.mylittlefarm.menus.watcher;

import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.item.ItemUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.utils.StringUtils;

public class WatcherMenu extends InventoryContainer {

    private final int COST_OF_ABILITY_TO_SELL_HAY = 500;

    public WatcherMenu() {
        super("§8Продажа сена", 3);
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        val playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        if (playerResolver.getData(DataType.HAY_SELL_ABILITY) != 1) {
            addItem(13, ItemUtil.build(Material.HAY_BLOCK, itemBuilder -> {
                itemBuilder.setName("&e&lРазблокировать продажу сена");
                itemBuilder.addLore(
                        "&cСток сена (Не разблокировано)",
                        "&7Разблокирование возможность добычи",
                        "&7а также продаже сена.",
                        String.format("&7Стоимость разблокировки: &6%s$", COST_OF_ABILITY_TO_SELL_HAY)
                );
            }), (container, event) -> {
                if (playerResolver.getBalance() < COST_OF_ABILITY_TO_SELL_HAY) {
                    ChatUtil.sendMessage(player, "§cУ вас недостаточно денег!");
                    return;
                }

                playerResolver.setDataLevel(DataType.HAY_SELL_ABILITY, 1);
                playerResolver.removeBalance(COST_OF_ABILITY_TO_SELL_HAY);
                ChatUtil.sendMessage(player, "&a&lВы успешно приобрели сено!");
                player.closeInventory();
            });
        } else {
            addItem(13, ItemUtil.build(Material.HAY_BLOCK, itemBuilder -> {
                itemBuilder.setName("§aПродать сено");
                if (playerResolver.getData(DataType.BROKEN_HAY) == 1)
                    itemBuilder.addLore(
                            "§fДоступно к продаже §a" + playerResolver.getData(DataType.BROKEN_HAY),
                            "§fВы получите за 1 блок §a" + StringUtils.formatDouble(playerResolver.getIncome() * 30) + " монет",
                            "§eБлок сена можно добыть раз в 5 минут"
                    );
                else itemBuilder.addLore(
                        "§fДоступно к продаже §a" + playerResolver.getData(DataType.BROKEN_HAY),
                        "§fВы получите за §a" + StringUtils.fixNumerical(playerResolver.getData(DataType.BROKEN_HAY), "§fблок сена §e", "§fблока сена §e", "§fблоков сена §e") + StringUtils.formatDouble(playerResolver.getIncome() * 30 * playerResolver.getData(DataType.BROKEN_HAY)) + " §fмонет",
                        "§eБлок сена можно добыть раз в 5 минут"
                );
            }), (container, event) -> {
                if (playerResolver.getData(DataType.BROKEN_HAY) <= 0) {
                    ChatUtil.sendMessage(player, "§cУ вас нет сена на продажу!");
                    return;
                }

                val result = (playerResolver.getIncome() * 30) * playerResolver.getData(DataType.BROKEN_HAY);
                ChatUtil.sendMessage(player, "§aПродано §2%d §aсена за §2%s §aмонет.", playerResolver.getData(DataType.BROKEN_HAY), StringUtils.formatDouble(result));
                playerResolver.addBalance(result);
                playerResolver.setDataLevel(DataType.BROKEN_HAY, 0);
                player.closeInventory();
            });
        }
    }
}