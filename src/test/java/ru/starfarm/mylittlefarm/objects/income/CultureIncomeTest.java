package ru.starfarm.mylittlefarm.objects.income;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.TestDataConfiguration;
import ru.starfarm.mylittlefarm.objects.boosters.Booster;
import ru.starfarm.mylittlefarm.test_classes.TestCultureIncome;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class CultureIncomeTest {

    private static final TestDataConfiguration testData = TestDataConfiguration.instance();

    @BeforeAll
    static void loadCultures() {
        testData.loadCultures();
    }

    @Test
    void first_culture_income_test() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithoutBoosters(CultureType.WHEAT, 1, 0, 1);
        testCultureIncome.calculateIncome();

        double expected = testData.cultures.get(CultureType.WHEAT, 1).baseIncome();
        double actual = testCultureIncome.income();
        assertEquals(expected, actual);
    }

    @Test
    void first_culture_with_first_upgrade() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithoutBoosters(CultureType.WHEAT, 3, 50, 1);
        testCultureIncome.calculateIncome();

        double baseIncome = testData.cultures.get(CultureType.WHEAT, 1).baseIncome() * testCultureIncome.cultureCount();
        double expected = baseIncome + (baseIncome * testCultureIncome.upgradeLevel() / 100);
        double actual = testCultureIncome.income();
        assertEquals(expected, actual);
    }

    /**
     * Проверяет корректность расчета базового дохода культуры.
     * Ожидаемый результат — это доход, который культура генерирует без учета апгрейдов и бустеров.
     */
    @Test
    void base_culture_income_is_correct() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithBoosters(CultureType.POTATO, 60, 3500, 2);

        double expected = testData.cultures.get(CultureType.POTATO, 1).baseIncome() * testCultureIncome.cultureCount();
        double actual = testCultureIncome.cultureIncome();
        assertEquals(expected, actual);
    }

    /**
     * Проверяет правильность увеличения дохода культуры при использовании улучшений.
     * Ожидаемый результат — это базовый доход, увеличенный в зависимости от уровня улучшения.
     */
    @Test
    void income_is_correctly_increased_by_upgrade() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithBoosters(CultureType.POTATO, 60, 3500, 2);
        double baseIncome = testCultureIncome.cultureIncome();

        double actual = testCultureIncome.incomeIncreasedByUpgrades();
        double expected = (baseIncome * 3500 / 100);
        assertEquals(expected, actual);
    }

    /**
     * Проверяет правильность расчета дохода культуры с учетом апгрейдов и бустеров.
     * Ожидаемый результат — это доход, увеличенный как улучшениями, так и активными бустерами.
     */
    @Test
    void income_is_correctly_increased_by_upgrade_and_boosters() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithBoosters(CultureType.POTATO, 60, 3500, 2);
        testCultureIncome.calculateIncome();

        double baseIncome = testCultureIncome.cultureIncome();

        double withUpgrade = baseIncome + testCultureIncome.incomeIncreasedByUpgrades();
        double actual = testCultureIncome.incomeIncreasedByBoosters() + testCultureIncome.incomeIncreasedByUpgrades();
        double expected = withUpgrade * testCultureIncome.boostersModifier();
        assertEquals(expected, actual);
    }

    @Test
    void income_is_correctly_increased_by_upgrade_and_zero_booster_modifier() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithoutBoosters(CultureType.POTATO, 60, 3500, 2);
        double baseIncome = testCultureIncome.cultureIncome();

        double actual = testCultureIncome.incomeIncreasedByBoosters();
        double expected = ((baseIncome + testCultureIncome.incomeIncreasedByUpgrades()) * testCultureIncome.boostersModifier());
        assertEquals(expected, actual);
    }

    /**
     * Проверяет корректность расчета дохода культуры с учетом модификатора сервера.
     * Ожидаемый результат — это доход, учитывающий апгрейды, бустеры и общий модификатор сервера.
     */
    @Test
    void income_is_correctly_increased_by_modifier() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithBoosters(CultureType.POTATO, 60, 3500, 2);
        double totalIncome = (testCultureIncome.cultureIncome() + testCultureIncome.incomeIncreasedByBoosters());

        double expected = (totalIncome / 100 * testCultureIncome.serverModifier());
        double actual = testCultureIncome.incomeIncreasedByModifier();
        assertEquals(expected, actual);
    }

    /**
     * Проверяет корректность полного расчета дохода культуры с учетом всех модификаторов.
     * Ожидаемый результат = базовый доход + доход с апгрейдами и бустерами, увеличенный на серверный коэффициент и умноженный на коэффициент фонтана.
     */
    @Test
    void income_is_correctly_calculated() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithBoosters(CultureType.POTATO, 60, 3500, 2);
        testCultureIncome.calculateIncome();

        double baseIncome = testCultureIncome.cultureIncome();
        double upgradeIncome = testCultureIncome.incomeIncreasedByUpgrades();
        double boosterIncome = testCultureIncome.incomeIncreasedByBoosters();
        double modifier = testCultureIncome.serverModifier();
        int fountainModifier = testCultureIncome.fountainModifier();

        double withoutModifiers = baseIncome + upgradeIncome + boosterIncome;

        double expected = (withoutModifiers + (withoutModifiers / 100 * modifier)) * fountainModifier;
        double actual = testCultureIncome.income();
        assertEquals(expected, actual);
    }

    /**
     * Проверяет корректность полного расчета дохода культуры с учетом всех модификаторов без активных бустеров.
     * Ожидаемый результат = базовый доход + доход с апгрейдами и бустерами, увеличенный на серверный коэффициент и умноженный на коэффициент фонтана.
     */
    @Test
    void income_is_correctly_calculated_without_boosters() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithoutBoosters(CultureType.POTATO, 60, 3500, 2);
        testCultureIncome.calculateIncome();

        double baseIncome = testCultureIncome.cultureIncome();
        double upgradeIncome = testCultureIncome.incomeIncreasedByUpgrades();
        double boosterIncome = testCultureIncome.incomeIncreasedByBoosters();
        double modifier = testCultureIncome.serverModifier();
        int fountainModifier = testCultureIncome.fountainModifier();

        double withoutModifiers = baseIncome + upgradeIncome + boosterIncome;

        double expected = (withoutModifiers + (withoutModifiers / 100 * modifier)) * fountainModifier;
        double actual = testCultureIncome.income();

        assertEquals(0, testCultureIncome.boostersModifier());
        assertEquals(expected, actual);
    }

    /**
     * Проверяет корректность расчёта общего кол-ва монет за время, которое игрок отсутствовал в игре
     */
    @Test
    void general_money_after_offline_without_boosters_is_correct() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithoutBoosters(CultureType.WHEAT, 10, 0, 0);

        double actual = testCultureIncome.generalMoneyAfterOffline(10_000);
        double expected = testCultureIncome.cultureIncome() * 10;

        assertEquals(expected, actual);
    }

    /**
     * Проверяет корректность расчёта общего кол-ва монет за время, которое игрок отсутствовал в игре
     */
    @Test
    void general_money_after_offline_with_boosters_is_correct() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithBoosters(CultureType.WHEAT, 10, 0, 0);

        //25 - общее время оффлайна, 2 - модификатор бустера, 10 и 20 - длительность бустеров в момент выхода игрока

        double expected = (testCultureIncome.cultureIncome() * 25) + (testCultureIncome.cultureIncome() * 2 * 10) + (testCultureIncome.cultureIncome() * 2 * 20);
        double actual = testCultureIncome.generalMoneyAfterOffline(25_000);
        // Проверяем что после расчёта монет бустеры были удалены из дохода культуры
        assertTrue(testCultureIncome.boosters().isEmpty());
        assertEquals(expected, actual);

    }

    /**
     * Проверяет корректность расчёта общего кол-ва монет за время, которое игрок отсутствовал в игре
     */
    @Test
    void general_money_after_offline_with_boosters_and_upgrade_is_correct() {
        TestCultureIncome testCultureIncome = testData.testCultureIncomeWithBoosters(CultureType.WHEAT, 25, 500, 0);
        final ArrayList<Booster> boosters = new ArrayList<>(testCultureIncome.boosters());

        // 6 - модификатор от улучшения, 15 - общее время оффлайна, 2 - модификатор бустера, 10 и 15 - оставшаяся длительность бустеров после выхода игрока
        double expected = (testCultureIncome.cultureIncome() * 6 * 15) + (testCultureIncome.cultureIncome() * 6 * 2 * 10) + (testCultureIncome.cultureIncome() * 6 * 2 * 15);
        double actual = testCultureIncome.generalMoneyAfterOffline(15_000);

        // Проверяем что после расчёта монет первый бустер был удален из дохода культуры, и что у второго длительность снизилась до 5 секунд.
        assertFalse(testCultureIncome.boosters().stream().anyMatch(booster -> booster.equals(boosters.get(0))));
        assertEquals(5000, testCultureIncome.boosters().get(0).duration());
        assertEquals(expected, actual);
    }
}


