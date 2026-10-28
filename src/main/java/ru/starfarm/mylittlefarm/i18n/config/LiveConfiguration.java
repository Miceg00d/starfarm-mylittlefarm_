package ru.starfarm.mylittlefarm.i18n.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.starfarm.mylittlefarm.i18n.JsonValue;
import ru.starfarm.mylittlefarm.utils.FileWatcher;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface LiveConfiguration extends I18nConfiguration {

    void watch();

    void sendUpdate(JsonNode oldConfig, JsonNode newConfig);

    void addPropertyChangeListener(PropertyChangeListener pcl);

    void removePropertyChangeListener(PropertyChangeListener pcl);


    @Slf4j(topic = "MyLittleFarm-Configurations")
    class Base implements LiveConfiguration, PropertyChangeListener {

        private final Path configPath;
        private JsonNode config;
        private final PropertyChangeSupport support;

        private final ObjectMapper objectMapper = JsonValue.Base.getInstance().objectMapper();
        private final FileWatcher fileWatcher = FileWatcher.getInstance();


        public Base(Path configPath) {
            this.configPath = Paths.get(System.getProperty("user.dir").concat(configPath.toString()));
            this.support = new PropertyChangeSupport(this);
            this.fileWatcher.addPropertyChangeListener(this);
            loadConfig();
        }

        @Override
        public void loadConfig() {
            try {
                this.config = objectMapper.readTree(this.configPath.toFile());
                sendUpdate(null, this.config);
                watch();
            } catch (IOException exception) {
                log.error(String.format("Config file with path %s not loaded", this.configPath.toAbsolutePath()) + exception);
            }
        }

        @Override
        public void reloadConfig() {
            JsonNode oldConfig = this.config;
            JsonNode newConfig;
            try {
                newConfig = objectMapper.readTree(this.configPath.toFile());
                log.info(String.format("Config %s has been reloaded", this.configPath.toAbsolutePath()));
                sendUpdate(oldConfig, newConfig);
                this.config = newConfig;
            } catch (IOException e) {
                throw new RuntimeException("Error while reload config" + e);
            }
        }

        @Override
        public JsonNode config() {
            if (this.config == null) {
                loadConfig();
            }
            return this.config;
        }

        @Override
        public void watch() {
            fileWatcher.watchFile(this.configPath);
        }

        @Override
        public void sendUpdate(JsonNode oldConfig, JsonNode newConfig) {
            support.firePropertyChange("reload_config", oldConfig, newConfig);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            support.addPropertyChangeListener(pcl);
            if (this.config != null) {
                sendUpdate(null, this.config);
            }
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            support.removePropertyChangeListener(pcl);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(this.configPath.toString())) {
                reloadConfig();
            }
        }
    }
}
