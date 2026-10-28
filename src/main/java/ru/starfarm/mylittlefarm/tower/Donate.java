package ru.starfarm.mylittlefarm.tower;

import org.bukkit.entity.Player;
import ru.starfarm.core.realm.IRealmService;
import ru.starfarm.core.tower.ITowerService;
import ru.starfarm.mylittlefarm.tower.packet.PacketDonate;

public class Donate {

    public static void buy(String name, String donate, int cost) {
        ITowerService.getService().getDirectApi().write(
                new PacketDonate(
                IRealmService.get().getRealmInfo().getDisplayName(),
                        name, donate, cost
                ),
                "MTRANSFER"
        );
    }

    public static void buy(Player player, String donate, int cost) {
        buy(player.getName(), donate, cost);
    }
}
