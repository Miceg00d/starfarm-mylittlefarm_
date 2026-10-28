package ru.starfarm.mylittlefarm.tower;

import ru.starfarm.core.tower.ITowerService;
import ru.starfarm.mylittlefarm.tower.packet.PacketDonate;
public class PacketHandler {

    public PacketHandler() {
        ITowerService.getService().getDirectApi().registerPacket(PacketDonate.class);
    }
}
