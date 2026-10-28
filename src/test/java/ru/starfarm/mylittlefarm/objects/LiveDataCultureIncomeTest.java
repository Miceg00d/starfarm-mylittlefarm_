package ru.starfarm.mylittlefarm.objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.income.Income;
import ru.starfarm.mylittlefarm.test_classes.TestCultureIncome;
import ru.starfarm.mylittlefarm.utils.livedata.Observer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LiveDataCultureIncomeTest {

    private static final TestDataConfiguration testData = TestDataConfiguration.instance();
    private TestObserver testObserver;

    @BeforeAll
    static void loadCultures() {
        testData.loadCultures();
    }

    @BeforeEach
    void setupObserver() {
        testObserver = new TestObserver();
    }

    @Test
    void income_is_changed_after_update_live_data() {
        testObserver.checkIncome(0);
        testObserver.buyCulture(CultureType.WHEAT, 1);
        testObserver.checkIncome(0.25);
        testObserver.buyCulture(CultureType.WHEAT, 3);
        testObserver.checkIncome(1);
    }

    @Test
    void income_is_changed_after_buy_cultures_and_upgrades() {
        testObserver.checkIncome(0);
        testObserver.buyCulture(CultureType.POTATO, 1);
        testObserver.checkIncome(2.5);
        testObserver.buyCulture(CultureType.POTATO, 9);
        testObserver.checkIncome(25);

        testObserver.buyUpgradeForCulture(CultureType.POTATO, 100);
        testObserver.checkIncome(50);
    }

    public class TestObserver implements Observer<Income> {

        private double income = 0;
        private Set<TestCultureIncome> cultureIncomeSet = new HashSet<>();


        public TestObserver() {
        }

        private TestCultureIncome getCultureIncome(CultureType cultureType) {
            return cultureIncomeSet.stream().filter(ci -> ci.cultureType().equals(cultureType)).findFirst().orElse(null);
        }

        public void buyUpgradeForCulture(CultureType cultureType, int upgradeModifier) {
            getCultureIncome(cultureType).setNewUpgradeLevel(upgradeModifier);
        }

        public void checkIncome(double income) {
            assertEquals(income, this.income);
        }

        public void buyCulture(CultureType cultureType, int level) {
            if (getCultureIncome(cultureType) == null) {
                TestCultureIncome newCultureIncome = testData.testCultureIncomeWithoutBoosters(cultureType, level, 0, 0);
                addCultureIncome(newCultureIncome);
                newCultureIncome.calculateIncome();
            } else {
                getCultureIncome(cultureType).increaseCultureCount(level);
            }
        }

        private void calculateIncome() {
            AtomicReference<Double> income = new AtomicReference<>((double) 0);
            cultureIncomeSet.forEach(cultureIncome -> income.updateAndGet(i -> i + cultureIncome.income()));
            this.income = income.get();
        }

        private void addCultureIncome(TestCultureIncome testCultureIncome) {
            cultureIncomeSet.remove(testCultureIncome);
            cultureIncomeSet.add(testCultureIncome);
            testCultureIncome.observe(this);
        }


        @Override
        public void onChanged(Income testCultureIncome) {
            calculateIncome();
        }
    }
}


