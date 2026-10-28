package ru.starfarm.mylittlefarm.objects.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Hook {
    TEST("");

    public String name;

    public static Hook getHookById(int id) {
        return values()[id];
    }
}
