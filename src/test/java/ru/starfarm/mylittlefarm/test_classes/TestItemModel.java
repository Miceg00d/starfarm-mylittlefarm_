package ru.starfarm.mylittlefarm.test_classes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestItemModel {

    private String key;
    private FakeJsonItem jsonModel;

    public TestItemModel(
            @JsonProperty("key") String key,
            @JsonProperty("value") FakeJsonItem jsonModel) {
        this.key = key;
        this.jsonModel = jsonModel;
    }

    public String getKey() {
        return key;
    }

    public FakeJsonItem getJsonModel() {
        return jsonModel;
    }

    @Override
    public String toString() {
        return "TestItemModel{" +
                "key='" + key + '\'' +
                ", jsonModel=" + jsonModel +
                '}';
    }
}
