package ru.starfarm.mylittlefarm.top.impl;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.mylittlefarm.top.BannerBoard;
import ru.starfarm.mylittlefarm.top.Fragment;

public class FragmentImpl extends EntityItemFrame implements Fragment {
    private final short mapId = Fragment.getNextId();

    private final int column;

    private final int row;

    private final BannerBoard owner;

    private Location location;

    private EnumDirection direction;

    private PacketPlayOutSpawnEntity spawnPacket;

    private PacketPlayOutEntityDestroy destroyPacket;

    private PacketPlayOutEntityMetadata metadataPacket;

    private PacketPlayOutMap mapPacket;

    FragmentImpl(Location location, EnumDirection direction, BannerBoard board, int column, int row) {
        super((World)((CraftWorld)location.getWorld()).getHandle(), new BlockPosition(location
                .getBlockX(), location.getBlockY(), location.getBlockZ()), direction);
        this.owner = board;
        this.column = column;
        this.row = row;
        setInvisible(true);
        setInvulnerable(true);
        setNoGravity(true);
        DataWatcher dataWatcher = getDataWatcher();
        dataWatcher.set(DataWatcherRegistry.f.a(6), CraftItemStack.asNMSCopy(new ItemStack(Material.MAP, 1, this.mapId)));
        dataWatcher.set(DataWatcherRegistry.b.a(7), Integer.valueOf(0));
        this.spawnPacket = new PacketPlayOutSpawnEntity((Entity)this, 71);
        this.destroyPacket = new PacketPlayOutEntityDestroy(new int[] { getId() });
        this.metadataPacket = new PacketPlayOutEntityMetadata(getId(), dataWatcher, true);
    }

    public BannerBoard getOwner() {
        return this.owner;
    }

    public void updateMap(byte[] bytes) {
        int width = this.owner.getRenderer().getBufferedImage().getWidth();
        byte[] buffer = new byte[16384];
        for (int x = this.column * 128; x < this.column * 128 + 128; x++) {
            for (int y = this.row * 128; y < this.row * 128 + 128; y++)
                buffer[(y - this.row * 128) * 128 + x - this.column * 128] = bytes[y * width + x];
        }
        this
                .mapPacket = new PacketPlayOutMap(this.mapId, (byte)3, true, Lists.newArrayList(), buffer, 0, 0, 128, 128);
    }

    public short getMapId() {
        return this.mapId;
    }

    public int getEntityId() {
        return getId();
    }

    public void spawn(Player player) {
        (((CraftPlayer)player).getHandle()).playerConnection.sendPacket((Packet)this.spawnPacket);
        (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(this.metadataPacket);
    }

    public void broadcastSpawn() {
        this.location.getWorld().getPlayers().forEach(this::spawn);
    }

    public void destroy(Player player) {
        (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(this.destroyPacket);
    }

    public void broadcastDestroy() {
        this.location.getWorld().getPlayers().forEach(this::destroy);
    }

    public void update(Player player) {
        (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(this.mapPacket);
    }

    public void broadcastUpdate() {
        Bukkit.getOnlinePlayers().forEach(this::update);
    }

    public Location getLocation() {
        return this.location;
    }

    public void teleport(Location location) {
        this.location = location;
        setPosition(location.getX(), location.getY(), location.getZ());
        this.spawnPacket = new PacketPlayOutSpawnEntity((Entity)this, 71);
    }

    public void setDirection(EnumDirection direction) {
        this.direction = direction;
        super.setDirection(direction);
        this.spawnPacket = new PacketPlayOutSpawnEntity((Entity)this, 71);
    }
}