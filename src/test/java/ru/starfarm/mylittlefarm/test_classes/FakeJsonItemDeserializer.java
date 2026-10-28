package ru.starfarm.mylittlefarm.test_classes;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ru.starfarm.mylittlefarm.i18n.JsonValue;
import ru.starfarm.mylittlefarm.i18n.config.LiveConfiguration;
import ru.starfarm.mylittlefarm.utils.UncheckedConversion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FakeJsonItemDeserializer extends StdDeserializer<FakeJsonItem> {
    private final LiveConfiguration commonStrings = new LiveConfiguration.Base(Path.of("/plugins/MyLittleFarm/l10n/common_strings.json"));

    public FakeJsonItemDeserializer() {
        this(null);
    }

    protected FakeJsonItemDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FakeJsonItem deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        ObjectMapper objectMapper = JsonValue.Base.getInstance().objectMapper();

        JsonNode node = ctxt.readTree(p);
        String name = node.get("name").asText();
        List<String> lore = node.get("lore").isTextual() ? List.of(node.get("lore").asText()) : objectMapper.readValue(node.findValue("lore").traverse(), new TypeReference<>() {
        });
        String material = node.get("material").textValue();
        Optional<Integer> data = node.get("data") != null ? Optional.of(node.get("data").asInt()) : Optional.empty();
        Optional<Integer> slot = node.get("slot") != null ? Optional.of(node.get("slot").asInt()) : Optional.empty();
        Optional<Integer> page = node.get("page") != null ? Optional.of(node.get("page").asInt()) : Optional.empty();
        Optional<Integer> price = node.get("price") != null ? Optional.of(node.get("price").asInt()) : Optional.empty();


        if (lore.get(0).startsWith("$ref:")) {
            String keyRef = lore.get(0).substring(5);
            List<?> newLore = JsonValue.Base.getInstance().value(commonStrings.config(), keyRef, List.class);
            List<String> stringList = UncheckedConversion.castList(String.class, newLore);
            return new FakeJsonItem(name, stringList, material, data, slot, page, price);
        } else {
            return new FakeJsonItem(name, lore, material, data, slot, page, price);
        }
    }
}
