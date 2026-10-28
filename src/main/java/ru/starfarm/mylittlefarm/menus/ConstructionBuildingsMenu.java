package ru.starfarm.mylittlefarm.menus;

import lombok.NonNull;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.BlockInfo;
import ru.starfarm.mylittlefarm.objects.achievements.GameAchievement;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

public class ConstructionBuildingsMenu extends InventoryContainer {
    private final double fountainPrice = MyLittleFarm.getInstance().getCultureRegistry().getCulture(CultureType.FOUNTAIN, 2).price();
    private final double fountain2Price = MyLittleFarm.getInstance().getCultureRegistry().getCulture(CultureType.FOUNTAIN, 4).price();
    public boolean button;

    public ConstructionBuildingsMenu(boolean button) {
        super("Постройка зданий", 3);

        this.button = button;
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        if (button) {
            addItem(22,
                    ApiManager.newItemBuilder(Material.PAPER)
                            .name("§aВ главное меню")
                            .build(), (baseInventory, event) -> new MainMenu().openInventory((Player) event.getWhoClicked())
            );
        }

        val fountain = ApiManager.newItemBuilder(Material.WATER_BUCKET);
        fountain.setName("§b§lФонтан");
        int fountainLevel = playerResolver.getCultureLevel(CultureType.FOUNTAIN);

        fountain.addLore(fountainLevel < 2 ? "§c§lНе куплено" : "§a§lКуплено");
        if (fountainLevel < 2) {
            fountain.addLore("");
            fountain.addLore(ChatUtil.format("§7Цена: %s", playerResolver.getBalance() >= fountainPrice ? "§a100 млрд $" : "§c100 млрд $"));
        }
        fountain.addLore("");
        fountain.addLore("§7Удваивает производство");

        if (fountainLevel >= 2) {
            fountain.enchant(Enchantment.ARROW_FIRE, 1);
            fountain.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        addItem(12, fountain.build(), (baseInventory, event) -> {
            Player p = (Player) event.getWhoClicked();
            PlayerResolver res = MyLittleFarm.getInstance().getGamePlayer().getPlayer(p);

            if (res.getCultureLevel(CultureType.FOUNTAIN) >= 2) return;
            if (res.getBalance() < fountainPrice) {
                ChatUtil.sendMessage(p, "&8[&2MyLittleFarm&8] &cНедостаточно денег!");
                return;
            }

            res.removeBalance(fountainPrice);
            res.setCultureLevel(CultureType.FOUNTAIN, 2);

            Bukkit.getScheduler().runTask(MyLittleFarm.getInstance(), () -> {
                updateBlocksForPlayer(p, CultureType.FOUNTAIN, 2);
                new ConstructionBuildingsMenu(this.button).openInventory(p);
                ChatUtil.sendMessage(p, "&8[&2MyLittleFarm&8] &fВы приобрели постройку &bФонтан&f.");
                res.profile().giveAchievement(GameAchievement.MLF_BUILDING);
            });
        });

        val fontanishche = ApiManager.newItemBuilder(Material.WATER_BUCKET);
        fontanishche.setName("§b§lФонтанище");
        fontanishche.addLore(fountainLevel < 4 ? "§c§lНе куплено" : "§a§lКуплено");

        if (fountainLevel < 4) {
            fontanishche.addLore("");
            fontanishche.addLore(String.format("§7Цена: %s", playerResolver.getBalance() >= fountain2Price ? "§a25 трлн $" : "§c25 трлн $"));
        }
        fontanishche.addLore("");
        fontanishche.addLore("§7Удваивает производство");

        if (fountainLevel >= 4) {
            fontanishche.enchant(Enchantment.ARROW_FIRE, 1);
            fontanishche.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        addItem(14, fontanishche.build(), (baseInventory, event) -> {
            Player p = (Player) event.getWhoClicked();
            PlayerResolver res = MyLittleFarm.getInstance().getGamePlayer().getPlayer(p);

            if (res.getCultureLevel(CultureType.FOUNTAIN) >= 4) return;
            if (res.getCultureLevel(CultureType.FOUNTAIN) < 2) {
                ChatUtil.sendMessage(p, "&8[&2MyLittleFarm&8] &cСначала купите обычный фонтан!");
                return;
            }
            if (res.getBalance() < fountain2Price) return;

            res.removeBalance(fountain2Price);
            res.setCultureLevel(CultureType.FOUNTAIN, 4);

            Bukkit.getScheduler().runTask(MyLittleFarm.getInstance(), () -> {
                updateBlocksForPlayer(p, CultureType.FOUNTAIN, 4);
                new ConstructionBuildingsMenu(this.button).openInventory(p);
                ChatUtil.sendMessage(p, "&8[&2MyLittleFarm&8] &fВы приобрели постройку &bФонтанище&f.");
                res.profile().giveAchievement(GameAchievement.MLF_BUILDING);
            });
        });
    }

    private void updateBlocksForPlayer(Player p, CultureType type, int level) {
        val locations = MyLittleFarm.getInstance().getCultureRegistry().getCulture(type, level).locations();
        if (locations == null) return;

        for (String s : locations) {
            String[] block = s.split(" ");
            Location loc = new Location(Bukkit.getWorld(block[2]), Double.parseDouble(block[3]), Double.parseDouble(block[4]), Double.parseDouble(block[5]));
            int id = Integer.parseInt(block[0]);
            byte data = Byte.parseByte(block[1]);

            p.sendBlockChange(loc, id, data);
            MyLittleFarm.getInstance().getGamePlayer().getBlocks().get(p).add(new BlockInfo(id, data, loc));
        }
    }
}

