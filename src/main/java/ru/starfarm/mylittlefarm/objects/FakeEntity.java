package ru.starfarm.mylittlefarm.objects;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.starfarm.core.entity.FakeLivingEntity;
import ru.starfarm.core.entity.impl.*;
import ru.starfarm.core.entity.type.Interact;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.texture.skin.Skin;
import ru.starfarm.core.util.texture.skin.SkinUtil;
import ru.starfarm.core.util.time.CooldownUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.*;
import ru.starfarm.mylittlefarm.menus.MainMenu;
import ru.starfarm.mylittlefarm.menus.UpgradesMenu;
import ru.starfarm.mylittlefarm.menus.animal.AnimalMerchantMenu;
import ru.starfarm.mylittlefarm.menus.watcher.WatcherMenu;
import ru.starfarm.mylittlefarm.player.GamePlayer;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.LocationUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FakeEntity {
    public Map<String, FakeEntityInfo> fakeEntity = new ConcurrentHashMap<>();

    public FakeEntity() {
        MyLittleFarm.getInstance().getHandler().getExecuteHandler()
                .executeQuery(true, "SELECT * FROM mlf_npcs")
                .thenAccept(resultSet -> {
                    while (resultSet.next()) {
                        String baseName = resultSet.getString("name");
                        Location location = LocationUtils.fromString(resultSet.getString("location"));
                        Creature creature;
                        try {
                            creature = CultureType.valueOf(resultSet.getString("creature"));
                        } catch (IllegalArgumentException ignored) {
                            creature = AnimalType.valueOf(resultSet.getString("creature"));
                        }
                        EntityType entityType = EntityType.valueOf(resultSet.getString("entityType"));
                        log.debug(baseName + location + creature + entityType);
                        fakeEntity.put(baseName, new FakeEntityInfo(baseName, resultSet.getString("skin"), location, entityType, creature));
                    }
                });
    }

    public void loadOther() {

        fakeEntity.forEach((key, info) -> {

            Location location = info.getLocation();
            String baseName = info.getName();
            String name = ChatUtil.color("&7[NPC] &6&l".concat(baseName));
//            Skin skinFuture = SkinUtil.getSkin(info.getSkin());
            CompletableFuture<Skin> skinFuture = SkinUtil.getSkinLazy(info.getSkin());

            skinFuture.thenAccept(skin -> {
                FakeLivingEntity entity = null;
                switch (info.getEntityType()) {
                    case PLAYER: {
                        if (skin == null) {
                            entity = new FakePlayer(SkinUtil.NULL, location);
                            ((FakePlayer) entity).getHologram().textLine(0, name);
                            break;
                        }
                        entity = new FakePlayer(skin, location);
                        ((FakePlayer) entity).getHologram().textLine(0, name);
                        break;
                    }
                    case BLAZE: {
                        entity = new FakeBlaze(location);
                        break;
                    }
                    case OCELOT: {
                        entity = new FakeOcelot(location);
                        break;
                    }
                    case WOLF: {
                        entity = new FakeWolf(location);
                        break;
                    }
                    case IRON_GOLEM: {
                        entity = new FakeIronGolem(location);
                        break;
                    }
                    case ENDERMAN: {
                        entity = new FakeEnderman(location);
                        break;
                    }
                    case WITCH: {
                        entity = new FakeWitch(location);
                        break;
                    }
                }
                if (entity != null) {
                    if (!(entity instanceof FakePlayer)) {
                        entity.setCustomName(name);
                        entity.setCustomNameVisible(true);
                    }

                    FakeLivingEntity finalEntity = entity;
                    entity.setClick((player, interact) -> {
                        click(player, interact, finalEntity, info);
                    });
                }
            });
        });
    }

    public void click(Player player, Interact interact, FakeLivingEntity fakeEntity, FakeEntityInfo info) {
        GamePlayer gamePlayer = MyLittleFarm.getInstance().getGamePlayer();
        PlayerResolver playerResolver = gamePlayer.getPlayer(player);
        if (playerResolver == null) {
            ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &cВаши данные еще загружаются...");
            return;
        }
        Location entityLocation = fakeEntity.getLocation();

        if (interact != Interact.CLICK) return;
        if (CooldownUtil.has("fake_entity_click", player)) return;
        if (playerResolver.hasActiveEvent(EventType.DIRTY)) {
            ChatUtil.sendMessage(player, "&7%s: &fО-ге-ге! Сначала помойся, потом живность покупай!", ChatUtil.color(info.getName()));
            return;
        }
        else if (entityLocation.equals(LocationUtils.fromString("world -112.4 77 209.4 172.3 -0.1"))) {
            player.teleport(LocationUtils.fromString("world -561.5 70 550.4 179.5 0.3"));
            return;
        } else if (entityLocation.equals(LocationUtils.fromString("world -559.4 69 548.5 173.8 -2.0"))) {
            player.teleport(LocationUtils.fromString("world -114.4 77 211.5 179.7 -0.8"));
            return;
        } else if (entityLocation.equals(LocationUtils.fromString("world 43.383 85.93750 216.317 180.0 0.0"))) {
            new WatcherMenu().openInventory(player);
            return;
        } else if (entityLocation.equals(LocationUtils.fromString("world -20.485 88.00000 140.511"))) {
            new AnimalMerchantMenu().openInventory(player);
        }

        CooldownUtil.put("fake_entity_click", player, 500L);
        if (info.getName().contains("Баба Маня")) {
            if (playerResolver.getRebirths() > 0 && playerResolver.getData(DataType.TUTORIAL) < 4) {
                playerResolver.setDataLevel(DataType.TUTORIAL, 4);
            }
            if (playerResolver.getData(DataType.TUTORIAL) < 4) {
                switch (playerResolver.getData(DataType.TUTORIAL)) {
                    case 0: {
                        if (playerResolver.getCultureLevel(CultureType.WHEAT) == 0) {
                            ChatUtil.sendMessage(player, "&5Баба Маня&f: Привет, миленький! У меня для тебя есть задание, сходить купить 1 пшеничку! Она продается у Василича, он находиться позади!");
                            return;
                        }
                        playerResolver.setDataLevel(DataType.TUTORIAL, 1);
                        ChatUtil.sendMessage(player, "&5Баба Маня&f: Отлично справился! Теперь сходи в центр амбара и сруби 100 сорняков");
                        break;
                    }
                    case 1: {
                        if (playerResolver.getGrass() < 100) {
                            ChatUtil.sendMessage(player, "&5Баба Маня&f: Молодец, теперь сходи в центр амбара и сруби 100 сорняков");
                            return;
                        }
                        playerResolver.setDataLevel(DataType.TUTORIAL, 2);
                        ChatUtil.sendMessage(player, "&5Баба Маня&f: Молодец! Пирожочек, скорее покупай еще 4 пшеницы!");
                        break;
                    }
                    case 2: {
                        if (playerResolver.getCultureLevel(CultureType.WHEAT) < 5) {
                            ChatUtil.sendMessage(player, "&5Баба Маня&f: Молодец! Пирожочек, скорее покупай еще 4 пшеницы!");
                            return;
                        }
                        playerResolver.setDataLevel(DataType.TUTORIAL, 3);
                        ChatUtil.sendMessage(player, "&5Баба Маня&f: Отлично, теперь ты сможешь купить у меня улучшение для пшеницы!");
                        break;
                    }
                    case 3: {
                        if (playerResolver.getUpgradeLevel(UpgradeType.WHEAT_BOOSTER) < 2) {
                            ChatUtil.sendMessage(player, "&5Баба Маня&f: Отлично, теперь ты сможешь купить у меня улучшение для пшеницы!");
                            new UpgradesMenu().openInventory(player);
                            return;
                        }
                        playerResolver.setDataLevel(DataType.TUTORIAL, 4);
                        playerResolver.setDataLevel(DataType.ABILITIES_MAX_OFFLINE_HOURS, 2);
                        ChatUtil.sendMessage(player, "&5Баба Маня&f: Молодчина! Теперь я могу доверить ферму тебе!");
                        break;
                    }
//                    case 4: {
//                        playerResolver.setDataLevel(DataType.TUTORIAL, 5);
//                        break;
//                    }
//                    case 5: {
//                        playerResolver.setDataLevel(DataType.TUTORIAL, 6);
//                        break;
//                    }
                }
                return;
            }

            if (gamePlayer.getVisit().containsKey(player)) {
                val target = gamePlayer.getVisit().get(player);

                gamePlayer.getVisit().remove(player);
                Bukkit.getScheduler().scheduleSyncDelayedTask(MyLittleFarm.getInstance(), () -> {
                    target.hidePlayer(player);
                    player.hidePlayer(target);
                });

                Arrays.stream(CultureType.values())
                        .filter(culture -> culture != CultureType.FOUNTAIN && culture != CultureType.OTHER)
                        .forEach(culture -> MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(player, culture));

                gamePlayer.getVisit().entrySet().stream()
                        .filter(entry -> entry.getValue().getName().equals(target.getName()))
                        .forEach(entry -> {
                            Player t = entry.getKey();

                            Bukkit.getScheduler().scheduleSyncDelayedTask(MyLittleFarm.getInstance(), () -> {
                                player.hidePlayer(t);
                                t.hidePlayer(player);
                            });
                        });

                gamePlayer.reloadScoreboard(player);
                ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &fВы вернулись на свою ферму.");
                ChatUtil.sendMessage(target, "&8[&2MyLittleFarm&8] &c%s вышел с вашей фермы.", player.getName());
                return;
            }
            new MainMenu().openInventory(player);
            return;
        }

        if (info.getCreature() instanceof CultureType && info.getCreature() != CultureType.OTHER) {
            playerResolver.buyCulture((CultureType) info.getCreature(), true);
        }


    }

    public boolean isFakeEntity(String name) {
        return fakeEntity.containsKey(ChatColor.stripColor(name));
    }

    public FakeEntityInfo getFakeEntity(String name) {
        return fakeEntity.get(name);
    }
}