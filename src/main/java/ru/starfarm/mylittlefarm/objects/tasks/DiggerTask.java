package ru.starfarm.mylittlefarm.objects.tasks;

import lombok.Getter;

@Getter
public class DiggerTask {

    private int id;
    private int slot;
    private int needGrass;
    private int needHay;
    private double bonus;

    public DiggerTask(int slot, int needGrass, int needHay, double bonus) {
        this.id = TaskManager.getTasks().size() + 1;
        this.slot = slot;
        this.needGrass = needGrass;
        this.needHay = needHay;
        this.bonus = bonus;
    }
}

