package ru.starfarm.mylittlefarm.i18n.messages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import ru.starfarm.mylittlefarm.i18n.JsonValue;
import ru.starfarm.mylittlefarm.i18n.config.LanguageConfig;
import ru.starfarm.mylittlefarm.i18n.config.LiveConfiguration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class L10nMessages implements LanguageConfig.Message, PropertyChangeListener {

    private final LiveConfiguration liveConfiguration;
    private final Map<String, Map<String, String>> messages = new ConcurrentHashMap<>();
    private final JsonValue jsonValue = JsonValue.Base.getInstance();

    public L10nMessages(String language) {
        this.liveConfiguration = new LiveConfiguration.Base(Path.of(String.format("/plugins/MyLittleFarm/l10n/messages/messages_%s.json", language.toLowerCase())));
        this.liveConfiguration.addPropertyChangeListener(this);
        loadConfig(this.liveConfiguration.config());
    }


    @Override
    public String messageByPathForMap(String main, String secondary) {
        if (messages.containsKey(main)) {
            if (messages.get(main).containsKey(secondary)) {
                return messages.get(main).get(secondary);
            }
        }
        return "N/A";
    }

    @Override
    public void loadConfig(JsonNode jsonNode) {
        Map<String, Map<String, String>> messages = jsonValue.value(jsonNode, new TypeReference<>() {
        });
        this.messages.putAll(messages);
    }

    @Override
    public void reloadConfig(JsonNode jsonNode) {
        clearConfig();
        loadConfig(jsonNode);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {

    }

    @Override
    public void clearConfig() {
        messages.clear();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        reloadConfig((JsonNode) evt.getNewValue());
    }
}
