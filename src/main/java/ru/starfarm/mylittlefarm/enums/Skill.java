package ru.starfarm.mylittlefarm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Skill {
    FISHING("Рыболовство");

    private final String name;
}
