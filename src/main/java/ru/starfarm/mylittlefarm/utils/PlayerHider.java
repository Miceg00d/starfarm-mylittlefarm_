package ru.starfarm.mylittlefarm.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.mylittlefarm.MyLittleFarm;

@UtilityClass
public class PlayerHider {

    public void hidePlayer(Player player, @NotNull Player target) {
        player.hidePlayer(MyLittleFarm.getInstance(), target);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) target).getHandle())
        );
    }

    public void showPlayer(Player player, @NotNull Player target) {
        player.showPlayer(MyLittleFarm.getInstance(), target);
    }
}
