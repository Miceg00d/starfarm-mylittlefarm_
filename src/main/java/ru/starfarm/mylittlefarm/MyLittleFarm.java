package ru.starfarm.mylittlefarm;

import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.CorePlugin;
import ru.starfarm.core.chat.IChatService;
import ru.starfarm.core.database.DatabaseApi;
import ru.starfarm.core.database.DatabaseCredentials;
import ru.starfarm.core.database.connection.DatabaseConnectionHandler;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import ru.starfarm.core.profile.IProfileService;
import ru.starfarm.core.profile.achievement.AchievementSection;
import ru.starfarm.core.profile.group.DonateGroup;
import ru.starfarm.core.scoreboard.IScoreboardService;
import ru.starfarm.core.scoreboard.ScoreboardService;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.commands.*;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.EventType;
import ru.starfarm.mylittlefarm.i18n.CommonStrings;
import ru.starfarm.mylittlefarm.i18n.menu.MenuConfig;
import ru.starfarm.mylittlefarm.i18n.messages.L10nMessages;
import ru.starfarm.mylittlefarm.listeners.ComboDisplayTask;
import ru.starfarm.mylittlefarm.listeners.GlobalCanceller;
import ru.starfarm.mylittlefarm.listeners.GlobalListener;
import ru.starfarm.mylittlefarm.listeners.PacketListener;
import ru.starfarm.mylittlefarm.objects.Achievement;
import ru.starfarm.mylittlefarm.objects.FakeEntity;
import ru.starfarm.mylittlefarm.objects.GameItem;
import ru.starfarm.mylittlefarm.objects.Upgrade;
import ru.starfarm.mylittlefarm.objects.achievements.GameAchievement;
import ru.starfarm.mylittlefarm.objects.animals.AnimalRegistry;
import ru.starfarm.mylittlefarm.objects.cultures.CultureRegistry;
import ru.starfarm.mylittlefarm.objects.modifiers.Modifiers;
import ru.starfarm.mylittlefarm.objects.amulets.AmuletCaseManager;
import ru.starfarm.mylittlefarm.objects.skills.FishingRegistry;
import ru.starfarm.mylittlefarm.player.GamePlayer;
import ru.starfarm.mylittlefarm.top.BoardAPI;
import ru.starfarm.mylittlefarm.top.leader.LeaderManager;
import ru.starfarm.mylittlefarm.tower.Donate;
import ru.starfarm.mylittlefarm.tower.PacketHandler;
import ru.starfarm.mylittlefarm.utils.config.ConfigManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter @Slf4j
public class MyLittleFarm extends CorePlugin {
    public static @Getter MyLittleFarm instance;

    private DatabaseConnectionHandler handler;
    private GamePlayer gamePlayer;
    private ConfigManager configManager;
    private CultureRegistry cultureRegistry;
    private AnimalRegistry animalRegistry;
    private FishingRegistry fishingRegistry;
    private AmuletCaseManager amuletCaseManager;
    private FakeEntity fakeEntity;
    private LeaderManager leaderManager;
    private GameItem gameItem;
    private final Map<String, MenuConfig> menuConfigs = new HashMap<>();
    private final Map<String, L10nMessages> messageConfigs = new HashMap<>();
    private CommonStrings commonStrings;

    public static final Set<Player> BUILD_MODE = new HashSet<>();

    public @Getter boolean loaded;

