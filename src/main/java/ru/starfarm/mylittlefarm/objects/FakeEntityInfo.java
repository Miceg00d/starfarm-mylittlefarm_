package ru.starfarm.mylittlefarm.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

@Getter
@RequiredArgsConstructor
@ToString
public class FakeEntityInfo {
    private final String name;
    private final String skin;
    private final Location location;
    private final EntityType entityType;
    private final Creature creature;
}
