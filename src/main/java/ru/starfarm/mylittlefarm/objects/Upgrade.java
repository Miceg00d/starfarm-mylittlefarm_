package ru.starfarm.mylittlefarm.objects;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.enums.AnimalType;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.UpgradeType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Upgrade {
    public static Map<String, Upgrade> upgrades = new LinkedHashMap<>();
    private final String id;
    private final short data;
    private final Material material;
    private List<String> lore;
    private Creature needStat;
    private final int needQuantity;
    private UpgradeType previousStat;
    private final int previousQuantity;
    private UpgradeType giveStat;
    private final int giveQuantity;
    private final double price;
    private final int page, slot;

    public Upgrade(String id, ConfigurationSection section) {
        this.id = id.replace("^", ".");
        material = Material.valueOf(section.getString("material"));
        data = (short) section.getInt("data");
        if (section.getStringList("lore") != null)
            lore = section.getStringList("lore");
        needStat = null;
        if (!section.getString("need-stat").equals("null")) {
            try {
                needStat = CultureType.valueOf(section.getString("need-stat"));
            } catch (IllegalArgumentException ignored) {
                needStat = AnimalType.valueOf(section.getString("need-stat"));
            }
        }
        previousStat = null;
        if (section.getString("previous-stat") != null) {
            try {
                previousStat = UpgradeType.valueOf(section.getString("previous-stat"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        giveStat = null;
        if (section.getString("give-stat") != null) {
            try {
                giveStat = UpgradeType.valueOf(section.getString("give-stat"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        needQuantity = section.getInt("need-quantity");
        previousQuantity = section.getInt("previous-quantity");
        giveQuantity = section.getInt("give-quantity");
        price = section.getDouble("price");
        page = section.getInt("page");
        slot = section.getInt("slot");
        upgrades.put(id, this);
    }

    public static Upgrade getUpgrade(String id) {
        return upgrades.get(id.replace(".", "^"));
    }

    public static List<Upgrade> getUpgrades(int page) {
        return upgrades.values().stream()
                .filter(info -> info.page == page)
                .collect(Collectors.toList());
    }

    public ItemStack getGuiIcon(PlayerResolver playerResolver) {
        ItemStack itemStack;
        if (price == 0) {
            List<String> itemLore = new ArrayList<>();
            if (lore != null)
                itemLore.addAll(lore);
            itemStack =
                    ApiManager.newItemBuilder(material)
                            .durability(data)
                            .name("§a" + id)
                            .lore(itemLore)
                            .build();
        } else if ((needStat instanceof CultureType ? playerResolver.getCultureLevel((CultureType) needStat) : playerResolver.getAnimalLevel((AnimalType) needStat)) < needQuantity) {
            itemStack =
                    ApiManager.newItemBuilder(Material.WOOL)
                            .name("§c" + id)
                            .addLore("§7Это улучшение пока вам недоступно.")
                            .durability(14)
                            .build();
        } else if (playerResolver.getUpgradeLevel(previousStat) < previousQuantity) {
            itemStack =
                    ApiManager.newItemBuilder(Material.WOOL)
                            .name("§c" + id)
                            .addLore("§7Это улучшение пока вам недоступно.")
                            .durability(14)
                            .build();
        } else if (playerResolver.getUpgradeLevel(giveStat) >= giveQuantity) {
            List<String> itemLore = new ArrayList<>();
            if (lore != null)
                itemLore.addAll(lore);
            itemStack =
                    ApiManager.newItemBuilder(Material.WOOL)
                            .durability(13)
                            .name("§a" + id)
                            .lore(itemLore)
                            .enchant(Enchantment.ARROW_FIRE, 1)
                            .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            .build();
        } else if ((needStat instanceof CultureType ? playerResolver.getCultureLevel((CultureType) needStat) : playerResolver.getAnimalLevel((AnimalType) needStat)) >= needQuantity && playerResolver.getUpgradeLevel(previousStat) >= previousQuantity) {
            List<String> itemLore = new ArrayList<>();
            if (lore != null)
                itemLore.addAll(lore);
            if (price != 0) {
                itemLore.add("");
                itemLore.add(ChatUtil.color("&7Нажми, чтобы приобрести"));
                itemLore.add(ChatUtil.color("&7это улучшение за &a" + StringUtils.formatDouble(price) + "$"));
            }
            itemStack =
                    ApiManager.newItemBuilder(Material.WOOL)
                            .durability(4)
                            .name("§e" + id)
                            .lore(itemLore)
                            .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            .build();
        } else {
            itemStack =
                    ApiManager.newItemBuilder(Material.WOOL)
                            .name("§c" + id)
                            .addLore("§7Это улучшение пока вам недоступно.")
                            .durability(14)
                            .build();
        }
        return itemStack;
    }
}
