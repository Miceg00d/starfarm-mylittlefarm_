package ru.starfarm.mylittlefarm.i18n.config;

import com.fasterxml.jackson.databind.JsonNode;
import ru.starfarm.mylittlefarm.i18n.item.ItemModel;
import ru.starfarm.mylittlefarm.i18n.menu.JsonMenuModel;

import java.beans.PropertyChangeListener;

public interface LanguageConfig {

    void loadConfig(JsonNode jsonNode);

    void reloadConfig(JsonNode jsonNode);

    void addPropertyChangeListener(PropertyChangeListener pcl);

    void removePropertyChangeListener(PropertyChangeListener pcl);

    void clearConfig();


    interface Message extends LanguageConfig {
        String messageByPathForMap(String main, String secondary);
    }

    interface Item extends LanguageConfig {
        ItemModel itemByKey(String key);
    }

    interface Menu extends LanguageConfig {
        <T> T menuByKey(String key);

        void sendUpdate(JsonMenuModel oldModel, JsonMenuModel newModel);
    }
}
