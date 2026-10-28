package ru.starfarm.mylittlefarm.enums;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.starfarm.mylittlefarm.objects.Creature;

import java.lang.reflect.Field;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Slf4j
public enum AnimalType implements Creature {
    CHICKEN("&e&lКурочка", "яйца", 11, createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzAyYjI3YTJlNmVmZjg0YjA0M2Q2OGMzYzNlYTY2NDdiZWYxZmEwNzYzOTVjZWMwODk4YTYxOTY3M2ExMTZmOSJ9fX0=")),
    RABBIT("&e&lКролик", "кроличью шерсть", 13, createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI3ZDVjMGRkNmZmNTE0ZWRiM2FhMzkyMTA3NzAyNTAxZDUwYmY5MGJjNWU1ODIyYWVjYjFkODZjMmQ0YmE5MiJ9fX0=")),
    SHEEP("&e&lОвечка", "овечье мясо", 15, createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzhhM2ZkMWFiZDczMGM3Y2I0NjkwYWY5MDY0MzU2ZDkzMmZmODRkZWM4N2JiZDM1ZGM4NzBiYTNjZGM4YzBjMCJ9fX0=")),
    PIG("&e&lСвинюша", "свинину", 29, createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmVlODUxNDg5MmYzZDc4YTMyZTg0NTZmY2JiOGM2MDgxZTIxYjI0NmQ4MmYzOThiZDk2OWZlYzE5ZDNjMjdiMyJ9fX0=")),
    COW("&e&lКоровка", "молоко", 31, createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU4NDU2MTU1MTQyY2JlNGU2MTM1M2ZmYmFmZjMwNGQzZDljNGJjOTI0N2ZjMjdiOTJlMzNlNmUyNjA2N2VkZCJ9fX0=")),
    HORSE("&e&lЛошадка", "конину", 33, createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjk5MDgyMTk1MWZkOWFiOTMwMWFlYmY1ZDc2YTliNjBmMTVlYmY2MTllMWM5ZThkNjY4OTE5ZDM5NGQ5YTkzMyJ9fX0="));

    private final String name;
    private final String produce;
    private final int slot;
    private final ItemStack itemStack;
    private final Product product = Product.values()[ordinal()];
    private final EntityType type = EntityType.valueOf(name());

    public static AnimalType ofUpgradeType(UpgradeType upgradeType) {
        AnimalType animalType = null;
        try {
            animalType = valueOf(upgradeType.name().replace("_BOOSTER", ""));
        } catch (IllegalArgumentException ignored) {
        }
        return animalType;
    }

    public static ItemStack createCustomHead(String value) {
        String signature = "H116D5fhmj/7BVWqiRQilXmvoJO6wJzXH4Dvou6P2o9YMb+HaJT8s9+zt03GMYTipzK+NsW2D2JfzagnxLUTuiOtrCHm6V2udOM0HG0JeL4zR0Wn5oHmu+S7kUPUbt7HVlKaRXry5bobFQ06nUf7hOV3kPfpUJsfMajfabmoJ9RGMRVot3uQszjKOHQjxyAjfHP2rjeI/SktBrSscx0DzwBW9LCra7g/6Cp7/xPQTIZsqz2Otgp6i2h3YpXJPy02j4pIk0H4biR3CaU7FB0V4/D1Hvjd08giRvUpqF0a1w9rbpIWIH5GTUP8eLFdG/9SnHqMCQrTj4KkQiN0GdBO18JvJS/40LTn3ZLag5LBIa7AyyGus27N3wdIccvToQ6kHHRVpW7cUSXjircg3LOsSQbJmfLoVJ/KAF/m+de4PxIjOJIcbiOkVyQfMQltPg26VzRiu3F0qRvJNAAydH8AHdaqhkpSf6yjHqPU3p3BHFJld5o59WoD4WNkE3wOC//aTpV/f9RJ0JQko08v2mGBVKx7tpN7vHD1qD5ILzV1nDCV1/qbKgiOK9QmdXqZw9J3pM/DHtZ6eiRKni9BuGWlbWFN/qfFO2xY+J7SYFqTxBbffmvwvuF83QP5UdRTNVLYoV5S+yR5ac7fVWUZmLbq7tawyuCu0Dw24M9E1BSnpSc=";

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", value, signature));

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        Field profileField;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, gameProfile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
        skull.setItemMeta(skullMeta);

        return skull;
    }
}
