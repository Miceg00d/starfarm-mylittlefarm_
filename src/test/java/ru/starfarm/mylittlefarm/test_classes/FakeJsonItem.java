package ru.starfarm.mylittlefarm.test_classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.starfarm.mylittlefarm.i18n.item.JsonItem;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderExtractor;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonDeserialize(using = FakeJsonItemDeserializer.class)
public class FakeJsonItem implements JsonItem {

    @JsonProperty("name")
    private String name;
    @JsonProperty("lore")
    private List<String> lore;
    @JsonProperty("material")
    private String material;
    @JsonProperty("data")
    private Optional<Integer> data = Optional.empty();
    @JsonProperty("slot")
    private Optional<Integer> slot = Optional.empty();
    @JsonProperty("page")
    private Optional<Integer> page = Optional.empty();
    @JsonProperty("price")
    private Optional<Integer> price = Optional.empty();

    public FakeJsonItem(String name, List<String> lore, String material, Optional<Integer> data, Optional<Integer> slot, Optional<Integer> page, Optional<Integer> price) {
        this.name = name;
        this.lore = lore;
        this.material = material;
        this.data = data;
        this.slot = slot;
        this.page = page;
        this.price = price;
    }

    public FakeJsonItem() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakeJsonItem that = (FakeJsonItem) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    @JsonProperty("name")
    public String name() {
        return name;
    }

    @Override
    @JsonProperty("lore")
    public List<String> lore() {
        return lore;
    }

    @Override
    @JsonProperty("material")
    public String material() {
        return material;
    }

    public void updateLore(Map<PlaceholderType, String> placeholderValues) {
        List<String> updatedLore = PlaceholderExtractor.replacePlaceholdersInList(this.lore, placeholderValues);
        this.lore = updatedLore;
    }

    @Override
    @JsonProperty("slot")
    public Optional<Integer> slot() {
        return slot;
    }

    @Override
    public int menuSlot(@Nullable Integer slot) {
        if (slot == null) {
            if (this.slot.isEmpty()) {
                throw new NullPointerException(String.format("Item '%s' does not have slot in config and has been set to NULL for menu!", this.name));
            } else return this.slot.get();
        } else return slot;
    }

    @JsonProperty("data")
    @Override
    public Optional<Integer> data() {
        return data;
    }

    @JsonProperty("page")
    @Override
    public Optional<Integer> page() {
        return page;
    }

    @JsonProperty("price")
    @Override
    public Optional<Integer> price() {
        return price;
    }

    @Override
    public String toString() {
        return "TestJsonItemModel{" +
                "name='" + name + '\'' +
                ", lore=" + lore +
                ", material='" + material + '\'' +
                ", data=" + data +
                ", slot=" + slot +
                ", page=" + page +
                ", price=" + price +
                '}';
    }
}
