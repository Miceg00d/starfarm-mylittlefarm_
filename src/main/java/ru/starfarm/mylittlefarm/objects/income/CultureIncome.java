package ru.starfarm.mylittlefarm.objects.income;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.boosters.Booster;
import ru.starfarm.mylittlefarm.utils.livedata.LiveDataWrapper;
import ru.starfarm.mylittlefarm.utils.livedata.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@ToString
public class CultureIncome implements Income {

    private final CultureType cultureType;
    private int cultureCount;
    private final AtomicDouble income = new AtomicDouble();
    private float serverModifier;
    private ArrayList<Booster> boosters;
    private int upgradeLevel;
    private int fountainModifier;

    private LiveDataWrapper<Income> liveDataWrapper = new IncomeWrapper();

    public CultureIncome(CultureType cultureType, int cultureCount, float serverModifier, ArrayList<Booster> boosters, int upgradeLevel, int fountainModifier) {
        this.cultureType = cultureType;
        this.cultureCount = cultureCount;
        this.serverModifier = serverModifier;
        this.boosters = boosters;
        this.upgradeLevel = upgradeLevel;
        this.fountainModifier = fountainModifier;
        calculateIncome();
    }

    private CultureIncome(CultureIncome original) {
        this.cultureType = original.cultureType;
        this.cultureCount = original.cultureCount;
        this.serverModifier = original.serverModifier;
        this.boosters = new ArrayList<>(original.boosters);
        this.upgradeLevel = original.upgradeLevel;
        this.fountainModifier = original.fountainModifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CultureIncome that = (CultureIncome) o;
        return cultureType == that.cultureType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cultureType);
    }

    @Override
    public double income() {
        return this.income.get();
    }

    @Override
    public double incomeIncreasedByModifier() {
        double income = cultureIncome() + incomeIncreasedByBoosters();
        return income * this.serverModifier / 100;
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


    @Override
    public void addBooster(Booster booster) {
        boosters.add(booster);
        booster.startBooster(this);
        calculateIncome();
    }

    @Override
    public void removeBooster(Booster booster) {
        boosters.remove(booster);
        calculateIncome();
    }

    @Override
    public void calculateIncome() {

        double rbMultiplier = 1.0;
        double cultureIncome = cultureIncome();
        double withUpgrade = incomeIncreasedByUpgrades();
        double withBooster = incomeIncreasedByBoosters();
        double modifierIncome = incomeIncreasedByModifier();

        double totalIncome = (cultureIncome + withBooster + modifierIncome + withUpgrade) * fountainModifier() * rbMultiplier;

        this.income.set(totalIncome);
        liveDataWrapper.update(this);
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
    public double generalMoneyAfterOffline(long offlineTimeMillis) {
        double totalMoney = 0;
        double incomeWithoutBoosters = (cultureIncome() + incomeIncreasedByUpgrades()) * fountainModifier();
        for (Booster booster : boosters) {
            totalMoney += (incomeWithoutBoosters * booster.modifier()) * booster.generalTimeInOfflineMillis(offlineTimeMillis);
        }
        setupBoostersAfterOffline(offlineTimeMillis);
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
    public List<Booster> boosters() {
        return this.boosters;
    }

    @Override
    public int cultureCount() {
        return this.cultureCount;
    }


    public void increaseIncome(double income) {
        this.income.set(income() + income);
    }

    public void reduceIncome(double income) {
        this.income.set(income() - income);
    }

    public int fountainModifier() {
        return this.fountainModifier > 0 ? this.fountainModifier : 1;
    }

    public void changeFountainMultiplier(int newModifier) {
        this.fountainModifier = newModifier;
        calculateIncome();
    }

    public int upgradeLevel() {
        return this.upgradeLevel;
    }

    public void setNewUpgradeLevel(int level) {
        this.upgradeLevel = level;
        calculateIncome();
    }

    public void changeModifier(float serverModifier) {
        this.serverModifier = serverModifier;
        calculateIncome();
    }

    public void increaseCultureCount(int count) {
        this.cultureCount = count;
        calculateIncome();
    }

    public double cultureIncome() {
        return MyLittleFarm.getInstance().getCultureRegistry().getCulture(this.cultureType, 1).baseIncome() * this.cultureCount;
    }

    public float serverModifier() {
        return this.serverModifier;
    }

    public double boostersModifier() {
        return boosters.stream().mapToDouble(Booster::modifier).sum();
    }

    public CultureType cultureType() {
        return this.cultureType;
    }

    @Override
    public CultureIncome clone() {
        return new CultureIncome(this);
    }

    public void observe(Observer<Income> observer) {
        liveDataWrapper.liveData().observe(observer);
    }
}
