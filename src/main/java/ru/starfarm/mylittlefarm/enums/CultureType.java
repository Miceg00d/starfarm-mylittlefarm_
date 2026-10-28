package ru.starfarm.mylittlefarm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.starfarm.mylittlefarm.objects.Creature;

@Slf4j
@Getter
@AllArgsConstructor
public enum CultureType implements Creature {
    WHEAT(0, "Пшеница"),
    POTATO(2, "Картошка"),
    CARROT(4, "Морковь"),
    SUGAR_CANE(6, "Тростник"),
    PUMPKIN(8, "Тыква"),
    MELON(10, "Арбуз"),
    RED_MUSHROOM(12, "Красный гриб"),
    BROWN_MUSHROOM(14, "Бурый гриб"),
    ROSE(16, "Роза"),
    LILY(20, "Ландыш"),
    HERMIT(22, "Пустынник"),
    NETHER(24, "Адский нарост"),
    CHORUS(31, "Цветок хоруса"),

    FOUNTAIN(-1, "Фонтан"),
    OTHER(-3, "Другое");

    private final int id;
    private final String name;

    public static boolean isPlant(CultureType cultureType) {
        return cultureType != FOUNTAIN && cultureType != OTHER;
    }

    public static CultureType ofItemKey(String key) {
        CultureType cultureType = null;
        try {
            cultureType = valueOf(key.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            log.error("Wrong key for CultureType" + key);
        }
        return cultureType;
    }

    public static CultureType ofUpgradeType(UpgradeType upgradeType) {
        String name = upgradeType.name().replace("_BOOSTER", "").replace("_MONEY", "");

        if (name.startsWith("GRASS")) {
            return null;
        }

        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {

            log.warn("Unknown culture for upgrade: " + upgradeType.name());
            return null;
        }
    }
}
