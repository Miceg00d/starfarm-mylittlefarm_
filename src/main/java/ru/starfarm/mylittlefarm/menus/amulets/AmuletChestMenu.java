package ru.starfarm.mylittlefarm.menus.amulets;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.util.item.ItemUtil;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.Product;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.MyLittleFarm;

public class AmuletChestMenu {
    
    private final Inventory inventory;
    
    public AmuletChestMenu(Player player) {
        PlayerResolver resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        int keys = resolver.getProductCount(Product.AMULET_KEY);
        
        inventory = Bukkit.createInventory(null, 27, ChatUtil.format("&6&lКейс с амулетами"));
        fillBorders();

        ItemStack caseItem = ItemUtil.build(Material.ENDER_CHEST, 1, (short) 0, builder -> {
            builder.name("§6§lКейс с амулетами");
            builder.lore(
                "§7У вас есть §e" + keys + " §7ключей",
                "",
                (keys > 0 ? "§a§lНажмите чтобы открыть!" : "§c§lНужно больше ключей!"),
                "",
                "§7Ключи можно получить за:",
                "§8• Перерождения",
                "§8• Покупка в донате",
                "§8• Мероприятия"
            );
        });
        
        inventory.setItem(13, caseItem);

        ItemStack keyInfo = ItemUtil.build(Material.TRIPWIRE_HOOK, 1, (short) 0, builder -> {
            builder.name("§e§lИнформация о ключах");
            builder.lore(
                "§7Ключи используются для открытия",
                "§7кейсов с амулетами.",
                "",
                "§7Текущее количество: §e" + keys,
                "",
                "§cКлючи не возвращаются",
                "§cесли амулет не понравится!"
            );
        });
        
        inventory.setItem(11, keyInfo);

        ItemStack stats = ItemUtil.build(Material.BOOK, 1, (short) 0, builder -> {
            builder.name("§b§lВаши амулеты");
            builder.lore(
                "§7Собрано амулетов: §e" + resolver.getData(DataType.AMULETS_STORAGE),
                "",
                "§7Скоро будет инвентарь",
                "§7для экипировки амулетов!"
            );
        });
        
        inventory.setItem(15, stats);
    }
    
    private void fillBorders() {
        ItemStack border = ItemUtil.build(Material.STAINED_GLASS_PANE, 1, (short) 7, builder -> {
            builder.name(" ");
        });

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18) {
                inventory.setItem(i, border);
            }
        }

        inventory.setItem(9, border);
        inventory.setItem(10, border);
        inventory.setItem(16, border);
        inventory.setItem(17, border);
    }
    
    public void openInventory(Player player) {
        player.openInventory(inventory);
    }
    
    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String itemName = clicked.getItemMeta().getDisplayName();
        
        if (itemName != null && itemName.contains("Кейс с амулетами")) {
            event.setCancelled(true);
            
            PlayerResolver resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
            int keys = resolver.getProductCount(Product.AMULET_KEY);
            
            if (keys > 0) {
                player.closeInventory();

                new AmuletCaseMenu().openInventory(player);
            } else {
                player.sendMessage(ChatUtil.format("&c&lОшибка! &7У вас нет ключей для открытия кейса."));
                player.sendMessage(ChatUtil.format("&7Получайте ключи за перерождения или другими способами."));
                player.closeInventory();
            }
        }
    }
}
