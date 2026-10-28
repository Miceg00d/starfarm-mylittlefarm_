package ru.starfarm.mylittlefarm.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.hologram.Hologram;
import ru.starfarm.core.task.GlobalTaskContext;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.math.Cuboid;
import ru.starfarm.core.util.number.PercentUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.EventType;
import ru.starfarm.mylittlefarm.enums.Flower;
import ru.starfarm.mylittlefarm.enums.UpgradeType;
import ru.starfarm.mylittlefarm.objects.BlockInfo;
import ru.starfarm.mylittlefarm.player.GamePlayer;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.LocationUtils;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
public class PacketListener extends PacketAdapter {

    private final Field chunkX;
    private final Field chunkZ;
    private final int cooldown;
    private final Cuboid zone;
    private static final Location hay = LocationUtils.fromString("world 50 86 216");
    private final ProtocolManager protocolManager;

    @SneakyThrows
    public PacketListener(Plugin plugin, int cooldown) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Client.USE_ITEM, PacketType.Play.Client.SETTINGS);
        this.cooldown = cooldown;
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        this.zone = Cuboid.atLocations(
                LocationUtils.fromString("world 49 88 132"),
                LocationUtils.fromString("world 61 88 153")
        );

        Field blockChangePosition = PacketPlayOutBlockChange.class.getDeclaredField("a");
        chunkX = PacketPlayOutMapChunk.class.getDeclaredField("a");
        chunkZ = PacketPlayOutMapChunk.class.getDeclaredField("b");

        blockChangePosition.setAccessible(true);
        chunkX.setAccessible(true);
        chunkZ.setAccessible(true);

    }

    public void onPacketSending(PacketEvent event) {
        if (event.getPacket().getHandle() instanceof PacketPlayOutMapChunk) {
            Bukkit.getScheduler().runTaskLater(MyLittleFarm.getInstance(), () -> {
                PacketPlayOutMapChunk change = (PacketPlayOutMapChunk) event.getPacket().getHandle();
                int x = 0;
                int z = 0;
                try {
                    x = (Integer) chunkX.get(change);
                    z = (Integer) chunkZ.get(change);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (x == hay.getChunk().getX() && z == hay.getChunk().getZ() && !hay.getBlock().hasMetadata("break_" + event.getPlayer().getName())) event.getPlayer().sendBlockChange(hay, Material.HAY_BLOCK, (byte) 0);
                if (x != 0 && z != 0)
                    MyLittleFarm.getInstance().getGamePlayer().updateChunk(event.getPlayer(), x, z);
            }, 5L);
        }
    }

    public void onPacketReceiving(PacketEvent event) {
        Packet<PacketListenerPlayIn> packet = (Packet<PacketListenerPlayIn>) event.getPacket().getHandle();
        Player player = event.getPlayer();
        String metadataName = "break_" + player.getName();

        if (packet == null) return;

        if (event.getPacketType() == PacketType.Play.Client.SETTINGS) {
            String language = event.getPacket().getStrings().read(0);
            log.info("Player language in packet: " + language);

            PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
            if (playerResolver != null) {
                setLanguage(playerResolver, language);
            } else {
                GlobalTaskContext.asyncAfter(20, task1 -> {
                    PlayerResolver resolverAsync = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
                    if (resolverAsync != null) setLanguage(resolverAsync, language);
                });
            }
        }
        if (packet instanceof PacketPlayInBlockDig) {
            PacketPlayInBlockDig digPacket = (PacketPlayInBlockDig) packet;
            net.minecraft.server.v1_12_R1.BlockPosition position = digPacket.a();

            Block block = Bukkit.getWorlds().get(0).getBlockAt(position.getX(), position.getY(), position.getZ());

            if (block.getType() != Material.LONG_GRASS && !LocationUtils.equals(block.getLocation(), this.hay) && !zone.contains(block)) {
                val playerBlocksMap = MyLittleFarm.getInstance().getGamePlayer().getBlocks();
                val playerBlocks = playerBlocksMap.get(player);

                // Проверяем, что список блоков для игрока существует в мапе
                if (playerBlocks != null) {
                    for (BlockInfo blockInfo : playerBlocks.stream()
                            .filter(info -> info.getLocation().distance(block.getLocation()) <= 15)
                            .collect(Collectors.toList())) {
                        player.sendBlockChange(blockInfo.getLocation(), blockInfo.getId(), blockInfo.getData());
                    }
                }
            }

            if (LocationUtils.equals(block.getLocation(), this.hay) && !MyLittleFarm.getInstance().getGamePlayer().getVisit().containsKey(player) && player.getLocation().distance(block.getLocation()) < 7) {
                event.setCancelled(true);
                PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

                if (playerResolver == null) return;
                if (playerResolver.getData(DataType.HAY_SELL_ABILITY) != 1) return;

                if (!block.hasMetadata(metadataName)) {
                    handleBreak(player, block);
                    block.setMetadata(metadataName, new FixedMetadataValue(plugin, true));

                    player.sendBlockChange(hay, Material.AIR, (byte) 0);
//                    PacketContainer airPacket = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
//                    airPacket.getBlockPositionModifier().write(0, new BlockPosition(block.getX(), block.getY(), block.getZ()));
//                    airPacket.getBlockData().write(0, WrappedBlockData.createData(Material.AIR, 0));
//                    protocolManager.sendServerPacket(player, airPacket);

                    GlobalTaskContext.getContext().asyncAfter(300 * 20, task -> {
                        block.removeMetadata(metadataName, plugin);

                        player.sendBlockChange(hay, Material.HAY_BLOCK, (byte) 0);
//                        PacketContainer hayPacket = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
//                        hayPacket.getBlockPositionModifier().write(0, new BlockPosition(block.getX(), block.getY(), block.getZ()));
//                        hayPacket.getBlockData().write(0, WrappedBlockData.createData(Material.HAY_BLOCK, 0));
//                        protocolManager.sendServerPacket(player, hayPacket);

                    });
                }
                return;
            }
            if (block.getType() == Material.LONG_GRASS && block.getData() == 1 && zone.contains(block) && !MyLittleFarm.getInstance().getGamePlayer().getVisit().containsKey(player) && player.getLocation().distance(block.getLocation()) < 7) {
                event.setCancelled(true);
                PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

                if (playerResolver == null) return;

                if (block.hasMetadata(metadataName))
                    Bukkit.broadcast(ChatUtil.format("&8[&6Анти-чит&8] &fИгрок %s использует нюкер!", player.getName()), "mlf.moder");
                else {
                    handleBreak(player, block);
                    block.setMetadata(metadataName, new FixedMetadataValue(plugin, true));

                    Flower flower = Flower.get(playerResolver);
                    GlobalTaskContext.getContext().asyncAfter(cooldown * 20, task -> {
                        block.removeMetadata(metadataName, plugin);
                        if (flower != null && block.hasMetadata(flower.name().toLowerCase().concat("_").concat(player.getName()))) {
                            block.removeMetadata(flower.name().toLowerCase().concat("_").concat(player.getName()), plugin);
                        }
                        if (flower != null && ThreadLocalRandom.current().nextFloat() < 0.03) {
                            block.setMetadata(flower.name().toLowerCase().concat("_").concat(player.getName()), new FixedMetadataValue(plugin, true));
                            player.sendBlockChange(block.getLocation(), flower.material, flower.data);
                            return;
                        }
                        player.sendBlockChange(block.getLocation(), Material.LONG_GRASS, (byte) 1);
                    });
                }
            }
        }
    }

    public static void handleBreak(Player player, Block block) {
        GamePlayer gamePlayer = MyLittleFarm.getInstance().getGamePlayer();
        PlayerResolver playerResolver = gamePlayer.getPlayer(player);

        if (playerResolver == null) return;

        if (LocationUtils.equals(block.getLocation(), hay)) {
            playerResolver.addDataLevel(DataType.BROKEN_HAY, 1);
            playerResolver.addHays(1);
            return;
        }

        Flower flower = Flower.get(playerResolver);
        playerResolver.updateCombo();

        double moneyBooster = PercentUtil.getNumberByPercent(playerResolver.getUpgradeLevel(UpgradeType.GRASS_BOOSTER), playerResolver.getIncome());
        double moneyGrass = playerResolver.getUpgradeLevel(UpgradeType.GRASS_MONEY) > 0 ? playerResolver.getUpgradeLevel(UpgradeType.GRASS_MONEY) : 1;
        double currentCombo = playerResolver.getComboMultiplier();
        double money = (moneyGrass + moneyBooster) * currentCombo * playerResolver.getGlobalMoneyMultiplier();

        if (flower != null && block.hasMetadata(flower.name().toLowerCase().concat("_").concat(player.getName())))
            money *= flower.modifier;

        if (playerResolver.hasActiveEvent(EventType.MORNING_DEW))
            money *= 2;

        playerResolver.addBalance(money);
        playerResolver.addGrass(1);

        if (playerResolver.getData(DataType.HOLOGRAM) == 0) {
            showTemp(player, block.getLocation().clone().add(0, 1.0, 0), ChatUtil.format("&a+%s$", StringUtils.formatDouble(money)), 3);
        }
    }

    public static void setLanguage(PlayerResolver playerResolver, String language) {
        if (playerResolver == null || language == null) {
            return;
        }
        String playerLanguage = language.split("_")[0];
        String playerLocale;
        if (playerLanguage.equals("ru") || playerLanguage.equals("kk") || playerLanguage.equals("be")) {
            playerLocale = "ru";
        } else {
            playerLocale = "en";
        }
        playerResolver.setLanguage(playerLocale);
    }

    public static void showTemp(Player player, Location location, String text, int existenceInSeconds) {
        Hologram protocolHolographic = ApiManager.createHologram(location.add(0, 0.7, 0));
        protocolHolographic.setAccess(p -> p.getName().equals(player.getName()));
        protocolHolographic.textLine(text);

        Bukkit.getScheduler().runTaskLaterAsynchronously(MyLittleFarm.getInstance(), protocolHolographic::handleRemove, 20L * existenceInSeconds);
    }
}