    @Override
    protected void enable() {
        instance = this;
        handler = DatabaseApi.createConnection(DatabaseCredentials.from(
                getConfig().getString("mysql.user"),
                getConfig().getString("mysql.host"),
                getConfig().getString("mysql.password"),
                getConfig().getString("mysql.scheme")
        ));
        loadCommonStrings();
        loadMenuConfigs();
        loadMessageConfigs();
        gamePlayer = new GamePlayer(this);
        configManager = new ConfigManager();
        cultureRegistry = new CultureRegistry();
        animalRegistry = new AnimalRegistry();
        fishingRegistry = new FishingRegistry();
        amuletCaseManager = new AmuletCaseManager(this);
        fakeEntity = new FakeEntity();
        leaderManager = new LeaderManager(this);
        leaderManager.createIfNotExists();
        gameItem = new GameItem();

        registerListeners(
                new GlobalListener(), new GlobalCanceller()
        );

        registerCommands(
                new BuildCommand(), new SpeedCommand(), new AvistCommand(),
                new StatisticCommand(), new AdminCommand(), new TopCommand(),
                new OpenIdleInventoryCommand(), new AmuletCommand()
        );

        registerService(IScoreboardService.class, new ScoreboardService());
        getServiceManager().unregisterService(IChatService.class);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this, 5));

        Modifiers.initialize(this);
        loadAchievements();
        loadUpgrades();
        registerTasks();
        new PacketHandler();
        new ComboDisplayTask(this).start();

        Bukkit.getScheduler().runTaskLater(this, this::copyAdvancements, 100L);

        loaded = true;
    }

    @Override
    protected void disable() {
        Bukkit.getOnlinePlayers().forEach(gamePlayer::unload);

        Set<Chunk> chunks = new HashSet<>();
        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
            if (entity.getType() == EntityType.ARMOR_STAND || entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.ITEM_FRAME) {
                return;
            }
            Chunk chunk = entity.getLocation().getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
                chunks.add(chunk);
            }
            entity.remove();
        }));
        chunks.forEach(Chunk::unload);
        handler.disconnect();
        BoardAPI.stop();
    }

    void registerListeners(Listener... listeners) {
        Arrays.asList(listeners).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void registerTasks() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () ->
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !gamePlayer.getVisit().containsKey(player))
                        .map(gamePlayer::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(playerResolver -> {
                            if (playerResolver.getHandle() == null) return;

                            double income = playerResolver.getIncome();
                            if (playerResolver.hasActiveEvent(EventType.BLOOM))
                                income *= 2;

                            playerResolver.addBalanceWithHologramUpdate(income);
                        }), 20L, 20L);
    }

    @Override
    public void handleTowerConnect() {
        ApiManager.newDonateAbilitiesBuilder("§2MyLittleFarm", Material.WHEAT)
                .ability(DonateGroup.UNIQUE,
                        "&c▸ &fБонус к общему модификатору производства: &a3%",
                        "&c▸ &fДоступ к двойному прыжку",
                        "&c▸ &fПовышенный шанс на положительное событие"
                )
                .ability(DonateGroup.SPONSOR_PLUS,
                        "&c▸ &fБонус к общему модификатору производства: &a2.25%",
                        "&c▸ &fДоступ к двойному прыжку"
                )
                .ability(DonateGroup.SPONSOR,
                        "&c▸ &fБонус к общему модификатору производства: &a1.75%",
                        "&c▸ &fДоступ к двойному прыжку"
                )
                .ability(DonateGroup.ELITE_PLUS,
                        "&c▸ &fБонус к общему модификатору производства: &a1.5%",
                        "&c▸ &fДоступ к двойному прыжку"
                )
                .ability(DonateGroup.ELITE,
                        "&c▸ &fБонус к общему модификатору производства: &a1.25%",
                        "&c▸ &fДоступ к двойному прыжку"
                )
                .ability(DonateGroup.PREMIUM_PLUS,
                        "&c▸ &fБонус к общему модификатору производства: &a1%"
                )
                .ability(DonateGroup.PREMIUM,
                        "&c▸ &fБонус к общему модификатору производства: &a0.75%"
                )
                .ability(DonateGroup.VIP_PLUS,
                        "&c▸ &fБонус к общему модификатору производства: &a0.5%"
                )
                .ability(DonateGroup.VIP,
                        "&c▸ &fБонус к общему модификатору производства: &a0.25%"
                )
                .register();
        ApiManager.appendAchievements(
                new AchievementSection("MLF", "§aMyLittleFarm", Material.WHEAT), GameAchievement.class
        );
        loadDonate();

    }

    private void loadDonate() {
        ApiManager.newDonateBuilder("Доступ к полёту")
                .slot(0)
                .icon(new ItemStack(Material.FEATHER))
                .description(
                        "§fПорхай как бабочка, жаль как оса.",
                        "",
                        "§fСрок действия: §aнеограничено"
                )
                .accessor(player -> gamePlayer.getPlayer(player).getData(DataType.ABILITIES_FLY) != 1)
                .callback(ctx -> {
                    Player player = ctx.getPlayer();

                    gamePlayer.getPlayer(player).setDataLevel(DataType.ABILITIES_FLY, 1);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    Donate.buy(player, ctx.getDonate().getName(), ctx.getDonate().getPrice().apply(player));
                })
                .price(250)
                .register();
        ApiManager.newDonateBuilder("§6Двойной прыжок")
                .slot(1)
                .icon(new ItemStack(Material.GOLD_BOOTS))
                .description(
                        "§fПодбрасывает Вас в направлении взгляда",
                        "§cОтключается при покупке флая.",
                        "",
                        "§fСрок действия: §aнеограничено"
                )
                .accessor(player -> gamePlayer.getPlayer(player).getData(DataType.ABILITIES_DOUBLE_JUMP) != 1 && !IProfileService.getService().getProfile(player).hasGroup(DonateGroup.ELITE))
                .callback(ctx -> {
                    gamePlayer.getPlayer(ctx.getPlayer()).setDataLevel(DataType.ABILITIES_DOUBLE_JUMP, 1);
                    Donate.buy(ctx.getPlayer(), ctx.getDonate().getName(), ctx.getDonate().getPrice().apply(ctx.getPlayer()));
                })
                .price(100)
                .register();
        ApiManager.newDonateBuilder("§aБухгалтерия")
                .slot(2)
                .icon(new ItemStack(Material.BOOK))
                .description(
                        "§fКупив это, вы получаете возможность улучшать свою",
                        "§fферму, не выходя из амбара!",
                        "",
                        "§fСрок действия: §aнеограничено"
                )
                .accessor(player -> gamePlayer.getPlayer(player).getData(DataType.ABILITIES_BOOK) != 1)
                .callback(ctx -> {
                    Player player = ctx.getPlayer();

                    gamePlayer.getPlayer(player).setDataLevel(DataType.ABILITIES_BOOK, 1);
                    player.getInventory().addItem(MyLittleFarm.getInstance().getGameItem().getItem("book"));
                    Donate.buy(player, ctx.getDonate().getName(), ctx.getDonate().getPrice().apply(player));
                })
                .price(300)
                .register();
        ApiManager.newDonateBuilder("§5Телефон")
                .slot(3)
                .icon(new ItemStack(Material.NETHER_BRICK_ITEM))
                .description(
                        "§fТелефонная станция позволяет вам общаться с",
                        "§fБабой Маней дистанционно! Стоит лишь набрать её и она тут же",
                        "§fвам ответит!",
                        "",
                        "§fСрок действия: §aнеограничено"
                )
                .accessor(player -> gamePlayer.getPlayer(player).getData(DataType.ABILITIES_TELEPHONE) != 1)
                .callback(ctx -> {
                    Player player = ctx.getPlayer();

                    gamePlayer.getPlayer(player).setDataLevel(DataType.ABILITIES_TELEPHONE, 1);
                    player.getInventory().addItem(MyLittleFarm.getInstance().getGameItem().getItem("telephone"));
                    Donate.buy(player, ctx.getDonate().getName(), ctx.getDonate().getPrice().apply(player));
                })
                .price(150)
                .register();
        ApiManager.newDonateBuilder("§aСкорость ходьбы")
                .slot(4)
                .icon(new ItemStack(Material.EXP_BOTTLE))
                .description(
                        "§fКупив это, вы получаете возможность перемещаться по",
                        "§fсвоей ферме со скоростью света!",
                        "",
                        "§fСрок действия: §aнеограничено"
                )
                .accessor(player -> gamePlayer.getPlayer(player).getData(DataType.ABILITIES_SPEED) != 1)
                .callback(ctx -> {
                    Player player = ctx.getPlayer();

                    gamePlayer.getPlayer(player).setDataLevel(DataType.ABILITIES_SPEED, 1);
                    ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &aТеперь вы можете изменять скорость ходьбы. Команда - /speed.");
                    Donate.buy(player, ctx.getDonate().getName(), ctx.getDonate().getPrice().apply(player));
                })
                .price(100)
                .register();
        ApiManager.newDonateBuilder("§aСчастливый день")
                .slot(5)
                .icon(new ItemStack(Material.WATCH))
                .description(
                        "§fВ течении этого дня вы не будете получать негативные события.",
                        "",
                        "§fСрок действия: §a1 день"
                )
                .countedType(100, 1, 25)
                .accessor(player -> gamePlayer.getPlayer(player).getData(DataType.OFFLINE_INCOME) != 1)
                .callback(ctx -> {
                    Player player = ctx.getPlayer();

                    long time = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(ctx.getCount());
                    gamePlayer.getPlayer(player).setHappy(time);
                    ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &aВы не будете получать отрицательных событий до &2%s&a.",
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
                    );

                    Donate.buy(player, ctx.getDonate().getName(), ctx.getDonate().getPrice().apply(player));
                })
                .price(25)
                .register();

    }

    private void loadCommonStrings() {
        this.commonStrings = CommonStrings.getInstance();
    }

    private void loadMenuConfigs() {
        MenuConfig englishConfig = new MenuConfig("en");
        MenuConfig russianConfig = new MenuConfig("ru");
        menuConfigs.put("en", englishConfig);
        menuConfigs.put("ru", russianConfig);
    }

    private void loadMessageConfigs() {
        L10nMessages english = new L10nMessages("en");
        L10nMessages russian = new L10nMessages("ru");
        messageConfigs.put("en", english);
        messageConfigs.put("ru", russian);
    }

    private void loadAchievements() {
        ConfigurationSection section = configManager.getAchievementConfig().getConfiguration();
        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((key) -> {
                ConfigurationSection keySection = section.getConfigurationSection(key);
                new Achievement(key, keySection);
            });
        }
    }

    private void loadUpgrades() {
        ConfigurationSection section = configManager.getUpgradeConfig().getConfiguration();
        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((key) -> {
                ConfigurationSection keySection = section.getConfigurationSection(key);
                new Upgrade(key, keySection);
            });
        }
    }
    
    private void copyAdvancements() {
        try {
            File datapackDir = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "datapacks/mlf/data/mlf/advancements");
            if (!datapackDir.exists()) {
                datapackDir.mkdirs();
            }

            File targetFile = new File(datapackDir, "grass_upgrade_1.json");
            InputStream inputStream = getResource("advancements/grass_upgrade_1.json");
            
            if (inputStream != null) {
                Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                inputStream.close();
                
                if (!targetFile.exists()) {
                    log.info("[MyLittleFarm] Advancement файл создан. ТРЕБУЕТСЯ ПЕРЕЗАГРУЗКА СЕРВЕРА для активации!");
                    log.info("[MyLittleFarm] Перезапустите сервер чтобы advancement заработали.");
                } else {
                    log.info("[MyLittleFarm] Advancement файл обновлен: " + targetFile.getAbsolutePath());
                }
            } else {
                log.error("[MyLittleFarm] Не найден resource advancements/grass_upgrade_1.json");
            }

            File mcmetaFile = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "datapacks/mlf/pack.mcmeta");
            if (!mcmetaFile.exists()) {
                mcmetaFile.getParentFile().mkdirs();
                Files.write(mcmetaFile.toPath(), 
                    "{\"pack\":{\"pack_format\":3,\"description\":\"MyLittleFarm Advancements\"}}".getBytes());
                log.info("[MyLittleFarm] Создан pack.mcmeta для датапака");
            }
            // Перезагружаем датапаки через reload
            Bukkit.getScheduler().runTaskLater(this, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
                log.info("[MyLittleFarm] Сервер перезагружен для активации датапака");
            }, 60L);
            
        } catch (Exception e) {
            log.error("[MyLittleFarm] Ошибка копирования advancement: " + e.getMessage(), e);
        }
    }
}
