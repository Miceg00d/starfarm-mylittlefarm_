package ru.starfarm.mylittlefarm.rebirth;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.item.ItemUtil;
import java.util.Arrays;

public class RebirthMainMenu extends InventoryContainer {

    public RebirthMainMenu() {
        super("Меню перерождений", 3);
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        addItem(11, ItemUtil.build(Material.NETHER_STAR, b -> {
            b.setName("§b§lПерерождение");
            b.setLore(Arrays.asList("", "§7Сбрось свой прогресс и получи токены для улучшения.", "", "§e▶ Нажми, чтобы перейти"));
        }), (c, e) -> new RebirthConfirmMenu(new RebirthManager()).openInventory(player));

        addItem(15, ItemUtil.build(Material.BEACON, b -> {
            b.setName("§a§lМагазин Бонусов");
            b.setLore(Arrays.asList("", "§7Прокачай свою ферму навсегда.", "", "§e▶ Нажми, чтобы открыть"));
        }), (c, e) -> new RebirthShopMenu().openInventory(player));
    }
}
