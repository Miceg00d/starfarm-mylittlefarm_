package ru.starfarm.mylittlefarm.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.boosters.Booster;
import ru.starfarm.mylittlefarm.objects.boosters.IncomeBooster;
import ru.starfarm.mylittlefarm.objects.cultures.CultureInfo;
import ru.starfarm.mylittlefarm.test_classes.TestCultureIncome;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestDataConfiguration {

    private static volatile TestDataConfiguration instance = null;

    public TestDataConfiguration() {
    }

    public Table<CultureType, Integer, CultureInfo> cultures = HashBasedTable.create();

    public void loadCultures() {
        if (!cultures.isEmpty()) {
            return;
        }
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        Map<String, Object> cultureConfigSection;
        Map<String, Object> cultureLocationSection;

        try {
            cultureConfigSection = yamlMapper.readValue(
                    new File("/Users/user/IdeaProjects/starfarm-mylittlefarm/src/test/resources/config.yml"), Map.class);
            cultureLocationSection = yamlMapper.readValue(
                    new File("/Users/user/IdeaProjects/starfarm-mylittlefarm/src/test/resources/culture_locations.yml"), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load YAML files", e);
        }

        if (cultureConfigSection != null && cultureConfigSection.containsKey("cultures")) {
            Map<String, Object> culturesSection = (Map<String, Object>) cultureConfigSection.get("cultures");
            Map<String, Object> locationsSection = (Map<String, Object>) cultureLocationSection.get("cultures");

            culturesSection.forEach((key, value) -> {
                Map<String, Object> keySection = (Map<String, Object>) value;
                Map<String, Object> locationSection = (Map<String, Object>) locationsSection.get(key);

                if ("FOUNTAIN".equals(key)) {
                    keySection.forEach((i, v) -> {
                        Map<String, Object> cultureSection = (Map<String, Object>) v;
                        Map<String, Object> locations = (Map<String, Object>) locationSection.get(i);
                        List<String> locationList = (List<String>) locations.get("locations");

                        cultures.put(CultureType.valueOf(key), Integer.parseInt(i),
                                new CultureInfo(
                                        ((Number) cultureSection.get("price")).doubleValue(),
                                        ((Number) cultureSection.get("income")).doubleValue(),
                                        locationList
                                ));
                    });
                } else {
                    double basePrice = ((Number) keySection.get("price")).doubleValue();
                    double income = ((Number) keySection.get("income")).doubleValue();
                    double priceMultiplier = ((Number) keySection.get("price_multiplier")).doubleValue();

                    double totalPrice = basePrice;
                    for (int i = 1; i <= 100; i++) {
                        Map<String, Object> locations = (Map<String, Object>) locationSection.get(Integer.toString(i));
                        List<String> locationList = (List<String>) locations.get("locations");

                        cultures.put(CultureType.valueOf(key), i, new CultureInfo(totalPrice, income, locationList));
                        totalPrice *= priceMultiplier;
                    }
                }
            });
        }
    }
    public TestCultureIncome testCultureIncomeWithBoosters(CultureType cultureType, int count, int upgrade, int fountainModifier) {
        return new TestCultureIncome(cultureType, count, 0F, testBoostersList(cultureType.name()), upgrade, fountainModifier);
    }

    public TestCultureIncome testCultureIncomeWithoutBoosters(CultureType cultureType, int count, int upgrade, int fountainModifier) {
        return new TestCultureIncome(cultureType, count, 0F, new ArrayList<>(), upgrade, fountainModifier);
    }

    public Booster testBooster(String culture, double modifier, long durationInMillis) {
        long currentTime = System.currentTimeMillis();
        return new IncomeBooster(UUID.randomUUID(), "Remkun", culture, currentTime, modifier, durationInMillis);
    }

    public ArrayList<Booster> testBoostersList(String culture) {
        ArrayList<Booster> boostersList = new ArrayList<>();
        boostersList.add(testBooster(culture, 2, 10000));
        boostersList.add(testBooster(culture, 2, 20000));
        return boostersList;
    }

    public static TestDataConfiguration instance() {
        synchronized (TestDataConfiguration.class) {
            if (instance == null) {
                instance = new TestDataConfiguration();
            }
            return instance;
        }
    }

}
