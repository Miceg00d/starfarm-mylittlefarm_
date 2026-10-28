package ru.starfarm.mylittlefarm.top;

import net.minecraft.server.v1_12_R1.PacketPlayInUseEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public interface BannerBoard extends Livable {
    Fragment[][] getFragments();

    List<Fragment> getFragmentsAsList();

    List<Integer> getFragmentsEntityIds();

    Set<Player> getRecipients();

    boolean canReceive(Player paramPlayer);

    BoardRenderer getRenderer();

    void updateFragments();

    void createFragment(Location paramLocation, int paramInt1, int paramInt2);

    void setInteractListener(BiConsumer<PacketPlayInUseEntity.EnumEntityUseAction, Player> paramBiConsumer);

    BiConsumer<PacketPlayInUseEntity.EnumEntityUseAction, Player> getInteractListener();
}
