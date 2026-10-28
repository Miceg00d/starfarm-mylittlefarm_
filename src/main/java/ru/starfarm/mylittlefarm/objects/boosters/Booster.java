package ru.starfarm.mylittlefarm.objects.boosters;

import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.income.CultureIncome;

import java.util.UUID;

public interface Booster {

    UUID id();

    String playerName();

    double modifier();

    long startedTime();

    long duration();

    long expireTime();

    void reduceDuration(long millis);

    void increaseDuration(long millis);

    void startBooster(CultureIncome cultureIncome);

    void stopBooster(CultureIncome cultureIncome);

    boolean isExpired();

    CultureType cultureType();

    long generalTimeInOfflineMillis(long offlineTime);

}
