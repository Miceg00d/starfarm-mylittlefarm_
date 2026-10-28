package ru.starfarm.mylittlefarm.objects.income;

import ru.starfarm.mylittlefarm.objects.boosters.Booster;

import java.util.List;

public interface Income {

    double income();

    double incomeIncreasedByModifier();

    double incomeIncreasedByBoosters();

    double incomeIncreasedByUpgrades();

    void addBooster(Booster booster);

    void removeBooster(Booster booster);

    List<Booster> boosters();

    int cultureCount();

    double generalMoneyAfterOffline(long offlineTime);

    void calculateIncome();

    Income clone();

    double offlineEarning(long offlineTime);
}
