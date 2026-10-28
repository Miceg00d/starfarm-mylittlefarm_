package ru.starfarm.mylittlefarm.top;

import java.util.concurrent.atomic.AtomicInteger;

public interface Fragment extends Livable {
    public static final AtomicInteger nextId = new AtomicInteger(32767);

    static short getNextId() {
        return (short)nextId.getAndDecrement();
    }

    BannerBoard getOwner();

    void updateMap(byte[] paramArrayOfbyte);

    short getMapId();

    int getEntityId();
}
