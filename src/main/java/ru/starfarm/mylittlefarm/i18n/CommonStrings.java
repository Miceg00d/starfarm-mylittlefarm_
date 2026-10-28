package ru.starfarm.mylittlefarm.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import ru.starfarm.mylittlefarm.i18n.config.LanguageConfig;
import ru.starfarm.mylittlefarm.i18n.config.LiveConfiguration;
import ru.starfarm.mylittlefarm.utils.UncheckedConversion;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.List;

public class CommonStrings implements LanguageConfig, PropertyChangeListener {

    private JsonNode config;
    private static volatile CommonStrings instance = null;
    private final LiveConfiguration liveConfiguration;

    private CommonStrings() {
        this.liveConfiguration = new LiveConfiguration.Base(Path.of("/plugins/MyLittleFarm/l10n/common_strings.json"));
        this.liveConfiguration.addPropertyChangeListener(this);
        loadConfig(this.liveConfiguration.config());
    }

    public List<String> value(String key) {
        List<?> strings = JsonValue.Base.getInstance().value(this.config, key, List.class);
        return UncheckedConversion.castList(String.class, strings);
    }

    public static CommonStrings getInstance() {
        synchronized (CommonStrings.class) {
            if (instance == null) {
                instance = new CommonStrings();
            }
            return instance;
        }
    }

    @Override
    public void loadConfig(JsonNode jsonNode) {
        this.config = jsonNode;
    }

    @Override
    public void reloadConfig(JsonNode jsonNode) {
        this.config = jsonNode;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.liveConfiguration.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.liveConfiguration.removePropertyChangeListener(pcl);
    }

    @Override
    public void clearConfig() {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        reloadConfig((JsonNode) evt.getNewValue());
    }
}
