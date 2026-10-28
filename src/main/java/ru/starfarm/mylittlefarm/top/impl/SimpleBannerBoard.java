package ru.starfarm.mylittlefarm.top.impl;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.server.v1_12_R1.EnumDirection;
import net.minecraft.server.v1_12_R1.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;
import org.bukkit.plugin.Plugin;
import ru.starfarm.mylittlefarm.top.BannerBoard;
import ru.starfarm.mylittlefarm.top.BoardAPI;
import ru.starfarm.mylittlefarm.top.BoardRenderer;
import ru.starfarm.mylittlefarm.top.Fragment;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SimpleBannerBoard implements BannerBoard {
    private final Fragment[][] fragments;

    private final BoardRenderer renderer;

    private final int rows;

    private final int columns;

    private Location location;

    private EnumDirection direction;

    private BiConsumer<PacketPlayInUseEntity.EnumEntityUseAction, Player> interactListener;

    private final Set<Player> recipients;

    public SimpleBannerBoard(Location location, EnumDirection direction, int columns, int rows) {
        if (columns == 0 || rows == 0)
            throw new IllegalArgumentException("Rows or Columns must > 0");
        this.columns = columns;
        this.rows = rows;
        this.direction = direction;
        this.location = location;
        this.fragments = new Fragment[rows][columns];
        this.renderer = new BoardRenderer(new BufferedImage(columns * 128, rows * 128, 6));
        this.recipients = (Set<Player>)new ConcurrentSet();
        teleport(location);
        updateFragments();
    }

    public void updateFragments() {
        Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin) BoardAPI.getApi().getPlugin(), () -> {
            byte[] buffer = MapPalette.imageToBytes(this.renderer.getBufferedImage());
            for (Fragment[] fragment : this.fragments) {
                for (Fragment value : fragment)
                    value.updateMap(buffer);
            }
            broadcastUpdate();
        }, 1L);
    }

    public List<Fragment> getFragmentsAsList() {
        List<Fragment> fragments = new ArrayList<>();
        for (Fragment[] row : this.fragments)
            fragments.addAll(Arrays.asList(row));
        return fragments;
    }

    public List<Integer> getFragmentsEntityIds() {
        return (List<Integer>)getFragmentsAsList().stream().map(Fragment::getEntityId).collect(Collectors.toList());
    }

    public boolean canReceive(Player player) {
        return true;
    }

    public void spawn(Player player) {
        this.recipients.add(player);
        for (Fragment[] row : this.fragments) {
            for (Fragment fragment : row) {
                if (fragment != null)
                    fragment.spawn(player);
            }
        }
    }

    public void broadcastSpawn() {
        this.location.getWorld().getPlayers().forEach(this::spawn);
    }

    public void destroy(Player player) {
        this.recipients.remove(player);
        for (Fragment[] row : this.fragments) {
            for (Fragment fragment : row) {
                if (fragment != null)
                    fragment.destroy(player);
            }
        }
    }

    public void broadcastDestroy() {
        this.location.getWorld().getPlayers().forEach(this::destroy);
    }

    public void update(Player player) {
        for (Fragment[] row : this.fragments) {
            for (Fragment fragment : row) {
                if (fragment != null)
                    fragment.update(player);
            }
        }
    }

    public void broadcastUpdate() {
        Bukkit.getOnlinePlayers().forEach(this::update);
    }

    public void teleport(Location location) {
        broadcastDestroy();
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                if (this.fragments[row][column] == null) {
                    createFragment(location, column, row);
                } else {
                    this.fragments[row][column].teleport(locationForFragment(location, column, row));
                }
            }
        }
        this.location = location;
    }

    public void setDirection(EnumDirection direction) {
        if (direction == EnumDirection.UP || direction == EnumDirection.DOWN)
            throw new IllegalStateException("direction UP, DOWN not supported");
        broadcastDestroy();
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                if (this.fragments[row][column] == null) {
                    createFragment(this.location, column, row);
                } else {
                    this.fragments[row][column].setDirection(direction);
                }
            }
        }
        this.direction = direction;
    }

    public void createFragment(Location location, int column, int row) {
        if (this.direction == EnumDirection.UP || this.direction == EnumDirection.DOWN)
            throw new IllegalStateException("direction UP, DOWN not supported");
        Location loc = locationForFragment(location, column, row);
        this.fragments[row][column] = new FragmentImpl(loc, this.direction, this, column, row);
    }

    public Location locationForFragment(Location location, int column, int row) {
        Location loc = location.clone();
        switch (this.direction) {
            case NORTH:
                loc.add(-column, -row, 0.0D);
                break;
            case WEST:
                loc.add(0.0D, -row, column);
                break;
            case EAST:
                loc.add(0.0D, -row, -column);
                break;
            case SOUTH:
                loc.add(column, -row, 0.0D);
                break;
        }
        return loc;
    }

    public Set<Player> getRecipients() {
        return this.recipients;
    }

    public BiConsumer<PacketPlayInUseEntity.EnumEntityUseAction, Player> getInteractListener() {
        return this.interactListener;
    }

    public void setInteractListener(BiConsumer<PacketPlayInUseEntity.EnumEntityUseAction, Player> interactListener) {
        this.interactListener = interactListener;
    }

    public BoardRenderer getRenderer() {
        return this.renderer;
    }

    public Fragment[][] getFragments() {
        return this.fragments;
    }

    public Location getLocation() {
        return this.location;
    }

    public EnumDirection getDirection() {
        return this.direction;
    }
}
