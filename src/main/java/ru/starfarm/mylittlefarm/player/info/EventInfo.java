package ru.starfarm.mylittlefarm.player.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter @Setter
public class EventInfo {
    public int time;
    public List<String> progress;
}
