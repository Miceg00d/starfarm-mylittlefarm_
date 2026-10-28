package ru.starfarm.mylittlefarm.utils.config;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

public class YamlConfig{
    private final YamlConfiguration fileConfiguration;

    public YamlConfig(@NonNull Path configPath) {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(configPath.toFile());
    }

    public YamlConfig(@NonNull Plugin plugin, @NonNull String resourceName) {
        this(plugin.getDataFolder().toPath().resolve(resourceName));
    }

    public YamlConfiguration configuration() {
        return this.fileConfiguration;
    }

    public ConfigurationSection configurationSection(String path) {
        return fileConfiguration.getConfigurationSection(path);
    }

}
