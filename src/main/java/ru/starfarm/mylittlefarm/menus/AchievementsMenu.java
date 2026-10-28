package ru.starfarm.mylittlefarm.menus;

import lombok.NonNull;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.objects.Achievement;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

public class AchievementsMenu extends InventoryContainer {
    public int currentPage = 1;

    public AchievementsMenu() {
        super("Цели", 6);
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

        if (currentPage < 5)
            addItem(53,
                    ApiManager.newItemBuilder(Material.PAPER)
                            .name("§cСледующая страница")
                            .build(), (inventory, event) -> {
                        val clickedPlayer = (Player) event.getWhoClicked();

                        currentPage++;
                        updateInventory(clickedPlayer);
                    }
            );

        Achievement.getAchievements(currentPage).forEach(info -> addItem(info.getSlot(), info.getGuiIcon(playerResolver)));
    }
}
