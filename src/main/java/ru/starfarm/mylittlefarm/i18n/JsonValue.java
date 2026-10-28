package ru.starfarm.mylittlefarm.i18n;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.File;
import java.io.IOException;

public interface JsonValue {

    ObjectMapper objectMapper();

    JsonNode rootNode(File file);

    <T> T value(String jsonFilePath, String valuePath, Class<? extends T> clazz);

    <T> T value(JsonNode rootNode, String valuePath, Class<? extends T> clazz);

    <T> T value(JsonNode rootNode, TypeReference<T> typeReference);

    <T> T convertValue(JsonNode rootNode, TypeReference<T> typeReference);


    class Base implements JsonValue {

        private static volatile Base instance = null;

        private final ObjectMapper objectMapper;

        private Base() {
            this.objectMapper = new ObjectMapper()
                    .registerModule(new Jdk8Module());
        }

        public static Base getInstance() {
            synchronized (Base.class) {
                if (instance == null) {
                    instance = new Base();
                }
                return instance;
            }
        }

        @Override
        public ObjectMapper objectMapper() {
            return this.objectMapper;
        }

        @Override
        public <T> T value(String jsonFilePath, String valuePath, Class<? extends T> clazz) {
            JsonNode rootNode = rootNode(new File(jsonFilePath));
            JsonNode valueNode = rootNode.at(valuePath);
            return readValue(valueNode, valuePath, clazz);
        }

        @Override
        public <T> T value(JsonNode rootNode, String valuePath, Class<? extends T> clazz) {
            JsonNode valueNode = rootNode.at(valuePath);
            return readValue(valueNode, valuePath, clazz);
        }

        @Override
        public <T> T value(JsonNode rootNode, TypeReference<T> typeReference) {
            try {
                return objectMapper.readValue(rootNode.traverse(), typeReference);
            } catch (IOException e) {
                throw new RuntimeException("Error while read json value with TypeReference: " + typeReference.getType().getTypeName() + e);
            }
        }

        @Override
        public <T> T convertValue(JsonNode rootNode, TypeReference<T> typeReference) {
            try {
                return objectMapper.convertValue(rootNode, typeReference);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Error while convert json value from: " + rootNode.toString() + "for type: " + typeReference.getType().getTypeName());
            }
        }

        private <T> T readValue(JsonNode jsonNode, String valuePath, Class<T> clazz) {
            if (jsonNode.isMissingNode()) {
                throw new IllegalArgumentException("Value not found at the specified path: " + valuePath);
            }
            try {
                return objectMapper.treeToValue(jsonNode, clazz);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Incorrect type of returned data is specified: " + valuePath);
            }
        }

        @Override
        public JsonNode rootNode(File file) {
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(file);
            } catch (IOException e) {
                throw new RuntimeException("Error while read json file");
            }
            return rootNode;
        }
    }
}
