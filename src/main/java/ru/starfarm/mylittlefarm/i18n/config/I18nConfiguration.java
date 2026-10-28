package ru.starfarm.mylittlefarm.i18n.config;

import com.fasterxml.jackson.databind.JsonNode;

public interface I18nConfiguration {


    void loadConfig();

    void reloadConfig();

    JsonNode config();
}
