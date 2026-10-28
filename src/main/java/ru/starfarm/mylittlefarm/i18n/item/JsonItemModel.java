package ru.starfarm.mylittlefarm.i18n.item;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.util.item.ItemUtil;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderExtractor;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonDeserialize(using = JsonItemDeserializer.class)
public class JsonItemModel implements JsonItem {

    private String name;
    private List<String> lore;
    private String material;
    private Optional<Integer> data = Optional.empty();
    private Optional<Integer> slot = Optional.empty();
    private Optional<Integer> page = Optional.empty();
    private Optional<Integer> price = Optional.empty();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonItemModel that = (JsonItemModel) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    public ItemStack itemStack(String NBTTag, Map<PlaceholderType, String> placeholderValues) {
        int data = this.data.orElse(0);
        ItemStack item = ApiManager.newItemBuilder(Material.valueOf(material.toUpperCase()), (short) data)
                .name(this.name)
                .lore(updatedLore(placeholderValues))
                .build();
        return ItemUtil.setTag(item, "mlf_item", NBTTag);
    }

    public List<String> updatedLore(Map<PlaceholderType, String> placeholderValues) {
        return PlaceholderExtractor.replacePlaceholdersInList(this.lore, placeholderValues);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<String> lore() {
        return lore;
    }

    @Override
    public String material() {
        return material;
    }

    @Override
    public Optional<Integer> slot() {
        return slot;
    }

    @Override
    public int menuSlot(@Nullable Integer slot) {
        if (slot == null) {
            if (this.slot.isEmpty()) {
                throw new IllegalArgumentException(String.format("Item '%s' does not have slot in config and has been set to NULL for menu!", this.name));
            } else return this.slot.get();
        } else return slot;
    }

    @Override
    public Optional<Integer> data() {
        return data;
    }

    @Override
    public Optional<Integer> page() {
        return page;
    }

    @Override
    public Optional<Integer> price() {
        return price;
    }
}
