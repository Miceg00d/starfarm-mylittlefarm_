package ru.starfarm.mylittlefarm.top;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.server.v1_12_R1.EnumDirection;
import net.minecraft.server.v1_12_R1.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.starfarm.mylittlefarm.top.impl.PrivateBannerBoard;
import ru.starfarm.mylittlefarm.top.impl.SimpleBannerBoard;

import java.lang.reflect.Field;
import java.util.Set;

public class BoardAPI extends BukkitRunnable implements Listener {
    private static BoardAPI api;

    private static Field entityIdField;

    private final JavaPlugin plugin;

    private final Set<BannerBoard> boardsCache;

    static {
        try {
            entityIdField = PacketPlayInUseEntity.class.getDeclaredField("a");
            entityIdField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    BoardAPI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.boardsCache = (Set<BannerBoard>)new ConcurrentSet();
        runTaskTimerAsynchronously(plugin, 10L, 20L);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void register(BannerBoard board) {
        this.boardsCache.add(board);
    }

    public void unregister(BannerBoard board) {
        board.broadcastDestroy();
        this.boardsCache.remove(board);
    }

    void handleClick(Player player, int entityId, PacketPlayInUseEntity.EnumEntityUseAction action) {
        this.boardsCache.forEach((board) -> {
            if (board.getFragmentsEntityIds().stream().anyMatch((id) -> {
                return id == entityId;
            }) && board.getInteractListener() != null) {
                board.getInteractListener().accept(action, player);
            }

        });
    }


    @EventHandler
    void teleport(PlayerTeleportEvent e) {
        this.boardsCache.forEach(board -> board.destroy(e.getPlayer()));
    }

    @EventHandler
    void worldChange(PlayerChangedWorldEvent e) {
        this.boardsCache.forEach(board -> board.destroy(e.getPlayer()));
    }

    @EventHandler
    void quit(PlayerQuitEvent e) {
        this.boardsCache.forEach(board -> board.destroy(e.getPlayer()));
    }

    @EventHandler
    void kick(PlayerKickEvent e) {
        this.boardsCache.forEach(board -> board.destroy(e.getPlayer()));
    }

    @EventHandler
    void respawn(PlayerRespawnEvent e) {
        this.boardsCache.forEach(board -> board.destroy(e.getPlayer()));
    }

    @EventHandler
    public void join(final PlayerJoinEvent e) {
        this.boardsCache.forEach(board -> board.update(e.getPlayer()));
    }

    void stop0() {
        cancel();
        this.boardsCache.removeIf(b -> {
            b.broadcastDestroy();
            return true;
        });
        HandlerList.unregisterAll(this);
    }

    public void run() {
        Bukkit.getOnlinePlayers().forEach((player) -> {
            this.boardsCache.forEach((board) -> {
                if (player.getLocation().distance(board.getLocation()) > 60.0D && board.getRecipients().contains(player)) {
                    board.destroy(player);
                } else if (!board.getRecipients().contains(player) && board.canReceive(player)) {
                    board.spawn(player);
                }

            });
        });
    }

    public static BannerBoard createBanner(Location location, EnumDirection direction, int columns, int rows) {
        BoardAPI api = getApi();
        SimpleBannerBoard simpleBannerBoard = new SimpleBannerBoard(location, direction, columns, rows);
        api.register(simpleBannerBoard);
        return simpleBannerBoard;
    }

    public static BannerBoard createPrivateBanner(Location location, EnumDirection direction, int columns, int rows, Player viewer) {
        BoardAPI api = getApi();
        PrivateBannerBoard privateBannerBoard = new PrivateBannerBoard(location, direction, columns, rows, viewer);
        api.register(privateBannerBoard);
        return privateBannerBoard;
    }

    public static BoardAPI getApi() {
        if (api == null)
            throw new IllegalStateException("api not initialized");
        return api;
    }

    public static BoardAPI init(JavaPlugin plugin) {
        if (api != null)
            throw new IllegalStateException("api already initialized");
        api = new BoardAPI(plugin);
        return api;
    }

    public static void stop() {
        getApi().stop0();
        api = null;
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }
}
