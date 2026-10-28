package ru.starfarm.mylittlefarm.objects.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class Require {
    private final double price;
    private final int needLevel;
    public int weight;

    public static Require of(double price, int needLevel) {
        return new Require(price, needLevel);
    }

    public static Require of(double price, int needLevel, int weight) {
        return new Require(price, needLevel, weight);
    }
}
