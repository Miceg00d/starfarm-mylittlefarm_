package ru.starfarm.mylittlefarm.test_classes;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.ToString;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.TestDataConfiguration;
import ru.starfarm.mylittlefarm.objects.boosters.Booster;
import ru.starfarm.mylittlefarm.objects.income.Income;
import ru.starfarm.mylittlefarm.objects.income.IncomeWrapper;
import ru.starfarm.mylittlefarm.utils.livedata.LiveDataWrapper;
import ru.starfarm.mylittlefarm.utils.livedata.Observer;

import java.util.ArrayList;

@ToString
public class TestCultureIncome implements Income {

    private final CultureType cultureType;
    private int cultureCount;
    private final AtomicDouble income = new AtomicDouble();
    private float modifier;
    private final ArrayList<Booster> boosters;
    private int upgradeLevel;
    private int fountainModifier;

    private LiveDataWrapper<Income> liveDataWrapper;

    public TestCultureIncome(CultureType cultureType, int cultureCount, float modifier, ArrayList<Booster> boosters, int upgradeLevel, int fountainModifier) {
        this.cultureType = cultureType;
        this.cultureCount = cultureCount;
        this.modifier = modifier;
        this.boosters = boosters;
        this.upgradeLevel = upgradeLevel;
        this.fountainModifier = fountainModifier;
        this.liveDataWrapper = new IncomeWrapper();
    }

    @Override
    public double income() {
        return this.income.get();
    }

    public void increaseIncome(double income) {
        this.income.set(income() + income);
    }

    public void reduceIncome(double income) {
        this.income.set(income() - income);
    }


    @Override
    public double incomeIncreasedByModifier() {
        double income = cultureIncome() + incomeIncreasedByBoosters();
        return serverModifier() > 0 ? income * this.modifier / 100 : 0;
    }

    @Override
    public double incomeIncreasedByBoosters() {
        return boostersModifier() > 0 ? ((incomeIncreasedByUpgrades() + cultureIncome()) * boostersModifier() - incomeIncreasedByUpgrades()) : 0;
    }

    @Override
    public double incomeIncreasedByUpgrades() {
        double cultureIncome = cultureIncome();
        if (this.upgradeLevel == 0) {
            return 0;
        } else {
            return cultureIncome * this.upgradeLevel / 100;
        }
    }


    public void changeModifier(float modifier) {
        this.modifier = modifier;
        calculateIncome();
    }

    @Override
    public void addBooster(Booster booster) {
        boosters.add(booster);
        calculateIncome();
    }

    @Override
    public void removeBooster(Booster booster) {
        boosters.remove(booster);
        calculateIncome();
    }

    public void setNewUpgradeLevel(int level) {
        this.upgradeLevel = level;
        calculateIncome();
    }

    @Override
    public ArrayList<Booster> boosters() {
        return this.boosters;
    }

    @Override
    public int cultureCount() {
        return this.cultureCount;
    }

    public void changeFountainMultiplier(int newMultiplier) {
        this.fountainModifier = newMultiplier;
        calculateIncome();
    }

    public void increaseCultureCount(int count) {
        this.cultureCount += count;
        calculateIncome();
    }

    public int fountainModifier() {
        return this.fountainModifier > 0 ? this.fountainModifier : 1;
    }

    @Override
    public double generalMoneyAfterOffline(long offlineTimeMillis) {
        double totalMoney = 0;
        double incomeWithoutBoosters = (cultureIncome() + incomeIncreasedByUpgrades()) * fountainModifier();
        for (Booster booster : boosters) {
            totalMoney += (incomeWithoutBoosters * booster.modifier()) * booster.generalTimeInOfflineMillis(offlineTimeMillis);
        }
        setupBoostersAfterOffline(offlineTimeMillis);
        System.out.println("General money after offline: " + (totalMoney + (incomeWithoutBoosters * ((double) offlineTimeMillis / 1000))));
        return totalMoney + (incomeWithoutBoosters * ((double) offlineTimeMillis / 1000));
    }

    private void setupBoostersAfterOffline(long offlineTime) {
        for (Booster booster : boosters) {
            booster.reduceDuration(offlineTime);
        }
        boosters.removeIf(Booster::isExpired);
        calculateIncome();
    }

    @Override
    public double offlineEarning(long offlineTime) {
        double totalMoney = 0;
        double incomeWithoutBoosters = (cultureIncome() + incomeIncreasedByUpgrades()) * fountainModifier();
        for (Booster booster : boosters) {
            totalMoney += (incomeWithoutBoosters * booster.modifier()) * booster.generalTimeInOfflineMillis(offlineTime);
        }
        return totalMoney + (incomeWithoutBoosters * ((double) offlineTime / 1000));
    }


    @Override
    public void calculateIncome() {
        double cultureIncome = cultureIncome();
        double withUpgrade = incomeIncreasedByUpgrades();
        double withBoosters = incomeIncreasedByBoosters();
        double modifierIncome = incomeIncreasedByModifier();
        this.income.set((cultureIncome + withUpgrade + withBoosters + modifierIncome) * fountainModifier());
        liveDataWrapper.update(this);
    }

    @Override
    public Income clone() {
        return null;
    }

    public double serverModifier() {
        return this.modifier;
    }

    public double boostersModifier() {
        double generalBooster = 0;
        for (Booster booster : boosters) {
            generalBooster += booster.modifier();
        }
        return generalBooster;
    }

    public int upgradeLevel() {
        return this.upgradeLevel;
    }

    public double cultureIncome() {
        return TestDataConfiguration.instance().cultures.get(this.cultureType, 1).baseIncome() * this.cultureCount;
    }

    public CultureType cultureType() {
        return this.cultureType;
    }

    public void observe(Observer<Income> observer) {
        liveDataWrapper.liveData().observe(observer);
    }

}
