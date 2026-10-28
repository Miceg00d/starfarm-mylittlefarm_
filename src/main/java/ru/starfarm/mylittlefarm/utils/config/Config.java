package ru.starfarm.mylittlefarm.utils.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.starfarm.mylittlefarm.MyLittleFarm;

import java.io.File;
import java.io.IOException;

@Getter
public class Config {
    private final MyLittleFarm main;
    private final String fileName;
    private final String defaultConfig;
    private FileConfiguration configuration;

    public Config(String fileName, String defaultConfig) {
        main = MyLittleFarm.getInstance();
        this.fileName = fileName;
        this.defaultConfig = defaultConfig;
        init();
    }

    private void init() {
        if (!main.getDataFolder().exists())
            main.getDataFolder().mkdirs();
        File file = getConfigurationFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
            }
        }
        reload();
    }

    private void reload() {
        configuration = YamlConfiguration.loadConfiguration(getConfigurationFile());
    }

    public void save() {
        try {
            configuration.save(getConfigurationFile());
        } catch (IOException ignored) {
        }
    }

    private File getConfigurationFile() {
        return new File(main.getDataFolder(), fileName);
    }
}
