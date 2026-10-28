package ru.starfarm.mylittlefarm.objects.tasks;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class HourTaskManager {

    @Getter
    private static final List<HourTask> tasks = new LinkedList<>();

    public HourTaskManager() {
        add(21, 1, 0, 0.2);
    }

    private void add(int slot, int playerHours, int playerMinutes, double bonus) {
        tasks.add(new HourTask(slot, playerHours, playerMinutes, bonus));
    }
}