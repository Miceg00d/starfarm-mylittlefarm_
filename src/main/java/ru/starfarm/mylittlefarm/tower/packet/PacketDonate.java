package ru.starfarm.mylittlefarm.tower.packet;

import lombok.AllArgsConstructor;
import ru.starfarm.core.tower.direct.DirectPacket;

@DirectPacket.Id(id = 3)
@AllArgsConstructor
public class PacketDonate implements DirectPacket {

    public String realm;
    public String name;
    public String donate;
    public int cost;

}