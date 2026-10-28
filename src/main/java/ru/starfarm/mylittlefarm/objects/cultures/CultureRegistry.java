package ru.starfarm.mylittlefarm.objects.cultures;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.CorePlugin;
import ru.starfarm.core.hologram.Hologram;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.number.PercentUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.UpgradeType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.LocationUtils;
import ru.starfarm.mylittlefarm.utils.StringUtils;
import ru.starfarm.mylittlefarm.utils.config.YamlConfig;

import java.util.Arrays;

@Slf4j
public class CultureRegistry {
    public @Getter Table<CultureType, Integer, CultureInfo> cultures = HashBasedTable.create();
    public @Getter Table<Player, CultureType, Hologram> protocolHolographics = HashBasedTable.create();

    public CultureRegistry() {
        CorePlugin plugin = MyLittleFarm.getInstance();
        ConfigurationSection cultureConfigSection = plugin.getConfig().getConfigurationSection("cultures");
        ConfigurationSection cultureLocationSection = new YamlConfig(plugin, "culture_locations.yml").configurationSection("cultures");

        if (cultureConfigSection != null && !cultureConfigSection.getKeys(false).isEmpty()) {
            cultureConfigSection.getKeys(false).forEach((key) -> {

                ConfigurationSection keySection = cultureConfigSection.getConfigurationSection(key);
                ConfigurationSection locationSection = cultureLocationSection != null ? cultureLocationSection.getConfigurationSection(key) : null;
                
                try {
                    CultureType cultureType = CultureType.valueOf(key);
                    
                    if (key.equals("FOUNTAIN")) {
                        if (keySection != null && locationSection != null) {
                            keySection.getKeys(false).forEach((i) -> {
                                ConfigurationSection cultureSection = keySection.getConfigurationSection(i);
                                ConfigurationSection locations = locationSection.getConfigurationSection(i);
                                if (cultureSection != null && locations != null) {
                                    cultures.put(cultureType, Integer.parseInt(i), new CultureInfo(cultureSection.getDouble("price"), cultureSection.getDouble("income"), locations.getStringList("locations")));
                                } else {
                                    log.warn("Missing configuration for FOUNTAIN level {}: cultureSection={}, locations={}", i, cultureSection != null, locations != null);
                                }
                            });
                        } else {
                            log.warn("Missing FOUNTAIN configuration: keySection={}, locationSection={}", keySection != null, locationSection != null);
                        }
                    } else {
                        final double basePrice = keySection.getDouble("price");
                        final double income = keySection.getDouble("income");
                        final double priceMultiplier = keySection.getDouble("price_multiplier");
                        double totalPrice = basePrice;
                        
                        for (int i = 1; i <= 100; i++) {
                            ConfigurationSection locations = locationSection != null ? locationSection.getConfigurationSection(Integer.toString(i)) : null;
                            if (locations != null) {
                                cultures.put(cultureType, i, new CultureInfo(totalPrice, income, locations.getStringList("locations")));
                            } else {
                                log.warn("Missing location configuration for {} level {}", key, i);
                                cultures.put(cultureType, i, new CultureInfo(totalPrice, income, java.util.Collections.emptyList()));
                            }
                            totalPrice = totalPrice * priceMultiplier;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Unknown culture type: {}", key, e);
                } catch (Exception e) {
                    log.error("Error loading culture: {}", key, e);
                }
            });
            log.info("Loaded {} cultures!", cultureConfigSection.getKeys(false).size());
        } else {
            log.warn("No culture configuration found!");
        }
    }


    public CultureInfo getCulture(CultureType cultureType, int id) {
        CultureInfo culture = cultures.get(cultureType, id);
        if (culture == null) {
            log.debug("Culture not found: {} level {} - available cultures for this type: {}", 
                cultureType, id, cultures.row(cultureType).keySet());
        }
        return culture;
    }

    public Location getLocation(CultureType cultureType) {
        return LocationUtils.fromString(MyLittleFarm.getInstance().getConfig().getString("cultures.".concat(cultureType.name()).concat(".hologramLocation")));
    }

    public String getMessages(CultureType cultureType, int id) {
        return MyLittleFarm.getInstance().getConfig().getStringList("cultures.".concat(cultureType.name()).concat(".messages")).get(id);
    }

    public String getAchievements(CultureType cultureType, int id) {
        return MyLittleFarm.getInstance().getConfig().getStringList("cultures.".concat(cultureType.name()).concat(".achievements")).get(id);
    }

    public String getUpgrades(CultureType cultureType, int id) {
        return MyLittleFarm.getInstance().getConfig().getStringList("cultures.".concat(cultureType.name()).concat(".upgrades")).get(id);
    }

    public void loadHolographics(Player player) {
        final MyLittleFarm plugin = MyLittleFarm.getInstance();
        final String playerName = player.getName();


        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                Player p = org.bukkit.Bukkit.getPlayer(playerName);


                if (p == null || !p.isOnline()) {
                    this.cancel();
                    return;
                }

                PlayerResolver resolver = plugin.getGamePlayer().getPlayer(p);


                if (resolver != null) {

                    final Player finalPlayer = p;

                    java.util.Arrays.stream(CultureType.values())
                            .filter(c -> c != CultureType.FOUNTAIN && c != CultureType.OTHER)
                            .forEach(culture -> updateHolographic(finalPlayer, culture));


                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void unloadHolographic(Player player) {
        if (!protocolHolographics.containsRow(player)) {
            return;
        }

        protocolHolographics.rowMap().remove(player).forEach(((culture, hologram) -> hologram.handleRemove()));
    }

    public void updateHolographic(Player player, CultureType cultureType) {

        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        if (playerResolver == null) return;


        if (!protocolHolographics.contains(player, cultureType)) {
            Location loc = getLocation(cultureType);
            if (loc == null) return;
            Hologram hologram = ApiManager.createHologram(loc.clone().add(0, 0.8, 0));
            hologram.setAccess(p -> p.getName().equalsIgnoreCase(player.getName()));
            hologram.textLine(0, "§7");
            hologram.textLine(1, "§7");
            hologram.textLine(2, "§7");
            protocolHolographics.put(player, cultureType, hologram);
        }

        Hologram protocolHolographic = protocolHolographics.get(player, cultureType);
        if (protocolHolographic == null) return;

        int level = playerResolver.getCultureLevel(cultureType);


        protocolHolographic.textLine(2, ChatUtil.format("&b&l%s &f&l%s шт.", cultureType.getName(), level));


        if (level >= 100) {
            protocolHolographic.textLine(1, ChatUtil.format("&2&lДостигнут лимит"));
        } else {
            double discount = playerResolver.getData(DataType.RB_UPGRADE_CULTURE_DISCOUNT) * 0.01;
            CultureInfo nextLevelCulture = getCulture(cultureType, level + 1);
            
            // Пропускаем если культура следующего уровня не найдена в реестре
            if (nextLevelCulture == null) {
                protocolHolographic.textLine(1, ChatUtil.format("&c&lНедоступно"));
                return;
            }
            
            double basePrice = nextLevelCulture.price();
            double finalPrice = basePrice * (1.0 - discount); //

            String color = playerResolver.getBalance() >= finalPrice ? "§a§l" : "§c§l";
            protocolHolographic.textLine(1, ChatUtil.format("%sКупить за %s$", color, StringUtils.formatDouble(finalPrice)));
        }


        if (level > 0) {
            var incomeObj = playerResolver.getCultureIncome(cultureType);
            double totalIncome;
            if (incomeObj != null) {
                double base = (incomeObj.cultureIncome() + incomeObj.incomeIncreasedByUpgrades() + 
                              incomeObj.incomeIncreasedByBoosters() + incomeObj.incomeIncreasedByModifier()) * 
                              incomeObj.fountainModifier();
                double rbMultiplier = playerResolver.getGlobalMoneyMultiplier();
                totalIncome = base * rbMultiplier;
            } else {
                CultureInfo baseCulture = getCulture(cultureType, 1);
                if (baseCulture == null) {
                    protocolHolographic.textLine(0, "§c§lОшибка загрузки культуры");
                    return;
                }
                totalIncome = baseCulture.baseIncome() * level;
            }
            protocolHolographic.textLine(0, ChatUtil.format("&e&lПроизводит %s$/сек", StringUtils.formatDouble(totalIncome)));
        } else {
            protocolHolographic.textLine(0, "§7Участок не куплен");
        }
    }
}