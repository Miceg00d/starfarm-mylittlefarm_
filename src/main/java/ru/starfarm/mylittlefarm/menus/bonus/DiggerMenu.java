package ru.starfarm.mylittlefarm.menus.bonus;

import lombok.NonNull;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.objects.tasks.TaskManager;
import ru.starfarm.mylittlefarm.utils.StringUtils;

public class DiggerMenu extends InventoryContainer {

    public DiggerMenu() {
        super("Копатель", 5);
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        val playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        addItem(40,
                ApiManager.newItemBuilder(Material.PAPER)
                        .name("§aНазад")
                        .build(), (baseInventory, event) -> new BonusMenu().openInventory((Player) event.getWhoClicked())
        );

        TaskManager.getTasks().forEach((task -> {
            val isCompleted = playerResolver.isTaskCompleted(task);
            addItem(task.getSlot(), ApiManager.newItemBuilder(Material.CONCRETE)
                    .name("&2Задание #" + task.getId())
                    .durability(isCompleted ? 7 : 14)
                    .lore(
                            "",
                            String.format("§aСломать траву: §e%d/%d", playerResolver.getGrass(), task.getNeedGrass()),
                            String.format("§aСломать блок сена: §e%d/%d", playerResolver.getHays(), task.getNeedHay()),
                            "",
                            isCompleted ? "§eНаграда получена" : "§aНаграда: §e" + StringUtils.formatDouble(playerResolver.getIncome() * task.getBonus()),
                            "",
                            isCompleted ? "&aВыполнено" : "&eВ процессе"
                    ).build(), (inventory, event) -> {
                if (isCompleted) {
                    return;
                }

                if (playerResolver.getGrass() >= task.getNeedGrass() && playerResolver.getHays() >= task.getNeedHay()) {
                    playerResolver.addBalance(playerResolver.getIncome() * task.getBonus());
                    ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &aЗадание успешно выполнено!");
                    playerResolver.setTaskCompleted(task);
                    player.closeInventory();
                }
            });
        }));
    }
}
