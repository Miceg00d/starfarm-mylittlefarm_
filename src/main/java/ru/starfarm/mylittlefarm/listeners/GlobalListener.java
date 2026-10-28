package ru.starfarm.mylittlefarm.listeners;

import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import ru.starfarm.core.profile.IProfileService;
import ru.starfarm.core.profile.Profile;
import ru.starfarm.core.profile.group.DonateGroup;
import ru.starfarm.core.tower.event.TowerConnectEvent;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.format.message.click.ClickEventType;
import ru.starfarm.core.util.format.message.hover.HoverEventType;
import ru.starfarm.core.util.time.CooldownUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.menus.amulets.AmuletChestMenu;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.EventType;
import ru.starfarm.mylittlefarm.objects.WitchAnimation;
import ru.starfarm.mylittlefarm.objects.achievements.GameAchievement;
import ru.starfarm.mylittlefarm.player.enums.Top;
import ru.starfarm.mylittlefarm.utils.LocationUtils;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalListener implements Listener {
    public Map<Player, List<String>> visit = new ConcurrentHashMap<>();

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (!MyLittleFarm.getInstance().isLoaded()) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ChatUtil.color("&cДоступ к серверу ограничен: перезагрузка."));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        val player = event.getPlayer();
        player.teleport(LocationUtils.fromString(MyLittleFarm.getInstance().getConfig().getString("spawn-location")));
        MyLittleFarm.getInstance().getGamePlayer().load(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        val player = event.getPlayer();
        MyLittleFarm plugin = MyLittleFarm.getInstance();

        if (plugin.getGamePlayer().getPlayer(player) != null) {
            plugin.getGamePlayer().unload(player);
            plugin.getCultureRegistry().unloadHolographic(player);
            plugin.getAnimalRegistry().unload(player);
        }

        witchAnimation.remove(player);
        visit.remove(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        val player = event.getPlayer();
        val message = event.getMessage();
        val gamePlayer = MyLittleFarm.getInstance().getGamePlayer();
        val playerResolver = gamePlayer.getPlayer(player);

        event.setCancelled(true);

        if (playerResolver == null) {
            return;
        }

        if (gamePlayer.getSearch().contains(player)) {
            if (Bukkit.getPlayerExact(message) == null) {
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &fДанный игрок не в сети.");
                gamePlayer.getSearch().remove(player);
                return;
            }
            val target = Bukkit.getPlayerExact(message);
            if (player.getName().equals(target.getName())) {
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &cВы не можете отправиться к самому себе на ферму.");
                gamePlayer.getSearch().remove(player);
                return;
            }
            val targetResolver = gamePlayer.getPlayer(target);
            if (targetResolver == null) {
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &cИгрок еще загружается, подождите.");
                gamePlayer.getSearch().remove(player);
                return;
            }
            if (gamePlayer.getPlayer(target).getData(DataType.VISIT) == 2 && !player.hasPermission("mlf.admin") || gamePlayer.getPlayer(target).getData(DataType.VISIT) == 1 && !player.hasPermission("mlf.admin")) {
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &cДанный игрок запретил посещение его фермы.");
                gamePlayer.getSearch().remove(player);
                return;
            }
            if (gamePlayer.getVisit().containsKey(target)) {
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &fВы не можете посетить ферму данного игрока, ибо он сейчас посещает ферму другого игрока!");
                gamePlayer.getSearch().remove(player);
                return;
            }
            gamePlayer.getVisit().put(player, target);
            gamePlayer.getSearch().remove(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(MyLittleFarm.getInstance(), () -> {
                target.showPlayer(MyLittleFarm.getInstance(), player);
                player.showPlayer(MyLittleFarm.getInstance(), target);
            });

            Arrays.stream(CultureType.values())
                    .parallel()
                    .filter(culture -> culture != CultureType.FOUNTAIN && culture != CultureType.OTHER)
                    .forEach(culture -> MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(player, culture));

            gamePlayer.getVisit().entrySet().stream()
                    .parallel()
                    .filter(entry -> entry.getValue().getName().equals(target.getName()))
                    .forEach(entry -> {
                        Player t = entry.getKey();

                        Bukkit.getScheduler().scheduleSyncDelayedTask(MyLittleFarm.getInstance(), () -> {
                            player.showPlayer(MyLittleFarm.getInstance(), t);
                            t.showPlayer(MyLittleFarm.getInstance(), player);
                        });
                    });

            gamePlayer.reloadScoreboard(player);

            playerResolver.addDataLevel(DataType.FARM_VISIT, 1);
            if (visit.containsKey(player))
                visit.get(player).add(target.getName());
            else
                visit.put(player, new ArrayList<>());
            if (visit.containsKey(player)) {
                if (visit.get(player).size() >= 4) {
                    playerResolver.profile().giveAchievement(GameAchievement.MLF_VISIT);
                }
            }
            ChatUtil.sendMessage(target, "&8[&2MyLittleFarm&8] &a%s зашел(-ла) к вам на ферму.", player.getName());
            ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &fВы вошли на ферму игрока &a%s&f. Доход на вашей ферме остановлен, на время прибывания на ферме другого игрока.", target.getName());
            return;
        }
        if (playerResolver.getData(DataType.CHAT) == 1) {
            ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &cУ вас выключен чат, включить его можно в настройках фермы.");
            return;
        }

        Profile profile = IProfileService.getService().getProfile(player);

        if (profile.isActiveMute()) {
            player.spigot().sendMessage(profile.getMute().formatReceiveMessage().toArray(new BaseComponent[0]));
            return;
        }

        String format = String.format("§b%s §f%s §7#%s§f: %s", profile.getGuildTag() == null ? "" : profile.getGuildTag(), profile.getColoredNameWithTitle(), gamePlayer.getTopNumber(Top.INCOME, player), message);
        Bukkit.getOnlinePlayers().stream()
                .parallel()
                .map(gamePlayer::getPlayer)
                .filter(resolver -> resolver != null && resolver.getData(DataType.CHAT) != 1)
                .forEach(resolver -> resolver.getHandle().spigot().sendMessage(
                        ChatUtil.newBuilder(format)
                                .hover(HoverEventType.SHOW_TEXT,
                                        "§fНик: §b" + player.getName(),
                                        "§fБаланс: §a" + StringUtils.formatDouble(playerResolver.getBalance()) + "$",
                                        "§fДоход: §e" + StringUtils.formatDouble(playerResolver.getIncome()) + "$",
                                        "",
                                        "§aНажмите, чтобы написать в ЛС."
                                )
                                .click(ClickEventType.SUGGEST_COMMAND, ChatUtil.format("/m %s ", player.getName()))
                                .build()
                ));
        Bukkit.getConsoleSender().sendMessage(format);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        val player = event.getPlayer();
        val resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        if (resolver == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CAKE_BLOCK) {
                resolver.profile().giveAchievement(GameAchievement.MLF_PICNIC);
            }
        }

        MyLittleFarm.getInstance().getGameItem().interact(event);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        val player = event.getPlayer();
        val resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        if (resolver == null) return;

        if (event.getFrom().getChunk().getX() != event.getTo().getChunk().getX() ||
                event.getFrom().getChunk().getZ() != event.getTo().getChunk().getZ()) {

            int startX = event.getTo().getChunk().getX();
            int startZ = event.getTo().getChunk().getZ();
            Bukkit.getScheduler().runTaskLater(MyLittleFarm.getInstance(), () -> {
                if (!player.isOnline()) return;
                for (int x = startX - 1; x <= startX + 1; x++) {
                    for (int z = startZ - 1; z <= startZ + 1; z++) {
                        MyLittleFarm.getInstance().getGamePlayer().updateChunk(player, x, z);
                    }
                }
            }, 1L);
        }

        if (resolver.getData(DataType.ABILITIES_FLY) != 1) {
            boolean doubleJump = IProfileService.getService().getProfile(player).hasGroup(DonateGroup.ELITE) || resolver.getData(DataType.ABILITIES_DOUBLE_JUMP) == 1;

            if (doubleJump && !CooldownUtil.has("double_jump", player) && (!player.isFlying() || player.getLocation().subtract(0.0D, 1.0D, 0.0D).getBlock().getType() == Material.AIR)) {
                player.setAllowFlight(true);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        val resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        if (resolver == null) return; // Защита от NPE

        if (resolver.getData(DataType.ABILITIES_FLY) == 1)
            return;

        if (!CooldownUtil.has("double_jump", player) && IProfileService.getService().getProfile(player).hasGroup(DonateGroup.ELITE) || !CooldownUtil.has("double_jump", player) && resolver.getData(DataType.ABILITIES_DOUBLE_JUMP) == 1) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.playEffect(player.getLocation(), Effect.EXPLOSION, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
            player.setVelocity(player.getLocation().getDirection().setY(0.75D));

            CooldownUtil.put("double_jump", player, 1500L);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }

    }

    public static Map<Player, WitchAnimation> witchAnimation = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(CraftItemStack.asNMSCopy(event.getItemDrop().getItemStack()).hasTag());
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        val player = event.getPlayer();
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onAirChange(EntityAirChangeEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            val player = (Player) event.getEntity();
            val playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

            if (playerResolver == null) return;

            if (playerResolver.hasActiveEvent(EventType.DIRTY)) {
                playerResolver.removeActiveEvent(EventType.DIRTY);
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &fВас больше не считают грязнулей!");
            }
        }
    }

    @EventHandler
    public void onTowerConnect(TowerConnectEvent event) {
        MyLittleFarm.getInstance().getFakeEntity().loadOther();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle() != null && 
            event.getInventory().getTitle().equals("§6§lКейс с амулетами")) {
            AmuletChestMenu.handleClick(event);
        }
    }
}

