package ru.starfarm.mylittlefarm.objects.boosters;

import ru.starfarm.mylittlefarm.objects.income.CultureIncome;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IncomeBoostersSchedule implements BoosterSchedule {


    private final Map<Booster, CultureIncome> activeBoosters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static volatile IncomeBoostersSchedule instance = null;

    private IncomeBoostersSchedule() {
    }

    public void startBoosterSchedule() {
        scheduler.scheduleAtFixedRate(() -> {
            activeBoosters.keySet().parallelStream().forEach(booster -> {
                booster.reduceDuration(1000);

                if (booster.isExpired()) {
                    stopBooster(booster);
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void addBooster(Booster booster, CultureIncome cultureIncome) {
        activeBoosters.put(booster, cultureIncome);
    }

    @Override
    public void stopBooster(Booster booster) {
        booster.stopBooster(activeBoosters.get(booster));
    }

    @Override
    public void stopAllBoosters() {
        activeBoosters.keySet().forEach(this::stopBooster);
    }

    public static IncomeBoostersSchedule instance() {
        synchronized (IncomeBoostersSchedule.class) {
            if (instance == null) {
                instance = new IncomeBoostersSchedule();
            }
            return instance;
        }
    }

}
