package ru.starfarm.mylittlefarm.objects.movement;

import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.util.Vector;
import ru.starfarm.core.entity.FakeEntity;

@UtilityClass
public class MovementUtils {

    public boolean isEmpty(Block block) {
        return !block.getType().isSolid() && !block.isLiquid();
    }

    public boolean isSlab(Block block) {
        Material type = block.getType();
        return type == Material.STEP || type.name().contains("SLAB");
    }

    public boolean isOnSlab(FakeEntity entity) {
        Block entityBlock = entity.getLocation().getBlock();
        return isSlab(entityBlock) && entityBlock.getY() + 0.5 - entity.getLocation().getY() <= 0.04;
    }

    public boolean isOnGround(FakeEntity entity) {
        Location location = entity.getLocation();
        Block underBlock = location.clone().add(0, -1, 0).getBlock();
        return (underBlock.getType() != Material.AIR) && location.getY() % 1 == 0;
    }

    public boolean isInGround(FakeEntity entity) {
        Location location = entity.getLocation();
        Block entityBlock = location.getBlock();
        return !isEmpty(entityBlock) && !isOnSlab(entity);
    }

    public double calcToUpGround(FakeEntity entity) {
        Location location = entity.getLocation();
        Block entityBlock = location.getBlock();
        if (isSlab(entityBlock))
            return entityBlock.getY() + 0.5 - location.getY();
        return entityBlock.getY() + 1 - location.getY() + 0.04;
    }

    public double calcToDownGround(FakeEntity entity) {
        Location location = entity.getLocation();
        Block under = location.clone().add(0, -1, 0).getBlock();
        double toGround = under.getType() != Material.AIR
                ? location.getY() % 1 == 0 ? 0 : location.getY() - under.getY() - 1 : location.getY() - 2;
        return toGround;
    }

    public void move(Vector vector, FakeEntity entity) {
        PacketPlayOutEntity.PacketPlayOutRelEntityMove packet = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                entity.getId(),
                (byte) (vector.getX() * 4096),
                (byte) (vector.getY() * 4096),
                (byte) (vector.getZ() * 4096),
                false
        );
        entity.getViewers().forEach(uuid -> ((CraftPlayer) Bukkit.getPlayer(uuid)).getHandle().playerConnection.sendPacket(packet));
        entity.getLocation().add(vector);
    }

    public void look(Location location, FakeEntity entity) {
        Location loc = entity.getLocation().clone().setDirection(location.clone().subtract(entity.getLocation()).toVector());
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();
        entity.look(yaw, pitch);
    }

    public double calcMove(double i, double s) {
        double speed = i > 0 ? s : s * -1;
        double pi = i < 0 ? i * -1 : i;
        return pi - s < 0 ? i : speed;
    }

    public Movement.MovementBuilder movement() {
        return new Movement.MovementBuilder();
    }

}
