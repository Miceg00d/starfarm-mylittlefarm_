package ru.starfarm.mylittlefarm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    DROUGHT("§6§lЗасуха", true),
    DIRTY("§3§lГрязнуля", true),
    BLOOM("§d§lЦветение", false),
    MORNING_DEW("§b§lУтренняя роса", false);

    private final String name;
    private final boolean negative;
}