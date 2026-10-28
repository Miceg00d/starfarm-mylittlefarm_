package ru.starfarm.mylittlefarm.objects.tasks;

import lombok.Getter;

@Getter
public class HourTask {
    private int id;
    private int slot;
    private int playerHours;
    private int playerMinutes;
    private double bonus;

    public HourTask(int slot, int playerHours, int playerMinutes, double bonus) {
        this.id = HourTaskManager.getTasks().size() + 1;
        this.slot = slot;
        this.playerHours = playerHours;
        this.playerMinutes = playerMinutes;
        this.bonus = bonus;
    }
}