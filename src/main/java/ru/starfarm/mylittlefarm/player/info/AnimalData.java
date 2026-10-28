package ru.starfarm.mylittlefarm.player.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class AnimalData {
    public int count;
    public long lastUpdate;
    public int secondsLeft;
}
