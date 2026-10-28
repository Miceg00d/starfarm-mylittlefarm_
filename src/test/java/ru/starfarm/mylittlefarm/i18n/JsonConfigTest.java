package ru.starfarm.mylittlefarm.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import ru.starfarm.mylittlefarm.i18n.config.LiveConfiguration;
import ru.starfarm.mylittlefarm.test_classes.TestItemModel;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonConfigTest {

    private final JsonValue jsonValue = JsonValue.Base.getInstance();
    private static LiveConfiguration enItemsConfig = new LiveConfiguration.Base(Paths.get("/plugins/MyLittleFarm/l10n/items/items_ru.json"));
    private static LiveConfiguration enMessagesConfig = new LiveConfiguration.Base(Paths.get("/plugins/MyLittleFarm/l10n/messages/messages_en.json"));

    public JsonConfigTest() {
    }

    @Test
    void reading_json_objects_is_correct() {
        Map<String, Map<String, String>> messagesMap = jsonValue.value(enMessagesConfig.config(), new TypeReference<>() {
        });

        String englishExpected = "&5Granny Manya&f: Hello, sweetie! I have a task for you: go and buy 1 wheat! It’s sold by Vasilich, who is located at the back!";
        String tutorialMessages = messagesMap.get("tutorial").get("1");
        assertEquals(englishExpected, tutorialMessages);
    }

    @Test
    void serializable_item_from_json_is_correct() {
        List<TestItemModel> list = jsonValue.value(enItemsConfig.config(), new TypeReference<>() {
        });
        assertEquals("&6Accounting Book", list.get(0).getJsonModel().name());
    }


}

