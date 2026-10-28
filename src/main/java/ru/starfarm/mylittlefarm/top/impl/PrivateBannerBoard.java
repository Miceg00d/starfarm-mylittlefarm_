package ru.starfarm.mylittlefarm.top.impl;


import net.minecraft.server.v1_12_R1.EnumDirection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PrivateBannerBoard extends SimpleBannerBoard {
    private final Player viewer;

    public PrivateBannerBoard(Location location, EnumDirection direction, int columns, int rows, Player viewer) {
        super(location, direction, columns, rows);
        this.viewer = viewer;
    }

    public boolean canReceive(Player player) {
        return (player == this.viewer);
    }
}
