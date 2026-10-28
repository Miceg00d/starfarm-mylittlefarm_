package ru.starfarm.mylittlefarm.menus.bonus;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.mylittlefarm.menus.MainMenu;

public class BonusMenu extends InventoryContainer {

    public BonusMenu() {
        super("Активность и бонусы", 5);
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        addItem(40,
                ApiManager.newItemBuilder(Material.PAPER)
                        .name("§aВ главное меню")
                        .build(), (baseInventory, event) -> new MainMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(21,
                ApiManager.newItemBuilder(Material.GOLD_NUGGET)
                        .name("§7Каждый час получает бонус")
                        .build(), (baseInventory, event) -> new HourMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(23,
                ApiManager.newItemBuilder(Material.DIAMOND_PICKAXE)
                        .name("§7Копатель")
                        .build(), (baseInventory, event) -> new DiggerMenu().openInventory((Player) event.getWhoClicked())
        );
    }
}
