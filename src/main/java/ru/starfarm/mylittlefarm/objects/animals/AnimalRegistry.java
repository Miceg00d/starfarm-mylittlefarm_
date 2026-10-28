package ru.starfarm.mylittlefarm.objects.animals;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.starfarm.core.hologram.Hologram;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.AnimalType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.LocationUtils;
import ru.starfarm.mylittlefarm.utils.StringUtils;

@Slf4j
public class AnimalRegistry {
    public @Getter Table<AnimalType, Integer, AnimalInfo> animals = HashBasedTable.create();
    public @Getter Table<Player, AnimalType, Hologram> holograms = HashBasedTable.create();

    public AnimalRegistry() {
        ConfigurationSection section = MyLittleFarm.getInstance().getConfig().getConfigurationSection("animals");
        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((key) -> {
                ConfigurationSection keySection = section.getConfigurationSection(key);

                keySection.getKeys(false).forEach((i) -> {
                    if (!NumberUtils.isNumber(i))
                        return;
                    ConfigurationSection cultureSection = keySection.getConfigurationSection(i);
                    animals.put(AnimalType.valueOf(key), Integer.parseInt(i), new AnimalInfo(cultureSection.getDouble("price"), EntityType.valueOf(key)));
                });
            });

            log.info("Loaded {} animals!", section.getKeys(false).size());
        }
    }


    public AnimalInfo getAnimal(AnimalType animal, int id) {
        return animals.get(animal, id);
    }

    public Location getLocation(AnimalType animalType) {
        return LocationUtils.fromString(MyLittleFarm.getInstance().getConfig().getString("animals.".concat(animalType.name()).concat(".hologramLocation")));
    }

    public String getAchievements(AnimalType animalType, int id) {
        return MyLittleFarm.getInstance().getConfig().getStringList("animals.".concat(animalType.name()).concat(".achievements")).get(id);
    }

    public String getUpgrades(AnimalType animalType, int id) {
        return MyLittleFarm.getInstance().getConfig().getStringList("animals.".concat(animalType.name()).concat(".upgrades")).get(id);
    }

    public void unload(Player player) {
        if (!holograms.containsRow(player)) {
            return;
        }

        holograms.rowMap().remove(player).forEach(((culture, hologram) -> hologram.handleRemove()));
    }

    public void update(Player player, AnimalType animalType) {
        if (!holograms.containsRow(player))
            return;
        if (!holograms.containsColumn(animalType))
            return;

        val gamePlayer = MyLittleFarm.getInstance().getGamePlayer();
        PlayerResolver playerResolver = gamePlayer.getPlayer(gamePlayer.getVisit().getOrDefault(player, player));
        Hologram protocolHolographic = holograms.get(player, animalType);

        protocolHolographic.textLine(2, ChatUtil.format("&b&l%s &f&l%s шт.", animalType.getName(), playerResolver.getAnimalLevel(animalType)));
        protocolHolographic.textLine(1, playerResolver.getAnimalLevel(animalType) >= 5 ?
                ChatUtil.format("&2&lДостигнут лимит") :
                ChatUtil.format("%s%s$", playerResolver.getBalance() < MyLittleFarm.getInstance().getAnimalRegistry().getAnimal(animalType, playerResolver.getAnimalLevel(animalType) + 1).getPrice() ?
                        "§c§l" : "§a§l", ChatUtil.format("Купить за %s", StringUtils.formatDouble(MyLittleFarm.getInstance().getAnimalRegistry().getAnimal(animalType, playerResolver.getAnimalLevel(animalType) + 1).getPrice()))
                )
        );

        if (playerResolver.getAnimalLevel(animalType) > 0) {
            protocolHolographic.textLine(0,
                    ChatUtil.format("&e&lПроизводит %s", animalType.getProduce())
            );
        }

    }
}