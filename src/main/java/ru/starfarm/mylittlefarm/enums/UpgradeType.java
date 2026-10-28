package ru.starfarm.mylittlefarm.enums;

public enum UpgradeType {
    GRASS_MONEY, GRASS_BOOSTER, WHEAT_BOOSTER, POTATO_BOOSTER, CARROT_BOOSTER, SUGAR_CANE_BOOSTER,
    PUMPKIN_BOOSTER, MELON_BOOSTER, RED_MUSHROOM_BOOSTER, BROWN_MUSHROOM_BOOSTER, ROSE_BOOSTER,
    LILY_BOOSTER, HERMIT_BOOSTER, CHORUS_BOOSTER, NETHER_BOOSTER, CHICKEN_BOOSTER, RABBIT_BOOSTER, SHEEP_BOOSTER,
    PIG_BOOSTER, COW_BOOSTER, HORSE_BOOSTER, FISHING_LINE, FISHING_LEVEL;

    public static UpgradeType ofCultureType(CultureType cultureType) {
        UpgradeType upgradeType = null;
        try {
            upgradeType = valueOf(cultureType.name().concat("_BOOSTER"));
        } catch (IllegalArgumentException ignored) {
        }
        return upgradeType;
    }
}
