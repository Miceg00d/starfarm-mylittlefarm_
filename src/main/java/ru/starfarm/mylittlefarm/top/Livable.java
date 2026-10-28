package ru.starfarm.mylittlefarm.top;

import net.minecraft.server.v1_12_R1.EnumDirection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Livable {
    void spawn(Player paramPlayer);

    void broadcastSpawn();

    void destroy(Player paramPlayer);

    void broadcastDestroy();

    void update(Player paramPlayer);

    void broadcastUpdate();

    Location getLocation();

    void teleport(Location paramLocation);

    EnumDirection getDirection();

    void setDirection(EnumDirection paramEnumDirection);
}

