package ru.starfarm.mylittlefarm.i18n.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderType;

import java.util.Map;
import java.util.Objects;

@NoArgsConstructor
public class ItemModel {

    private String key;
    private JsonItemModel jsonModel;

    public ItemModel(@JsonProperty("key") String key, @JsonProperty("value") JsonItemModel jsonModel) {
        this.key = key;
        this.jsonModel = jsonModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemModel itemModel = (ItemModel) o;
        return Objects.equals(key, itemModel.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    public ItemStack itemStack(Map<PlaceholderType, String> placeholderValues) {
        return jsonModel.itemStack(key, placeholderValues);
    }

    public String key() {
        return key;
    }

    public JsonItemModel jsonModel() {
        return jsonModel;
    }

    @Override
    public String toString() {
        return "ItemModel{" +
                "key='" + key + '\'' +
                ", jsonModel=" + jsonModel +
                '}';
    }
}
