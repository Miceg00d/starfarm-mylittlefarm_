package ru.starfarm.mylittlefarm.objects.skills;

import lombok.AllArgsConstructor;
import org.bukkit.Material;

@AllArgsConstructor
public enum Fish {
    SEAWEED(10, "Водоросли", Material.LONG_GRASS),
    STICK(11, "Палка", Material.STICK);

    public int slot;
    public String name;
    public Material material;
}
