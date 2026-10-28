package ru.starfarm.mylittlefarm.menus;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;

/**
 * Created by Anton Znikin 24.08.2021.
 **/
public class DonateMenu extends InventoryContainer {

    public DonateMenu() {
        super("Дополнительные возможности", 5);
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        addItem(40,
                ApiManager.newItemBuilder(Material.PAPER)
                        .name("§aВ главное меню")
                        .build(), (baseInventory, event) -> new MainMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(18,
                ApiManager.newItemBuilder(Material.IRON_INGOT)
                        .name("§7Возможности групп: §aVIP§7, §aVIP+")
                        .addLore("§7- §fБонус к общему модификатору производства: §a0.5%§f/§a1%")
                        .build()
        );

        addItem(20,
                ApiManager.newItemBuilder(Material.GOLD_INGOT)
                        .name("§7Возможности групп: §2PREMIUM§7, §2PREMIUM+")
                        .addLore("§7- §fБонус к общему модификатору производства: §a1.75%§f/§a2%")
                        .build()
        );

        addItem(22,
                ApiManager.newItemBuilder(Material.DIAMOND)
                        .name("§7Возможности групп: §eELITE§7, §eELITE+")
                        .addLore("§7- §fБонус к общему модификатору производства: §a2.25%§f/§a2.5%")
                        .addLore("§7- §fБесплатный доступ к двойному прыжку")
                        .build()
        );

        addItem(24,
                ApiManager.newItemBuilder(Material.EMERALD)
                        .name("§7Возможности групп: §6SPONSOR§7, §6SPONSOR+")
                        .addLore("§7- §fБонус к общему модификатору производства: §a2.75%§f/§a3.5%")
                        .addLore("§7- §fБесплатный доступ к двойному прыжку")
                        .build()
        );

        addItem(26,
                ApiManager.newItemBuilder(Material.NETHER_STAR)
                        .name("§7Возможности групп: §3UNIQUE")
                        .addLore("§7- §fБонус к общему модификатору производства: §a5%")
                        .addLore("§7- §fБесплатный доступ к двойному прыжку")
                        .addLore("§7- §fУвеличенный шанс на получение положительного события")
                        .build()
        );
    }
}
