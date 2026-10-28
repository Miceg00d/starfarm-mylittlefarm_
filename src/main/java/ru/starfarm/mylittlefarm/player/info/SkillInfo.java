package ru.starfarm.mylittlefarm.player.info;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class SkillInfo {
    public int level;
    public double exp;
    public Map<String, Integer> other;
}
