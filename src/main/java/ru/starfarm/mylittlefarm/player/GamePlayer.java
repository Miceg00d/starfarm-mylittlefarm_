package ru.starfarm.mylittlefarm.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.CorePlugin;
import ru.starfarm.core.database.query.row.ValueQueryRow;
import ru.starfarm.core.database.table.DatabaseTable;
import ru.starfarm.core.profile.IProfileService;
import ru.starfarm.core.scoreboard.IScoreboardService;
import ru.starfarm.core.scoreboard.ScoreboardBuilder;
import ru.starfarm.core.task.GlobalTaskContext;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.EventType;
import ru.starfarm.mylittlefarm.enums.UpgradeType;
import ru.starfarm.mylittlefarm.objects.BlockInfo;
import ru.starfarm.mylittlefarm.objects.achievements.GameAchievement;
import ru.starfarm.mylittlefarm.player.enums.Top;
import ru.starfarm.mylittlefarm.utils.*;
import ru.starfarm.mylittlefarm.utils.config.ExclusionsStrategies;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class GamePlayer extends BukkitRunnable {
    private final @Getter Map<String, PlayerResolver> players = new ConcurrentHashMap<>();
    public @Getter Map<Player, List<BlockInfo>> blocks = new ConcurrentHashMap<>();
    public @Getter Map<Player, Player> visit = new ConcurrentHashMap<>();
    public @Getter Set<Player> search = new ConcurrentSet<>();

    public Map<Top, Map<String, Integer>> top = new LinkedHashMap<>();
    public Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionsStrategies()).create();

    private final DatabaseTable table;

    public GamePlayer(CorePlugin corePlugin) {
        table = MyLittleFarm.getInstance().getHandler().getTable("mlf_players");
        runTaskTimerAsynchronously(corePlugin, 1, 20 * TimeUnit.MINUTES.toSeconds(5));

        GlobalTaskContext.getContext().everyAsync(0, 40, task -> players.values().forEach(player -> {
            if (player.getLastUpdate() == 0L)
                player.setLastUpdate(System.currentTimeMillis());
            if (player.getNext() == 0L)
                player.setNext(ThreadLocalRandom.current().nextInt(5400000, 7200000));
            if (System.currentTimeMillis() - player.getLastUpdate() >= player.getNext()) {
                onEvent(player.getHandle());
                player.setLastUpdate(System.currentTimeMillis());
            }
        }));

        GlobalTaskContext.getContext().everyAsync(20, 20, task -> players.values().forEach(player -> {
            player.getAnimalLog().forEach((animal, animalData) -> {
                if (animalData.getCount() == 0) return;

                int baseSeconds = 0;
                switch (animal) {
                    case CHICKEN: baseSeconds = 300; break;
                    case RABBIT: baseSeconds = 600; break;
                    case SHEEP: baseSeconds = 900; break;
                    case PIG: baseSeconds = 1200; break;
                    case COW: baseSeconds = 1500; break;
                    case HORSE: baseSeconds = 1800; break;
                }

                if (animalData.getSecondsLeft() <= 0) {
                    animalData.setSecondsLeft(baseSeconds);
                }

                animalData.setSecondsLeft(animalData.getSecondsLeft() - 1);

                if (animalData.getSecondsLeft() <= 0) {
                    int level = animalData.getCount();
                    int max = 5 * level;
                    int currentAmount = player.getProductCount(animal.getProduct());

                    if (currentAmount < max) {
                        int toAdd = level;

                        if (currentAmount + toAdd > max) {
                            toAdd = max - currentAmount;
                        }

                        player.addProductLevel(animal.getProduct(), toAdd);
                        animalData.setSecondsLeft(baseSeconds);
                    }
                }
            });
            player.getProducts().forEach((product, productInfo) -> {
                if (productInfo == null || productInfo.queue.isEmpty()) {
                    return;
                }

                Integer firstId = productInfo.queue.keySet().iterator().next();
                int timeLeft = productInfo.queue.get(firstId);

                int newTime = timeLeft - 1;
                productInfo.queue.put(firstId, newTime);

                if (newTime <= 0) {
                    ChatUtil.sendMessage(player.getHandle(), "&8[&2MyLittleFarm&8] &aОкончено создание ресурса: &e%s&a.", product.name.toLowerCase());

                    player.addProductLevel(product, 1);
                    productInfo.queue.remove(firstId);

                }
            });
            player.getEvents().forEach((event, info) -> {
                if (info.getTime() <= -1) return;
                info.setTime(info.getTime() - 1);

                if (info.getTime() <= 0) {
                    player.removeActiveEvent(event);

                    switch (event) {
                        case BLOOM: {
                            ChatUtil.sendMessage(player.getHandle(), "&8[&2MyLittleFarm&8] &aЦветы закончили свою благоухающую пору, цветение закончилось!");
                            player.profile().giveAchievement(GameAchievement.MLF_BLOOM);
                            break;
                        }
                        case MORNING_DEW: {
                            ChatUtil.sendMessage(player.getHandle(), "&8[&2MyLittleFarm&8] &aНаступил день, вся роса испарилась...");
                            break;
                        }
                    }
                }
            });
        }));

        ApiManager.newTabBuilder()
                .setTab(player -> IProfileService.getService().getProfile(player).getHideColoredNameWithTag())
                .setTag((player, hologram) -> {
                    hologram.textLine(0, IProfileService.getService().getProfile(player).getHideColoredNameWithTag());
                    ApiManager.addGuildToTag(hologram, ApiManager.getPlayerProfile(player), 1);
                })
                .setAccess((owner, target) -> !owner.isSneaking() && target.canSee(owner))
                .register();
    }

    public void onEvent(Player player) {
        PlayerResolver playerResolver = getPlayer(player);

        if (playerResolver == null) return;
        if (playerResolver.getData(DataType.TUTORIAL) <= 3) return;

        EventType eventType = EventType.values()[ThreadLocalRandom.current().nextInt(0, EventType.values().length)];
        if (playerResolver.hasActiveEvent(eventType)) {
            return;
        }
        if (playerResolver.getData(DataType.POSITIVE_EVENTS) >= 2) {
            eventType = EventType.DIRTY;
        }
        if (playerResolver.getData(DataType.NEGATIVE_EVENTS) >= 1) {
            eventType = EventType.values()[ThreadLocalRandom.current().nextInt(2, EventType.values().length)];
        }
        if (playerResolver.isHappyDay()) {
            eventType = EventType.values()[ThreadLocalRandom.current().nextInt(2, EventType.values().length)];
        }

        playerResolver.activeEvent(eventType);
    }

    public void load(Player player) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (player != p && player.canSee(p)) {
                PlayerHider.hidePlayer(player, p);
                PlayerHider.hidePlayer(p, player);
            }
        });


        table.newDatabaseQuery()
                .selectQuery()
                .queryRow(new ValueQueryRow("name", player.getName()))
                .executeQueryAsync(MyLittleFarm.getInstance().getHandler())
                .thenAccept(result -> {
                    PlayerResolver resolver;

                    if (!result.next()) {

                        table.newDatabaseQuery()
                                .insertQuery()
                                .queryRow(new ValueQueryRow("id", player.getUniqueId().toString())) // <--- ВОТ ЭТО ДОБАВИЛИ
                                .queryRow(new ValueQueryRow("name", player.getName()))
                                .queryRow(new ValueQueryRow("balance", 100.0))
                                .queryRow(new ValueQueryRow("income", 0.0))
                                .queryRow(new ValueQueryRow("inventory", ItemSerialization.toBase64(player.getInventory())))
                                .queryRow(new ValueQueryRow("culturesLog", "{}"))
                                .queryRow(new ValueQueryRow("upgradesLog", "{}"))
                                .queryRow(new ValueQueryRow("animalLog", "{}"))
                                .queryRow(new ValueQueryRow("products", "{}"))
                                .queryRow(new ValueQueryRow("skills", "{}"))
                                .queryRow(new ValueQueryRow("dataLog", "{}"))
                                .queryRow(new ValueQueryRow("longDataLog", "{}"))
                                .queryRow(new ValueQueryRow("events", "{}"))
                                .queryRow(new ValueQueryRow("tasks", "{}"))
                                .queryRow(new ValueQueryRow("boosters", "[]"))
                                .queryRow(new ValueQueryRow("rebirths", 0))
                                .queryRow(new ValueQueryRow("rbTokens", 0))
                                .executeAsync(MyLittleFarm.getInstance().getHandler());

                        resolver = PlayerResolver.builder()
                                .handle(player)
                                .balance(100)
                                .income(0)
                                .culturesLog(new ConcurrentHashMap<>())
                                .animalLog(new ConcurrentHashMap<>())
                                .products(new ConcurrentHashMap<>())
                                .skills(new ConcurrentHashMap<>())
                                .upgradesLog(new ConcurrentHashMap<>())
                                .dataLog(new ConcurrentHashMap<>())
                                .longDataLog(new ConcurrentHashMap<>())
                                .events(new ConcurrentHashMap<>())
                                .tasks(new ConcurrentHashMap<>())
                                .grass(0)
                                .hays(0)
                                .next(TimeUnit.MINUTES.toMillis(10))
                                .lastUpdate(System.currentTimeMillis())
                                .happy(0L)
                                .boosterList(new ArrayList<>())
                                .cultureIncomeSet(new HashSet<>())
                                .build();
                    } else {

                        resolver = PlayerResolver.builder()
                                .handle(player)
                                .balance(result.getDouble("balance"))
                                .income(result.getDouble("income"))
                                .culturesLog(Validator.getOrNull(gson.fromJson(result.getString("culturesLog"), Constants.CULTURE_TYPE), ConcurrentHashMap::new))
                                .animalLog(Validator.getOrNull(gson.fromJson(result.getString("animalLog"), Constants.ANIMAL_TYPE), ConcurrentHashMap::new))
                                .upgradesLog(Validator.getOrNull(gson.fromJson(result.getString("upgradesLog"), Constants.UPGRADE_TYPE), ConcurrentHashMap::new))
                                .dataLog(Validator.getOrNull(gson.fromJson(result.getString("dataLog"), Constants.DATA_TYPE), ConcurrentHashMap::new))
                                .longDataLog(Validator.getOrNull(gson.fromJson(result.getString("longDataLog"), Constants.LONG_DATA_TYPE), ConcurrentHashMap::new))
                                .products(Validator.getOrNull(gson.fromJson(result.getString("products"), Constants.PRODUCT_TYPE), ConcurrentHashMap::new))
                                .skills(Validator.getOrNull(gson.fromJson(result.getString("skills"), Constants.SKILL_TYPE), ConcurrentHashMap::new))
                                .events(Validator.getOrNull(gson.fromJson(result.getString("events"), Constants.EVENT_TYPE), ConcurrentHashMap::new))
                                .tasks(Validator.getOrNull(gson.fromJson(result.getString("tasks"), Constants.TASK_TYPE), ConcurrentHashMap::new))
                                .grass(result.getInt("grass"))
                                .hays(result.getInt("hays"))
                                .next(result.getLong("next"))
                                .lastUpdate(result.getLong("lastUpdate"))
                                .happy(result.getLong("happy"))
                                .boosterList(Validator.getOrNull(gson.fromJson(result.getString("boosters"), Constants.INCOME_BOOSTER_TYPE), ArrayList::new))
                                .rebirths(result.getInt("rebirths"))
                                .rbTokens(result.getLong("rbTokens"))
                                .cultureIncomeSet(new HashSet<>())
                                .build();

                        try {
                            String encodedInvContent = result.getString("inventory");
                            ItemStack[] invContents = ItemSerialization.itemStackArrayFromBase64(encodedInvContent);
                            for (int i = 0; i < invContents.length; i++) {
                                player.getInventory().setItem(i, invContents[i]);
                            }
                        } catch (Exception e) {
                            log.error("Ошибка загрузки инвентаря для " + player.getName());
                        }
                    }


                    players.put(player.getName(), resolver);

                    Bukkit.getScheduler().runTask(MyLittleFarm.getInstance(), () -> {
                        try {

                            ItemStack[] itemStacks = player.getInventory().getContents();
                            for (int i = 0; i < itemStacks.length; i++) {
                                ItemStack itemStack = itemStacks[i];
                                if (itemStack != null && (itemStack.getType() == Material.GLASS_BOTTLE || itemStack.getType() == Material.POTION)) {
                                    player.getInventory().setItem(i, null);
                                    resolver.removeActiveEvent(EventType.DROUGHT);
                                }
                            }

                            val blocksInfo = new ArrayList<BlockInfo>();
                            Arrays.stream(CultureType.values())
                                    .filter(cultureType -> resolver.getCultureLevel(cultureType) > 0)
                                    .forEach(cultureType -> MyLittleFarm.getInstance().getCultureRegistry().getCultures().row(cultureType).forEach((id, cultureInfo) -> {
                                        if (resolver.getCultureLevel(cultureType) >= id) {
                                            if (MyLittleFarm.getInstance().getCultureRegistry().getCulture(cultureType, id).locations() != null) {
                                                for (String s : MyLittleFarm.getInstance().getCultureRegistry().getCulture(cultureType, id).locations()) {
                                                    String[] block = s.split(" ");
                                                    blocksInfo.add(new BlockInfo(Integer.parseInt(block[0]), Byte.parseByte(block[1]),
                                                            new Location(Bukkit.getWorld(block[2]), Double.parseDouble(block[3]), Double.parseDouble(block[4]), Double.parseDouble(block[5]))));
                                                }
                                            }
                                        }
                                    }));
                            blocks.put(player, blocksInfo);

                            loadScoreboard(player);

                            MyLittleFarm.getInstance().getCultureRegistry().loadHolographics(player);



                            if (resolver.getData(DataType.ABILITIES_FLY) == 1) {
                                player.setAllowFlight(true);
                                player.setFlying(true);
                            }


                            try {
                                resolver.setupPlayer();
                            } catch (Throwable t) {
                                log.error("Критическая ошибка в resolver.setupPlayer (Core-плагин). Игрок загружен без NPC.");
                                t.printStackTrace();
                            }

                        } catch (Exception ex) {
                            log.error("Ошибка финальной стадии загрузки для " + player.getName());
                            ex.printStackTrace();
                        }
                    });
                });
    }

    public void loadScoreboard(Player player) {
        ScoreboardBuilder scoreboardBuilder = ApiManager.newScoreboardBuilder();
        scoreboardBuilder.setTitle(ChatUtil.color("&bMy Little Farm"));

        Player targetPlayer = visit.getOrDefault(player, player);
        PlayerResolver resolver = getPlayer(targetPlayer);

        if (resolver == null) {
            return;
        }

        scoreboardBuilder.setLine(15, "  §e" + LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        if (resolver.isActiveEvent()) {
            scoreboardBuilder.setLine(14, "");
            scoreboardBuilder.setLine(13, " §e§lАктивные события");
            scoreboardBuilder.setLine(12, ChatUtil.format("  %s", resolver.getEvents().keySet().stream().map(EventType::getName).collect(Collectors.joining("&7, "))));

        }
        scoreboardBuilder.setLine(11, "");
        scoreboardBuilder.setLine(10, ChatUtil.format(" §e§l%s", resolver.getHandle().getName()));
        scoreboardBuilder.setLine(9, ChatUtil.format("   &aБаланс: §2$%s", StringUtils.formatDouble(resolver.getBalance())));
        scoreboardBuilder.setLine(8, ChatUtil.format("   &dПерерождения: §5%s", resolver.getRebirths()));
        scoreboardBuilder.setLine(7, ChatUtil.format("   &aДоход: §2$%s", StringUtils.formatDouble(resolver.getIncome())));
        scoreboardBuilder.setLine(5, ChatUtil.format("   &fМесто в топе: §7%s", getTopNumber(Top.INCOME, resolver.getHandle())));
        scoreboardBuilder.setLine(4, "");
        scoreboardBuilder.setLine(3, ChatUtil.format("   &fОнлайн: §a%s", Bukkit.getOnlinePlayers().size()));
        scoreboardBuilder.setLine(2, "");
        scoreboardBuilder.setLine(1, "    §bwww.starfarm.fun");

        scoreboardBuilder.addUpdater(20, (boardPlayer, baseScoreboard) -> {
            Player upTarget = visit.getOrDefault(boardPlayer, boardPlayer);
            PlayerResolver playerResolver = getPlayer(upTarget);

            if (playerResolver == null) return;

            if (playerResolver.isActiveEvent()) {
                baseScoreboard.setLine(14, "");
                baseScoreboard.setLine(13, " §e§lАктивные события");
                baseScoreboard.setLine(12, ChatUtil.format("  %s", playerResolver.getEvents().keySet().stream().map(EventType::getName).collect(Collectors.joining("&7, "))));
            } else {

                baseScoreboard.removeLine(14);
                baseScoreboard.removeLine(13);
                baseScoreboard.removeLine(12);
            }
            double income = playerResolver.getIncome();
            if (playerResolver.hasActiveEvent(EventType.BLOOM))
                income *= 2;

            baseScoreboard.setLine(15, "  §e" + LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
            baseScoreboard.setLine(10, ChatUtil.format(" §e§l%s", playerResolver.getHandle().getName()));
            baseScoreboard.setLine(9, ChatUtil.format("   &aБаланс: §2$%s", StringUtils.formatDouble(playerResolver.getBalance())));
            baseScoreboard.setLine(8, ChatUtil.format("   &dПерерождения: §5%s", playerResolver.getRebirths()));
            baseScoreboard.setLine(7, ChatUtil.format("   &aДоход: §2$%s", StringUtils.formatDouble(income)));
            baseScoreboard.setLine(5, ChatUtil.format("   &fМесто в топе: §7%s", getTopNumber(Top.INCOME, playerResolver.getHandle())));
            baseScoreboard.setLine(3, ChatUtil.format("   &fОнлайн: §a%s", Bukkit.getOnlinePlayers().size()));


        });

        scoreboardBuilder.build(player);
    }

    public void reloadScoreboard(Player player) {
        IScoreboardService.getService().getPlayerScoreboard(player).remove();
        loadScoreboard(player);
    }

    public void save(PlayerResolver playerResolver) {
        MyLittleFarm.getInstance().getHandler().getExecuteHandler().executeUpdate(true,
                "UPDATE mlf_players SET `balance` = ?, `income` = ?, `inventory` = ?, `culturesLog` = ?, `animalLog` = ?, `products` = ?, `skills` = ?, `upgradesLog` = ?, `dataLog` = ?, `longDataLog` = ?, `events` = ?, `tasks` = ?, `grass` = ?, `hays` = ?, `next` = ?, `happy` = ?, `lastUpdate` = ?, `boosters` = ?, `rebirths` = ?, `rbTokens` = ? WHERE `name` = ?",
                playerResolver.getBalance(),
                playerResolver.getIncome(),
                ItemSerialization.toBase64(playerResolver.getHandle().getInventory()),
                gson.toJson(playerResolver.getCulturesLog()), gson.toJson(playerResolver.getAnimalLog()),
                gson.toJson(playerResolver.getProducts()), gson.toJson(playerResolver.getSkills()),
                gson.toJson(playerResolver.getUpgradesLog()), gson.toJson(playerResolver.getDataLog()), gson.toJson(playerResolver.getLongDataLog()),
                gson.toJson(playerResolver.getEvents()), gson.toJson(playerResolver.getTasks()), playerResolver.getGrass(), playerResolver.getHays(), playerResolver.getNext(),
                playerResolver.getHappy(), playerResolver.getLastUpdate(),
                gson.toJson(playerResolver.getBoosterList()),
                playerResolver.getRebirths(),
                playerResolver.getRbTokens(),
                playerResolver.getHandle().getName()
        );
    }

    public void unload(Player player) {
        getPlayer(player).setLogoutTime();
        save(players.remove(player.getName()));
        blocks.remove(player);
        visit.entrySet().stream()
                .filter(key -> key.getValue().getName().equals(player.getName()))
                .forEach(key -> {
                    visit.remove(key.getKey());
                    reloadScoreboard(key.getKey());
                    Arrays.stream(CultureType.values())
                            .filter(culture -> culture != CultureType.FOUNTAIN && culture != CultureType.OTHER)
                            .forEach(culture -> MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(key.getKey(), culture));
                });
        visit.remove(player);

        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) player).getHandle())
        ));
    }

    public void updateChunk(Player player, int x, int z) {
        val fakeBlocks = blocks.get(player);

        if (fakeBlocks == null) return;
        if (fakeBlocks.isEmpty()) return;

        Chunk chunk = Bukkit.getWorlds().get(0).getChunkAt(x, z);
        List<BlockInfo> blocks = fakeBlocks.stream()
                .filter(blockInfo -> blockInfo.getLocation().getChunk().equals(chunk))
                .collect(Collectors.toList());

        if (blocks.isEmpty()) {
            return;
        }
        updateBlocks(player, x, z, blocks);
    }

    public void updateBlocks(Player player, int x, int z, List<BlockInfo> blocks) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
        MultiBlockChangeInfo[] info = new MultiBlockChangeInfo[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            BlockInfo blockInfo = blocks.get(i);
            info[i] = new MultiBlockChangeInfo(blockInfo.getLocation(),
                    WrappedBlockData.createData(Material.getMaterial(blockInfo.getId()), blockInfo.getData()));
        }
        packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(x, z));
        packet.getMultiBlockChangeInfoArrays().write(0, info);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }

    public boolean isPlayerLoad(Player player) {
        return players.containsKey(player.getName());
    }

    public PlayerResolver getPlayer(Player player) {
        return players.get(player.getName());
    }

    public int getTopNumber(Top top, Player player) {
        if (this.top.get(top).containsKey(player.getName())) {
            return this.top.get(top).get(player.getName());
        }
        return 2000;
    }

    @Override
    public void run() {
        players.values().forEach(this::save);

        val leaderManager = MyLittleFarm.getInstance().getLeaderManager();

        Arrays.stream(Top.values()).forEach(info -> {
            AtomicInteger number = new AtomicInteger(1);
            Map<String, Integer> map = new HashMap<>();

            String criteria = info == Top.REBIRTH ? "rebirths" : info.name().toLowerCase();

            leaderManager.getTopFromCriteria(criteria, 2000)
                    .keySet()
                    .forEach(name -> map.put(name, number.getAndIncrement()));

            top.put(info, map);
        });

        leaderManager.update();
    }
}
