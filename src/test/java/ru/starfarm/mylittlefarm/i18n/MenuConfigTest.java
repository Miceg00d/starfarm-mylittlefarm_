package ru.starfarm.mylittlefarm.i18n;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.starfarm.mylittlefarm.i18n.config.LanguageConfig;
import ru.starfarm.mylittlefarm.i18n.config.LiveConfiguration;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderType;
import ru.starfarm.mylittlefarm.i18n.menu.JsonMenuModel;
import ru.starfarm.mylittlefarm.test_classes.TestItemModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.*;


public class MenuConfigTest {

    private final FakeMenuConfig enMenuConfig = new FakeMenuConfig.Base("en");

    public MenuConfigTest() {
    }

    @Test
    void menus_correctly_loaded() {
        enMenuConfig.checkMenuTitleByKey("idle", "Offline Earnings");
        enMenuConfig.checkLoadCalledTimes(1);
    }

    @Test
    void item_lore_is_correctly_deserialized_and_updated_with_placeholder() {
        List<String> expectedLore = List.of(
                "§7Click to collect money",
                "§7for the time you were offline",
                "",
                "§7Total earned: §a${afk_income}");
        enMenuConfig.checkItemLore("idle", "wheat", expectedLore);
        enMenuConfig.itemModelByKey("idle", "wheat").getJsonModel().updateLore(Map.of(PlaceholderType.AFK_INCOME, "$1000"));
        List<String> expectedUpdatedLore = List.of(
                "§7Click to collect money",
                "§7for the time you were offline",
                "",
                "§7Total earned: §a$1000");
        enMenuConfig.checkItemLore("idle", "wheat", expectedUpdatedLore);
    }

    @Test
    void item_lore_is_correctly_deserialized_and_updated_without_placeholder() {
        List<String> expectedLore = List.of(
                "§fBuying this gives you the opportunity to improve your",
                "farm, without leaving the barn!",
                "",
                "§fThe expiry date: §3unlimited");
        enMenuConfig.checkItemLore("donate", "book", expectedLore);
        enMenuConfig.itemModelByKey("donate", "book").getJsonModel().updateLore(Map.of());
        enMenuConfig.checkItemLore("donate", "book", expectedLore);
    }


    interface FakeMenuConfig extends LanguageConfig.Menu {

        void checkMenuTitleByKey(String key, String name);

        void checkLoadCalledTimes(int times);

        void checkItemLore(String menuKey, String itemKey, List<String> lore);

        TestItemModel itemModelByKey(String menuKey, String itemKey);

        class Base implements FakeMenuConfig, PropertyChangeListener {

            private final Map<String, FakeMenuModel> jsonMenus = new HashMap<>();
            private final LiveConfiguration liveConfiguration;
            private final JsonValue jsonValue = JsonValue.Base.getInstance();

            private int loadCounter = 0;

            public Base(String language) {
                this.liveConfiguration = new LiveConfiguration.Base(Path.of(String.format("/plugins/MyLittleFarm/l10n/menus/menus_%s.json", language)));
                this.liveConfiguration.addPropertyChangeListener(this);
            }

            @Override
            public TestItemModel itemModelByKey(String menuKey, String itemKey) {
                return jsonMenus.get(menuKey).items.stream()
                        .filter(testItemModel -> testItemModel.getKey().equals(itemKey))
                        .findFirst().orElse(null);
            }

            @Override
            public void checkMenuTitleByKey(String key, String name) {
                FakeMenuModel fakeMenuModel = menuByKey(key);
                String actualTitle = fakeMenuModel.title();
                Assertions.assertEquals(name, actualTitle);
            }

            @Override
            public void checkLoadCalledTimes(int times) {
                Assertions.assertEquals(times, this.loadCounter);
            }

            @Override
            public void checkItemLore(String menuKey, String itemKey, List<String> lore) {
                FakeMenuModel fakeMenuModel = menuByKey(menuKey);
                TestItemModel model = fakeMenuModel.items.stream()
                        .filter(testItemModel -> testItemModel.getKey().equals(itemKey))
                        .findFirst().orElse(null);

                Assertions.assertEquals(lore, model.getJsonModel().lore());
            }

            @Override
            public <T> T menuByKey(String key) {
                FakeMenuModel jsonMenu = jsonMenus.get(key);
                return (T) jsonMenu;
            }

            @Override
            public void sendUpdate(JsonMenuModel oldModel, JsonMenuModel newModel) {

            }

            @Override
            public void loadConfig(JsonNode jsonNode) {
                Map<String, FakeMenuModel> menus = new HashMap<>();
                jsonNode.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    JsonNode value = entry.getValue();
                    menus.put(key, jsonValue.value(value, new TypeReference<>() {
                    }));
                });
                putAll(menus);
                loadCounter++;
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
            public void propertyChange(PropertyChangeEvent evt) {
                reloadConfig((JsonNode) evt.getNewValue());
            }

            private void putAll(Map<String, FakeMenuModel> map) {
                this.jsonMenus.putAll(map);
            }

            @Override
            public void clearConfig() {
                this.jsonMenus.clear();
            }

        }
    }

    static class FakeMenuModel {
        @JsonProperty("title")
        private String title;
        @JsonProperty("rows")
        private int rows;
        @JsonProperty("items")
        private List<TestItemModel> items;
        @JsonProperty("pages")
        private Optional<Integer> pages = Optional.empty();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FakeMenuModel that = (FakeMenuModel) o;
            return Objects.equals(title, that.title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title);
        }

        public String title() {
            return title;
        }

        public int rows() {
            return rows;
        }

        public List<TestItemModel> items() {
            return items;
        }

        public Optional<Integer> pages() {
            return pages;
        }

        @Override
        public String toString() {
            return "JsonMenuModel{" +
                    "title='" + title + '\'' +
                    ", rows=" + rows +
                    ", items=" + items +
                    ", pages=" + pages +
                    '}';
        }
    }
}
