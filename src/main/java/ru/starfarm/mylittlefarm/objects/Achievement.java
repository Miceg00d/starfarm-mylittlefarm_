package ru.starfarm.mylittlefarm.objects;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.ApiManager;
import ru.starfarm.mylittlefarm.enums.AnimalType;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Achievement {
    public static Map<String, Achievement> achievements = new LinkedHashMap<>();
    private final String id;
    private final short data;
    private final Material material;
    private final String materialString;
    private List<String> lore;
    private Creature needStat;
    private final int quantity;
    private final double money;
    private final boolean hide;
    private final String footer;
    private final int page, slot;

    public Achievement(String id, ConfigurationSection section) {
        this.id = id.replace("^", "..");
        material = Material.valueOf(section.getString("material"));
        data = (short) section.getInt("data");
        materialString = section.getString("materialString");
        if (section.getStringList("lore") != null)
            lore = section.getStringList("lore");
        footer = section.getString("footer");
        needStat = null;
        if (!section.getString("need-stat").equals("null")) {
            try {
                needStat = CultureType.valueOf(section.getString("need-stat"));
            } catch (IllegalArgumentException ignored) {
                needStat = AnimalType.valueOf(section.getString("need-stat"));
            }
        }
        quantity = section.getInt("quantity");
        money = section.getDouble("money");
        hide = section.getBoolean("hide");
        page = section.getInt("page");
        slot = section.getInt("slot");
        achievements.put(id, this);
    }

    public static Achievement getAchievement(String id) {
        return achievements.get(id);
    }

    public static List<Achievement> getAchievements(int page) {
        return achievements.values().stream()
                .filter(info -> info.page == page)
                .collect(Collectors.toList());
    }

    public ItemStack getGuiIcon(PlayerResolver playerResolver) {
        ItemStack itemStack;
        if (!hide) {
            itemStack = ApiManager.newItemBuilder(material)
                    .name("§a" + id)
                    .durability(data)
                    .lore(lore)
                    .build();
        } else {
            if ((needStat instanceof CultureType ? playerResolver.getCultureLevel((CultureType) needStat) : playerResolver.getAnimalLevel((AnimalType) needStat)) >= quantity) {
                itemStack = ApiManager.newItemBuilder(material)
                        .name("§a" + id)
                        .durability(data)
                        .lore(lore)
                        .enchant(Enchantment.ARROW_FIRE, 1)
                        .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        .build();
            } else
                itemStack =
                        ApiManager.newItemBuilder(Material.WOOL)
                                .name("§c" + id)
                                .lore("§7Эта цель еще не достигнута вами.")
                                .durability(15)
                                .build();
        }
        return itemStack;
    }
}
