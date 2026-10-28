package ru.starfarm.mylittlefarm.commands;

import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.command.Command;
import ru.starfarm.core.command.context.CommandContext;
import ru.starfarm.core.util.item.ItemUtil;
import ru.starfarm.mylittlefarm.enums.Product;
import ru.starfarm.mylittlefarm.menus.amulets.AmuletCaseMenu;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.MyLittleFarm;

public class AmuletCommand extends Command<Player> {
    
    public AmuletCommand() {
        super("amulet", "Команда для работы с амулетами", "", "amulets", "амулеты");
    }
    
    @Override
    public void execute(@NotNull CommandContext<Player> commandContext) {
        Player player = commandContext.getSender();
        PlayerResolver resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        
        if (commandContext.getArgs().isEmpty()) {
            new AmuletCaseMenu().openInventory(player);
            return;
        }
        
        String arg = commandContext.getArgs().get(0).toString().toLowerCase();
        
        switch (arg) {
            case "case":
            case "кейс":
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && item.getType() == Material.ENDER_CHEST && 
                    item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                    item.getItemMeta().getDisplayName().equals("§6§lКейс с амулетом")) {
                    new AmuletCaseMenu().openInventory(player);
                } else {
                    player.sendMessage("§c§lОшибка! §7Возьмите кейс в руку.");
                }
                break;
                
            case "keys":
            case "ключи":
                int keys = resolver.getProductCount(Product.AMULET_KEY);
                player.sendMessage("§e§lКлючи от кейсов: §a" + keys + " §7шт.");
                break;
                
            case "givecase":
            case "выдатькейс":
                if (player.hasPermission("mylittlefarm.admin")) {
                    if (commandContext.getArgs().size() >= 2) {
                        try {
                            int amount = Integer.parseInt(commandContext.getArgs().get(1).toString());
                            resolver.addProductLevel(Product.AMULET_CASE, amount);
                            player.sendMessage("§a§lВыдано! §7Вы получили " + amount + " кейсов.");
                        } catch (NumberFormatException e) {
                            player.sendMessage("§c§lОшибка! §7Укажите корректное количество.");
                        }
                    } else {
                        resolver.addProductLevel(Product.AMULET_CASE, 1);
                        player.sendMessage("§a§lВыдано! §7Вы получили 1 кейс.");
                    }
                } else {
                    player.sendMessage("§c§lОшибка! §7У вас нет прав на эту команду.");
                }
                break;
                
            case "givekey":
            case "выдатьключ":
                if (player.hasPermission("mylittlefarm.admin")) {
                    if (commandContext.getArgs().size() >= 2) {
                        try {
                            int amount = Integer.parseInt(commandContext.getArgs().get(1).toString());
                            resolver.addProductLevel(Product.AMULET_KEY, amount);
                            player.sendMessage("§a§lВыдано! §7Вы получили " + amount + " ключей.");
                        } catch (NumberFormatException e) {
                            player.sendMessage("§c§lОшибка! §7Укажите корректное количество.");
                        }
                    } else {
                        resolver.addProductLevel(Product.AMULET_KEY, 1);
                        player.sendMessage("§a§lВыдано! §7Вы получили 1 ключ.");
                    }
                } else {
                    player.sendMessage("§c§lОшибка! §7У вас нет прав на эту команду.");
                }
                break;
                
            case "addcase":
            case "добавитькейс":
                if (player.hasPermission("mylittlefarm.admin")) {
                    Location loc = player.getLocation().getBlock().getLocation();
                    MyLittleFarm.getInstance().getAmuletCaseManager().addCaseLocation(loc);
                    player.sendMessage("§a§lКейс добавлен! §7На вашей позиции: " + 
                        String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
                    player.sendMessage("§7Поставьте эндер сундук на этой координате!");
                } else {
                    player.sendMessage("§c§lОшибка! §7У вас нет прав на эту команду.");
                }
                break;
                
            case "removecase":
            case "удалитькейс":
                if (player.hasPermission("mylittlefarm.admin")) {
                    Location loc = player.getLocation().getBlock().getLocation();
                    MyLittleFarm.getInstance().getAmuletCaseManager().removeCaseLocation(loc);
                    player.sendMessage("§a§lКейс удален! §7С вашей позиции: " + 
                        String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
                } else {
                    player.sendMessage("§c§lОшибка! §7У вас нет прав на эту команду.");
                }
                break;
                
            default:
                player.sendMessage("§e§lКоманды амулетов:");
                player.sendMessage("§7/amulet - открыть меню кейсов");
                player.sendMessage("§7/amulet keys - показать количество ключей");
                player.sendMessage("§7/amulet case [количество] - выдать кейс (админ)");
                player.sendMessage("§7/amulet key [количество] - выдать ключ (админ)");
                player.sendMessage("§7/amulet addcase - добавить кейс на позиции (админ)");
                player.sendMessage("§7/amulet removecase - удалить кейс с позиции (админ)");
                player.sendMessage("§7");
                player.sendMessage("§7Для физического кейса:");
                player.sendMessage("§71. Поставьте эндер сундук");
                player.sendMessage("§72. Используйте /amulet addcase на этой позиции");
                player.sendMessage("§73. Над сундуком появится надпись");
                break;
        }
    }
}
