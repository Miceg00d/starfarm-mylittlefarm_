package ru.starfarm.mylittlefarm.enums;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.util.Arrays;
import java.util.Comparator;

@AllArgsConstructor
public enum Flower {
    LARGE_FERN(UpgradeType.GRASS_MONEY, Material.DOUBLE_PLANT, (byte) 3, 5, 5),
    DEAD_BUSH(UpgradeType.GRASS_BOOSTER, Material.DEAD_BUSH, (byte) 0, 5, 10),
    BLUE_ORCHID(UpgradeType.GRASS_BOOSTER, Material.RED_ROSE, (byte) 1, 10, 15),
    LILAC(UpgradeType.GRASS_MONEY, Material.DOUBLE_PLANT, (byte) 1, 10_000_000, 20),
    ROSE_BUSH(UpgradeType.GRASS_BOOSTER, Material.DOUBLE_PLANT, (byte) 4, 20, 30),
    PEONY(UpgradeType.GRASS_MONEY, Material.DOUBLE_PLANT, (byte) 5, 1_000_000_000, 50);

    public final UpgradeType upgradeType;
    public final Material material;
    public final byte data;
    public final int count;
    public final double modifier;

    public static Flower get(PlayerResolver playerResolver) {
        return Arrays.stream(values())
                .filter(info -> playerResolver.getUpgradeLevel(info.upgradeType) >= info.count)
                .max(Comparator.comparingInt(Enum::ordinal)).orElse(null);
    }
}
