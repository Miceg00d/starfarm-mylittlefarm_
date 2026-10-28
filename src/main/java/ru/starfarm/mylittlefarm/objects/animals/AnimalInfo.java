package ru.starfarm.mylittlefarm.objects.animals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.EntityType;

@AllArgsConstructor
@Getter
public class AnimalInfo {
    private final double price;
    private final EntityType entityType;
}
