package ru.starfarm.mylittlefarm.objects.boosters;

import ru.starfarm.mylittlefarm.objects.income.CultureIncome;

public interface BoosterSchedule {

    void addBooster(Booster booster, CultureIncome cultureIncome);

    void stopAllBoosters();

    void stopBooster(Booster booster);
}
