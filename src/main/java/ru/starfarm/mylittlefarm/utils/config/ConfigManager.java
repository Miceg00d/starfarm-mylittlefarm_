package ru.starfarm.mylittlefarm.utils.config;

import lombok.Getter;

@Getter
public class ConfigManager {
    private Config config;
    private Config achievementConfig;
    private Config upgradeConfig;

    public ConfigManager() {
        init();
    }

    private void init() {
        config = new Config("config.yml", "config.yml");
        achievementConfig = new Config("achievements.yml", "achievements.yml");
        upgradeConfig = new Config("upgrades.yml", "upgrades.yml");
    }
}
