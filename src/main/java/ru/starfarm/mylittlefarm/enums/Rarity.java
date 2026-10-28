package ru.starfarm.mylittlefarm.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Rarity {

    COMMON("",1.0),
    RARE("",0.6),
    LEGENDARY("",0.1),
    ;

    String colored;
    double chance;
}
