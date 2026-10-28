package ru.starfarm.mylittlefarm.objects.booster;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.TestDataConfiguration;
import ru.starfarm.mylittlefarm.objects.boosters.IncomeBooster;
import ru.starfarm.mylittlefarm.test_classes.TestCultureIncome;
import ru.starfarm.mylittlefarm.utils.config.ExclusionsStrategies;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoosterTest {

    private static final TestDataConfiguration testData = TestDataConfiguration.instance();

    @BeforeAll
    static void loadCultures() {
        testData.loadCultures();
    }


    @Test
    void booster_correctly_serialized_and_compared_test() {
        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionsStrategies()).create();

        Type type = (new TypeToken<List<IncomeBooster>>() {
        }).getType();

        List<IncomeBooster> boosters = new ArrayList<>();
        boosters.add(new IncomeBooster(UUID.randomUUID(), "Remkun", "Wheat", System.currentTimeMillis(), 1.5, TimeUnit.HOURS.toMillis(1)));
        boosters.add(new IncomeBooster(UUID.randomUUID(), "Remkun", "Wheat", System.currentTimeMillis(), 1.5, TimeUnit.MINUTES.toMillis(1)));

        String json = gson.toJson(boosters);
        System.out.println("Serialized JSON: " + json);

        List<IncomeBooster> deserializedBooster = gson.fromJson(json, type);
        assertEquals(boosters.get(0), deserializedBooster.get(0));
        assertEquals(boosters.get(1), deserializedBooster.get(1));
    }

    @Test
    void booster_increase_income_test() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithoutBoosters(CultureType.POTATO, 60, 3500, 1);
        testCultureIncome.calculateIncome();
        double incomeWithoutBooster = testCultureIncome.income();

        testCultureIncome.addBooster(new IncomeBooster(UUID.randomUUID(), "Remkun", "Potato", System.currentTimeMillis(), 1.5, TimeUnit.SECONDS.toMillis(5)));
        testCultureIncome.addBooster(new IncomeBooster(UUID.randomUUID(), "Remkun", "Potato", System.currentTimeMillis(), 2, TimeUnit.SECONDS.toMillis(5)));

        double boosterIncome = testCultureIncome.income() - testCultureIncome.cultureIncome();
        assertEquals(3.5, (boosterIncome) / incomeWithoutBooster);
    }


}