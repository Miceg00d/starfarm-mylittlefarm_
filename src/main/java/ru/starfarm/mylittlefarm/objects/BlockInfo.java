package ru.starfarm.mylittlefarm.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@Getter
@RequiredArgsConstructor
public class BlockInfo {
    private final int id;
    private final byte data;
    private final Location location;
}
