package ru.starfarm.mylittlefarm.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@UtilityClass
public class LocationUtils {

    public Location fromString(String in) {
        String[] info = in.split(" ");

        if (info.length == 4)
            return new Location(Bukkit.getWorld(info[0]), Double.parseDouble(info[1]), Double.parseDouble(info[2]), Double.parseDouble(info[3]));
        else if (info.length == 6)
            return new Location(Bukkit.getWorld(info[0]), Double.parseDouble(info[1]), Double.parseDouble(info[2]), Double.parseDouble(info[3]), (float) Double.parseDouble(info[4]), (float) Double.parseDouble(info[5]));
        throw new IllegalArgumentException("cannot parse location");
    }

    public boolean equals(Location first, Location second) {
        if (first.getBlockX() == second.getBlockX() && first.getBlockY() == second.getBlockY() && first.getBlockZ() == second.getBlockZ()) {
            return true;
        }
        return false;
    }
    
    public String toString(Location location) {
        return location.getWorld().getName() + " " + 
               location.getX() + " " + 
               location.getY() + " " + 
               location.getZ() + " " + 
               location.getYaw() + " " + 
               location.getPitch();
    }

}
