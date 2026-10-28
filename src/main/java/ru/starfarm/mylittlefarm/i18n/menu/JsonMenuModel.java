package ru.starfarm.mylittlefarm.i18n.menu;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.starfarm.mylittlefarm.i18n.item.ItemModel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JsonMenuModel {

    @JsonProperty("title")
    private String title;
    @JsonProperty("rows")
    private int rows;
    @JsonProperty("items")
    private List<ItemModel> items;
    @JsonProperty("pages")
    private Optional<Integer> pages = Optional.empty();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonMenuModel that = (JsonMenuModel) o;
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

    public List<ItemModel> items() {
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
