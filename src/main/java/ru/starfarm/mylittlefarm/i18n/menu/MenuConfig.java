package ru.starfarm.mylittlefarm.i18n.menu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import ru.starfarm.mylittlefarm.i18n.JsonValue;
import ru.starfarm.mylittlefarm.i18n.config.LanguageConfig;
import ru.starfarm.mylittlefarm.i18n.config.LiveConfiguration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class MenuConfig implements LanguageConfig.Menu, PropertyChangeListener {

    private final Map<String, JsonMenuModel> jsonMenus = new HashMap<>();
    private final LiveConfiguration liveConfiguration;
    private final JsonValue jsonValue = JsonValue.Base.getInstance();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public MenuConfig(String language) {
        this.liveConfiguration = new LiveConfiguration.Base(Path.of(String.format("/plugins/MyLittleFarm/l10n/menus/menus_%s.json", language.toLowerCase())));
        this.liveConfiguration.addPropertyChangeListener(this);
        loadConfig(this.liveConfiguration.config());
    }

    @Override
    public <T> T menuByKey(String key) {
        JsonMenuModel jsonMenu = jsonMenus.get(key);
        return (T) jsonMenu;
    }

    @Override
    public void sendUpdate(JsonMenuModel oldModel, JsonMenuModel newModel) {
        this.support.firePropertyChange(newModel.title(), oldModel, newModel);
    }

    private void sendUpdate() {
        this.jsonMenus.values().forEach(jsonMenuModel -> sendUpdate(null, jsonMenuModel));
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        reloadConfig((JsonNode) evt.getNewValue());
    }

    private void putAll(Map<String, JsonMenuModel> map) {
        this.jsonMenus.putAll(map);
    }

    @Override
    public void clearConfig() {
        this.jsonMenus.clear();
    }

    @Override
    public void loadConfig(JsonNode jsonNode) {
        Map<String, JsonMenuModel> menus = new HashMap<>();
        jsonNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            menus.put(key, jsonValue.value(value, new TypeReference<>() {
            }));
        });
        putAll(menus);
    }

    @Override
    public void reloadConfig(JsonNode jsonNode) {
        clearConfig();
        loadConfig(jsonNode);
        sendUpdate();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }


    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

}
