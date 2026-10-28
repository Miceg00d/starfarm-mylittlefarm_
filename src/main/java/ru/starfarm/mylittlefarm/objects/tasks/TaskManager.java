package ru.starfarm.mylittlefarm.objects.tasks;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class TaskManager {

    @Getter
    private static final List<DiggerTask> tasks = new LinkedList<>();

    private void add(int slot, int needGrass, int needHay, double bonus) {
        tasks.add(new DiggerTask(slot, needGrass, needHay, bonus));
    }

    public TaskManager() {
        add(0, 500, 5, 5.0);
        add(1, 1000, 10, 10.0);
        add(2, 2500, 20, 20.0);
        add(3, 5000, 30, 30.0);
        add(4, 10000, 40, 40.0);
        add(5, 25000, 50, 50.0);
        add(6, 50000, 60, 60.0);
        add(7, 75000, 75, 75.0);
        add(8, 100000, 100, 100.0);
        add(9, 150000, 125, 500.0);
        add(10, 250000, 175, 1000.0);
        add(11, 500000, 250, 2500.0);
        add(12, 750000, 450, 5000.0);
        add(13, 1000000, 850, 7500.0);
        add(14, 1250000, 1250, 10000.0);
        add(15, 1500000, 1800, 15000.0);
    }
}
