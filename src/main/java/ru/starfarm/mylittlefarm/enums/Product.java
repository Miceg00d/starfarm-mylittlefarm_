package ru.starfarm.mylittlefarm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import ru.starfarm.mylittlefarm.objects.Recipe;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public enum Product {
    EGG(1, "Яйца", Material.EGG, 1000000000000.0), //1 трлн
    RABBIT_HIDE(2, "Кроличья шерсть", Material.RABBIT_HIDE, 100000000000000.0), //100 трлн
    SHEEP_MEAT(3, "Овечье мясо", Material.MUTTON, 5000000000000000.0), //5 квдр
    PORK(5, "Свинина", Material.PORK, 60_000_000_000_000_000.0), //60 квдр
    MILK(6, "Молоко", Material.MILK_BUCKET, 250000000000000000.0), //250 квдр
    HORSE_MEAT(7, "Конина", Material.COOKED_MUTTON, 1000000000000000000.0), //1 квнт

    // Новые предметы - кейсы и ключи
    AMULET_CASE(100, "Кейс с амулетом", Material.ENDER_CHEST, 0.0),
    AMULET_KEY(101, "Ключ от кейса", Material.TRIPWIRE_HOOK, 0.0),

    OMELETTE(20, "Омлет", Material.MUSHROOM_SOUP, 6000000000000.0, Recipe.of(new HashMap<Product, Integer>() {{ //60 трлн
        put(EGG, 4);
    }}, (int) TimeUnit.MINUTES.toSeconds(5))),
    JACKET(29, "Куртка", Material.LEATHER_CHESTPLATE, 750000000000000.0, Recipe.of(new HashMap<Product, Integer>() {{
        put(RABBIT_HIDE, 5);
    }}, (int) TimeUnit.MINUTES.toSeconds(30))),
    LASAGNA(38, "Лазанья", Material.DOUBLE_PLANT, 15000000000000000.0, Recipe.of(new HashMap<Product, Integer>() {{
        put(SHEEP_MEAT, 2);
        put(EGG, 4);
    }}, (int) TimeUnit.MINUTES.toSeconds(10))),
    PORK_IN_FRENCH(22, "Свинина по-французки", Material.GRILLED_PORK, 250_000_000_000_000_000.0, Recipe.of(new HashMap<Product, Integer>() {{
        put(PORK, 2);
        put(EGG, 2);
    }}, (int) TimeUnit.MINUTES.toSeconds(60))),
    MEAT_PLATTER(31, "Мясное ассорти", Material.RABBIT_STEW, 350000000000000000.0, Recipe.of(new HashMap<Product, Integer>() {{
        put(MILK, 1);
        put(PORK, 2);
        put(SHEEP_MEAT, 2);
    }}, (int) TimeUnit.MINUTES.toSeconds(60))),
    CHAMPION_BREAKFAST(33, "Завтрак чемпиона", Material.CHORUS_FRUIT, 900000000000000000.0, Recipe.of(new HashMap<Product, Integer>() {{
        put(LASAGNA, 2);
        put(PORK_IN_FRENCH, 1);
        put(MEAT_PLATTER, 1);
    }}, (int) TimeUnit.MINUTES.toSeconds(60))),
    CAKE(24, "Тортик", Material.CAKE, 1500000000000000000.0, Recipe.of(new HashMap<Product, Integer>() {{
        put(MILK, 4);
        put(EGG, 10);
    }}, (int) TimeUnit.MINUTES.toSeconds(120))),
    DRIED_HORSE_MEAT(40, "Вяленная конина", Material.COOKED_MUTTON, 6000000000000000000.0, Recipe.of(new HashMap<Product, Integer>() {{
        put(HORSE_MEAT, 4);
        put(MILK, 3);
    }}, (int) TimeUnit.MINUTES.toSeconds(120))),
    CUTLET(42, "Котлетка", Material.COOKED_BEEF, 15000000000000000000.0, Recipe.of(new HashMap<Product, Integer>() {{
        put(DRIED_HORSE_MEAT, 2);
        put(MILK, 1);
        put(MEAT_PLATTER, 1);
    }}, (int) TimeUnit.MINUTES.toSeconds(120)));

    public final int slot;
    public final String name;
    public final Material material;
    public final double money;
    public Recipe recipe;


    public static List<Product> valuesProducts() {
        return Arrays.stream(values()).filter(info -> ValueRange.of(0, 5).isValidValue(info.ordinal())).collect(Collectors.toList());
    }

    public static List<Product> valuesRecipes() {
        return Arrays.stream(values()).filter(info -> !ValueRange.of(0, 5).isValidValue(info.ordinal())).collect(Collectors.toList());
    }

    public boolean canCrafting(PlayerResolver playerResolver) {
        return recipe.ingredients.entrySet().stream().allMatch(entry -> playerResolver.getProductCount(entry.getKey()) >= entry.getValue());
    }

    public String getFormattedName(int count) {
        switch (this) {
            case EGG:
                return StringUtils.fixNumerical(count, "яйцо", "яйца", "яиц");
            case RABBIT_HIDE:
                return StringUtils.fixNumerical(count, "шерсть кролика", "шерсти кролика", "шерсти кролика");
            case SHEEP_MEAT:
                return StringUtils.fixNumerical(count, "овечье мясо", "овечьих мяса", "овечьих мяса");
            case PORK:
                return StringUtils.fixNumerical(count, "свинина", "свинины", "свинины");
            case MILK:
                return StringUtils.fixNumerical(count, "молоко", "молока", "молока");
            case HORSE_MEAT:
                return StringUtils.fixNumerical(count, "конина", "конины", "конины");
            case AMULET_CASE:
                return StringUtils.fixNumerical(count, "кейс с амулетом", "кейса с амулетом", "кейсов с амулетами");
            case AMULET_KEY:
                return StringUtils.fixNumerical(count, "ключ от кейса", "ключа от кейса", "ключей от кейсов");
            case OMELETTE:
                return StringUtils.fixNumerical(count, "омлет", "омлета", "омлетов");
            case LASAGNA:
                return StringUtils.fixNumerical(count, "лазанья", "лазаньи", "лазаний");
            case JACKET:
                return StringUtils.fixNumerical(count, "куртка", "куртки", "курток");
            case PORK_IN_FRENCH:
                return StringUtils.fixNumerical(count, "свинина по-французки", "свинины по-французки", "свинин по-французки");
            case MEAT_PLATTER:
                return StringUtils.fixNumerical(count, "мясное ассорти", "мясных ассорти", "мясных ассорти");
            case CHAMPION_BREAKFAST:
                return StringUtils.fixNumerical(count, "завтрак чемпиона", "завтрака чемпиона", "завтраков чемпиона");
            case CAKE:
                return StringUtils.fixNumerical(count, "тортик", "тортика", "тортов");
            case DRIED_HORSE_MEAT:
                return StringUtils.fixNumerical(count, "вяленная конина", "вяленных конины", "вяленных конины");
            case CUTLET:
                return StringUtils.fixNumerical(count, "котлетка", "котлетки", "котлет");
            default:
                return null;
        }
    }
}
