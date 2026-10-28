package ru.starfarm.mylittlefarm.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.profile.Profile;
import ru.starfarm.core.task.GlobalTaskContext;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.*;
import ru.starfarm.mylittlefarm.objects.Achievement;
import ru.starfarm.mylittlefarm.objects.BlockInfo;
import ru.starfarm.mylittlefarm.objects.Upgrade;
import ru.starfarm.mylittlefarm.objects.cultures.CultureInfo;
import ru.starfarm.mylittlefarm.objects.achievements.GameAchievement;
import ru.starfarm.mylittlefarm.objects.animals.AnimalInfo;
import ru.starfarm.mylittlefarm.objects.animals.AnimalRegistry;
import ru.starfarm.mylittlefarm.objects.boosters.Booster;
import ru.starfarm.mylittlefarm.objects.cultures.CultureRegistry;
import ru.starfarm.mylittlefarm.objects.income.CultureIncome;
import ru.starfarm.mylittlefarm.objects.income.Income;
import ru.starfarm.mylittlefarm.objects.modifiers.Modifiers;
import ru.starfarm.mylittlefarm.objects.tasks.DiggerTask;
import ru.starfarm.mylittlefarm.player.info.AnimalData;
import ru.starfarm.mylittlefarm.player.info.EventInfo;
import ru.starfarm.mylittlefarm.player.info.ProductInfo;
import ru.starfarm.mylittlefarm.player.info.SkillInfo;
import ru.starfarm.mylittlefarm.rebirth.RebirthConfirmMenu;
import ru.starfarm.mylittlefarm.rebirth.RebirthManager;
import ru.starfarm.mylittlefarm.utils.StringUtils;
import ru.starfarm.mylittlefarm.utils.livedata.Observer;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Slf4j(topic = "MyLittleFarm_Player")
@ToString
public class PlayerResolver implements Observer<Income> {
    private final Player handle;
    private double balance;
    private double income;
    @Builder.Default
    private double comboMultiplier = 1.0;
    private final ConcurrentMap<CultureType, Integer> culturesLog;
    private final ConcurrentMap<UpgradeType, Integer> upgradesLog;
    private final ConcurrentMap<AnimalType, AnimalData> animalLog;
    private final ConcurrentMap<Product, ProductInfo> products;
    private final ConcurrentMap<Skill, SkillInfo> skills;
    private final ConcurrentMap<DataType, Integer> dataLog;
    private final ConcurrentMap<DataType, Long> longDataLog;
    private final ConcurrentMap<EventType, EventInfo> events;
    private final ConcurrentMap<Integer, Boolean> tasks;
    private int grass;
    private int hays;
    private int rebirths;
    @Builder.Default
    private int comboBlocks = 0;
    private long rbTokens;
    private long next;
    private long lastUpdate;
    private long happy;
    @Builder.Default
    private long lastGrassBreakTime = 0;
    private boolean hasUnlockedHay;
    private final ArrayList<Booster> boosterList;
    private final Set<CultureIncome> cultureIncomeSet;
    private PlayerOfflineData playerOfflineData;
    private String language;

    public Profile profile() {
        return ApiManager.getPlayerProfile(handle);
    }

    public void addBalance(double money) {
        this.balance += money;
    }

    public void addBalanceWithHologramUpdate(double money) {
        this.balance += money;
        
        // Обновляем голограммы только если баланс близок к цене какой-либо культуры
        if (money > 0 && shouldUpdateHologramsForBalance()) {
            Bukkit.getScheduler().runTaskLater(MyLittleFarm.getInstance(), () -> {
                updateAllCultureHolograms();
            }, 1L);
        }
    }

    private boolean shouldUpdateHologramsForBalance() {
        for (CultureType cultureType : CultureType.values()) {
            if (getCultureLevel(cultureType) < 100) { // Только для некупленных культур
                CultureInfo cultureInfo = MyLittleFarm.getInstance().getCultureRegistry()
                    .getCulture(cultureType, 1);

                if (cultureInfo == null) {
                    continue;
                }
                
                double discount = getData(DataType.RB_UPGRADE_CULTURE_DISCOUNT) * 0.01;
                double price = cultureInfo.price() * (1.0 - discount);

                if (balance >= price * 0.8 && balance <= price * 1.2) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addIncome(double income) {
        this.income += income;

        if (this.income >= 1000) {
            profile().giveAchievement(GameAchievement.MLF_START);
        }
        if (this.income >= 50000) {
            profile().giveAchievement(GameAchievement.MLF_NOT_POOR);
        }
    }

    public void addCultureIncome(CultureIncome cultureIncome) {
        cultureIncomeSet.remove(cultureIncome);
        cultureIncomeSet.add(cultureIncome);
        cultureIncome.observe(this);
    }

    public double getGlobalMoneyMultiplier() {
        int level = this.getData(DataType.RB_UPGRADE_MONEY_BOOSTER);
        return 1.0 + (level * 0.35);
    }

    public CultureIncome getCultureIncome(CultureType cultureType) {
        return cultureIncomeSet.stream().filter(ci -> ci.cultureType().equals(cultureType)).findFirst().orElse(null);
    }

    public void addGrass(int grass) {
        this.grass += grass;

        if (this.grass >= 10000) {
            profile().giveAchievement(GameAchievement.MLF_HERBALIST);
        }
    }
    public void resetFarmProgress() {
        val gamePlayer = MyLittleFarm.getInstance().getGamePlayer();

        if (gamePlayer.blocks.containsKey(handle)) {
            List<BlockInfo> currentBlocks = gamePlayer.blocks.get(handle);
            for (BlockInfo info : currentBlocks) {
                val originalBlock = info.getLocation().getBlock();
                handle.sendBlockChange(info.getLocation(), originalBlock.getType(), originalBlock.getData());
            }
            currentBlocks.clear();
        }
        int rbTokenBonus = getData(DataType.RB_UPGRADE_TOKEN_BONUS);
        int rbMoneyBooster = getData(DataType.RB_UPGRADE_MONEY_BOOSTER);
        int rbCultureDiscount = getData(DataType.RB_UPGRADE_CULTURE_DISCOUNT);
        int rbRebirthDiscount = getData(DataType.RB_UPGRADE_REBIRTH_DISCOUNT);

        this.balance = 100.0;
        this.income = 0.0;
        this.grass = 0;
        this.hays = 0;
        this.comboBlocks = 0;
        this.comboMultiplier = 1.0;

        this.culturesLog.clear();
        this.animalLog.clear();
        this.upgradesLog.clear();
        this.products.clear();
        this.skills.clear();
        this.tasks.clear();
        this.events.clear();
        this.boosterList.clear();

        if (this.cultureIncomeSet != null) {
            this.cultureIncomeSet.clear();
        }

        this.dataLog.clear();
        this.longDataLog.clear();

        this.setDataLevel(DataType.UPGRADES, 0);
        this.setDataLevel(DataType.ACHIEVEMENTS, 0);
        this.setDataLevel(DataType.TUTORIAL, 0);

        this.setDataLevel(DataType.TASK_PROGRESSION, 0);

        setDataLevel(DataType.RB_UPGRADE_TOKEN_BONUS, rbTokenBonus);
        setDataLevel(DataType.RB_UPGRADE_MONEY_BOOSTER, rbMoneyBooster);
        setDataLevel(DataType.RB_UPGRADE_CULTURE_DISCOUNT, rbCultureDiscount);
        setDataLevel(DataType.RB_UPGRADE_REBIRTH_DISCOUNT, rbRebirthDiscount);

        gamePlayer.reloadScoreboard(handle);

        val cultureRegistry = MyLittleFarm.getInstance().getCultureRegistry();

        cultureRegistry.loadHolographics(handle);

        for (CultureType type : CultureType.values()) {
            try {
                cultureRegistry.updateHolographic(handle, type);
            } catch (Exception ignored) {}
        }
    }

    public void updateCombo() {
        long now = System.currentTimeMillis();

        if (now - lastGrassBreakTime < 3000) {
            this.comboBlocks++;

            if (comboBlocks >= 8000) {
                this.comboMultiplier = 3.0;
            } else if (comboBlocks >= 500) {

                double extra = Math.floor((comboBlocks - 500) / 500.0) * 0.1;
                this.comboMultiplier = Math.min(3.0, 1.5 + extra);
            } else if (comboBlocks >= 400) {
                this.comboMultiplier = 1.5;
            } else if (comboBlocks >= 300) {
                this.comboMultiplier = 1.4;
            } else if (comboBlocks >= 200) {
                this.comboMultiplier = 1.3;
            } else if (comboBlocks >= 100) {
                this.comboMultiplier = 1.2;
            } else if (comboBlocks >= 50) {
                this.comboMultiplier = 1.1;
            } else {
                this.comboMultiplier = 1.0;
            }
        } else {

            this.comboBlocks = 1;
            this.comboMultiplier = 1.0;
        }

        this.lastGrassBreakTime = now;
    }

    public double getComboMultiplier() {
        if (System.currentTimeMillis() - lastGrassBreakTime > 3000) {
            this.comboBlocks = 0;
            this.comboMultiplier = 1.0;
        }
        return comboMultiplier;
    }

    public boolean hasReward(CultureType cultureType) {
        return playerOfflineData.getPlayerCultureIncomeSet().contains(getCultureIncome(cultureType));
    }

    public void addHays(int hays) {
        this.hays += hays;
    }

    public void removeBalance(double money) {
        this.balance -= money;
    }

    public void removeIncome(double income) {
        this.income -= income;
    }

    public int getCultureLevel(CultureType cultureType) {
        return culturesLog.containsKey(cultureType) ? culturesLog.get(cultureType) : 0;
    }

    public boolean hasMax(CultureType cultureType) {
        return getCultureLevel(cultureType) >= 100;
    }

    public boolean isHappyDay() {
        return System.currentTimeMillis() < happy;
    }

    public void addCultureLevel(CultureType cultureType, int level) {
        if (culturesLog.containsKey(cultureType)) {
            int newLevel = culturesLog.get(cultureType) + level;
            culturesLog.put(cultureType, newLevel);
            CultureIncome cultureIncome = getCultureIncome(cultureType);
            if (cultureIncome != null) {
                cultureIncome.increaseCultureCount(newLevel);
            }
        } else {
            culturesLog.put(cultureType, level);
        }
    }

    public AnimalData getAnimalData(AnimalType animal) {
        if (!animalLog.containsKey(animal)) {
            AnimalData newData = new AnimalData(0, 0, 0);
            animalLog.put(animal, newData);
            return newData;
        }
        return animalLog.get(animal);
    }

    public int getAnimalLevel(AnimalType animal) {
        return getAnimalData(animal).getCount();
    }

    public void addAnimalLevel(AnimalType animal, int level) {
        AnimalData data = getAnimalData(animal);

        data.setCount(data.getCount() + level);
        checkPurchasedAnimalForServerAchievement();
    }

    public ProductInfo getProductInfo(Product product) {
        if (!products.containsKey(product)) {
            ProductInfo newInfo = new ProductInfo(0, Maps.newTreeMap());
            products.put(product, newInfo);
            return newInfo;
        }
        return products.get(product);
    }

    public void addProductLevel(Product product, int level) {
        val productInfo = getProductInfo(product);

        productInfo.setCount(productInfo.getCount() + level);
    }

    public int getProductCount(AnimalType animal) {
        return getProductCount(animal.getProduct());
    }

    public int getProductCount(Product product) {
        return getProductInfo(product).getCount();
    }

    public void removeProduct(AnimalType animal, int count) {
        removeProduct(animal.getProduct(), count);
    }

    public void removeProduct(Product product, int count) {
        ProductInfo info = getProductInfo(product);
        info.setCount(Math.max(0, info.getCount() - count));
        products.put(product, info);
    }

    public void addProductCount(AnimalType animal, int count) {
        val productInfo = getProductInfo(animal.getProduct());

        productInfo.setCount(productInfo.getCount() + count);
    }

    public void addProductQueue(Product product, int time) {
        getProductInfo(product).addQueue(time);
    }

    public SkillInfo getSkillInfo(Skill skill) {
        return skills.containsKey(skill) ? skills.get(skill) : new SkillInfo(1, 0, Maps.newHashMap());
    }

    public int getUpgradeLevelByCultureType(CultureType cultureType) {
        return upgradesLog.containsKey(UpgradeType.ofCultureType(cultureType)) ? upgradesLog.get(UpgradeType.ofCultureType(cultureType)) : 0;
    }

    public int getUpgradeLevel(UpgradeType upgrade) {
        return upgradesLog.containsKey(upgrade) ? upgradesLog.get(upgrade) : 0;
    }

    public void addUpgradeLevel(UpgradeType upgrade, int level) {
        if (upgradesLog.containsKey(upgrade))
            upgradesLog.put(upgrade, upgradesLog.get(upgrade) + level);
        else
            upgradesLog.put(upgrade, level);
    }

    public void setUpgradeLevel(UpgradeType upgradeType, int level) {
        log.info(String.format("Set upgrade level for player %s with upgrade %s and %s level", handle.getName(), upgradeType, level));
        CultureType cultureType = CultureType.ofUpgradeType(upgradeType);
        if (cultureType != null) {
            CultureIncome cultureIncome = getCultureIncome(cultureType);
            if (cultureIncome != null) {
                cultureIncome.setNewUpgradeLevel(level);
            }
            try {
                MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(handle, cultureType);
            } catch (Exception ignored) {}

            updateOpenRebirthMenus();
        }
        upgradesLog.put(upgradeType, level);
    }

    public void setCultureLevel(CultureType cultureType, int level) {
        culturesLog.put(cultureType, level);
        if (cultureType.equals(CultureType.FOUNTAIN)) {
            for (CultureIncome income : cultureIncomeSet) {
                if (income != null) {
                    income.changeFountainMultiplier(level);
                }
            }
        } else {
            CultureIncome cultureIncome = getCultureIncome(cultureType);
            if (cultureIncome != null) {
                cultureIncome.increaseCultureCount(level);
            }
        }
    }

    public int getData(DataType data) {
        return dataLog.containsKey(data) ? dataLog.get(data) : 0;

    }

    public void addDataLevel(DataType data, int level) {
        if (dataLog.containsKey(data))
            dataLog.put(data, dataLog.get(data) + level);
        else
            dataLog.put(data, level);

        if (data == DataType.ACHIEVEMENTS) {
            if (getData(DataType.ACHIEVEMENTS) >= 36) {
                profile().giveAchievement(GameAchievement.MLF_HALF);
            }
        }

    }

    public void setDataLevel(DataType data, int level) {
        dataLog.put(data, level);
    }

    public void setLongDataLevel(DataType data, Long level) {
        longDataLog.put(data, level);
    }

    public Long getLongData(DataType data) {
        return longDataLog.containsKey(data) ? longDataLog.get(data) : 0;

    }

    public void addLongDataLevel(DataType data, Long level) {
        if (longDataLog.containsKey(data))
            longDataLog.put(data, longDataLog.get(data) + level);
        else
            longDataLog.put(data, level);

        if (data == DataType.ACHIEVEMENTS) {
            if (getData(DataType.ACHIEVEMENTS) >= 36) {
                profile().giveAchievement(GameAchievement.MLF_HALF);
            }
        }

    }

    public boolean isActiveEvent() {
        return !events.isEmpty();
    }

    public boolean hasActiveEvent(EventType eventType) {
        return events.containsKey(eventType);
    }

    public EventInfo getEventInfo(EventType eventType) {
        return events.containsKey(eventType) ? events.get(eventType) : new EventInfo(-1, Lists.newArrayList());
    }

    public void setActiveEvent(EventType event, int time) {
        if (hasActiveEvent(event)) {
            return;
        }
        events.put(event, new EventInfo(time, Lists.newArrayList()));
    }

    public void removeActiveEvent(EventType eventType) {
        events.remove(eventType);
    }

    public void activeEvent(EventType eventType) {
        if (hasActiveEvent(eventType)) {
            return;
        }
        switch (eventType) {
            case BLOOM: {
                ChatUtil.sendMessage(handle, "&8[&2MyLittleFarm&8] &dВсе начинает благоухать, кажется, началось цветение!");
                setActiveEvent(eventType, (int) TimeUnit.MINUTES.toSeconds(ThreadLocalRandom.current().nextInt(15, 31)));
                break;
            }
            case MORNING_DEW: {
                ChatUtil.sendMessage(handle, "&8[&2MyLittleFarm&8] &dНочь прошла и на вашу ферму спустилась &b&lУтренняя роса.");
                setActiveEvent(eventType, (int) TimeUnit.MINUTES.toSeconds(ThreadLocalRandom.current().nextInt(15, 31)));
                break;
            }
            case DIRTY: {
                ChatUtil.sendMessage(handle, "&8[&2MyLittleFarm&8] &cВас начали считать грязнулей! Срочно помойтесь, иначе с вами никто не захочет работать!");
                setActiveEvent(eventType, -1);
                break;
            }
        }

        if (eventType.isNegative()) {
            addDataLevel(DataType.NEGATIVE_EVENTS, 1);
            setDataLevel(DataType.POSITIVE_EVENTS, 0);
        } else {
            addDataLevel(DataType.POSITIVE_EVENTS, 1);
            setDataLevel(DataType.NEGATIVE_EVENTS, 0);
        }

        addDataLevel(DataType.EVENTS, 1);
        setNext(ThreadLocalRandom.current().nextInt(5400000, 7200000));
    }

    public boolean isTaskCompleted(DiggerTask task) {
        return tasks.getOrDefault(task.getId(), false);
    }

    public void setTaskCompleted(DiggerTask task) {
        tasks.put(task.getId(), true);
    }


    public void buyCulture(CultureType cultureType, boolean message) {
        if (getCultureLevel(cultureType) == 100) return;

        CultureRegistry cultureRegistry = cultureRegistry();
        int next = getCultureLevel(cultureType) + 1;

        double discount = 1.0 - (getData(DataType.RB_UPGRADE_CULTURE_DISCOUNT) * 0.01);
        double price = cultureRegistry.getCulture(cultureType, next).price() * discount;

        if (!isEnoughMoneyForPurchase(price)) {
            ChatUtil.sendMessage(handle, ChatUtil.color(cultureRegistry.getMessages(cultureType, 2)));
            return;
        }

        if (next == 1) createCultureIncome(cultureType);

        addCultureLevel(cultureType, 1);
        updateOtherCultureHolograms(cultureType);
        
        removeBalance(price);
        handleBlockAfterBuyCulture(cultureRegistry.getCulture(cultureType, next).locations());

        handleMiscAfterBuyCulture(next, message, cultureType);
    }

    public void buyAnimal(AnimalType animal) {
        int currentLevel = getAnimalLevel(animal);
        if (currentLevel >= 5) return;

        AnimalInfo info = MyLittleFarm.getInstance().getAnimalRegistry().getAnimal(animal, currentLevel + 1);

        if (info != null && balance >= info.getPrice()) {
            removeBalance(info.getPrice());


            AnimalData data = getAnimalData(animal);
            data.setCount(currentLevel + 1);
            animalLog.put(animal, data);

            handle.playSound(handle.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
        }
    }

    public void calculateIncome() {
        double incomeSum = 0;
        double rbMultiplier = getGlobalMoneyMultiplier();
        for (CultureIncome ci : cultureIncomeSet) {
            double base = (ci.cultureIncome() + ci.incomeIncreasedByUpgrades() + ci.incomeIncreasedByBoosters() + ci.incomeIncreasedByModifier()) * ci.fountainModifier();
            incomeSum += (base * rbMultiplier);
        }
        setIncome(incomeSum);
    }

    public void calculateUpgrades() {
        upgradesLog.keySet().stream()
                .filter(upg -> upg.name().contains("_BOOSTER") &&
                        !upg.name().contains("GRASS") &&
                        !upg.name().contains("MONEY"))
                .forEach(upg -> {
                    int count = getUpgrade(upg);
                    if (getUpgradeLevel(upg) > count) setUpgradeLevel(upg, count);
                });
    }
    public int getUpgrade(UpgradeType upgradeType) {
        CultureType cultureType;
        AnimalType animalType;
        int count;

        cultureType = CultureType.ofUpgradeType(upgradeType);
        if (cultureType == null) {
            animalType = AnimalType.ofUpgradeType(upgradeType);
            count = getAnimalLevel(animalType);
        } else {
            count = getCultureLevel(cultureType);
        }

        if (count == 0) {
            return 0;
        } else if (count > 0 && count <= 10) {
            return 100;
        } else if (count > 10 && count <= 25) {
            return 500;
        } else if (count > 25 && count <= 60) {
            return 3500;
        }

        return 25000;
    }

    public void giveAchievement(Achievement achievement) {
        ChatUtil.sendMessage(handle,
                "\n&e" +
                        "&7-----------------------------------" +
                        "\n&a&lНовая цель достигнута: &e&l" + achievement.getId() +
                        "\n&7" + achievement.getFooter() +
                        "\n &f" +
                        "\n&e&lПолучено: +" + StringUtils.formatDouble(achievement.getMoney()) + "$" +
                        "\n&7-----------------------------------" +
                        "\n  &a"
        );

        addDataLevel(DataType.ACHIEVEMENTS, 1);
        addBalance(achievement.getMoney());
    }

    public void giveUpgrade(Upgrade upgrade) {
        handle.sendMessage(ChatUtil.color(
                "\n&e" +
                        "&7----------------------------------------------" +
                        "\n&a&lНовое улучшение доступно: &e&l" + upgrade.getId() +
                        "\n&7" + String.join("\n&7", upgrade.getLore()) +
                        "\n &f" +
                        "\n&7----------------------------------------------" +
                        "\n  &a"
        ));
    }

    public int getFountainModifier() {
        return getCultureLevel(CultureType.FOUNTAIN) > 0 ? getCultureLevel(CultureType.FOUNTAIN) : 1;
    }

    public Tasks getActiveTask() {
        return Tasks.getByOrdinal((int) getData(DataType.ACTIVE_TASK));
    }

    public TaskType getActiveTaskType() {
        return getActiveTask().getType();
    }

    public long getTaskProgression() {
        return getData(DataType.TASK_PROGRESSION);
    }

    public void addTaskProgression(long value) {
        setDataLevel(DataType.TASK_PROGRESSION, (int) (getTaskProgression() + value));
    }

    public void giveTask() {
        setDataLevel(DataType.ACTIVE_TASK, Tasks.getRandom().ordinal());
    }

    private CultureRegistry cultureRegistry() {
        return MyLittleFarm.getInstance().getCultureRegistry();
    }

    public void setupPlayer() {
        checkPurchasedAnimalForServerAchievement();
        setupBoosters();
        setupCultureIncome();
        if (getData(DataType.TUTORIAL) >= 4) setDataLevel(DataType.ABILITIES_MAX_OFFLINE_HOURS, 2);
        GlobalTaskContext.asyncAfter(20, task -> {
            setupOfflineData();
        });
    }

    private void setupCultureIncome() {
        calculateUpgrades();
        Map<CultureType, List<Booster>> boosterMap = boosterMap();
        if (!culturesLog.isEmpty()) {
            culturesLog.keySet().stream()
                    .filter(cultureType -> CultureType.isPlant(cultureType) && getCultureLevel(cultureType) > 0)
                    .forEach(cultureType -> {
                                int cultureLevel = culturesLog.get(cultureType);
                        if (cultureLevel == 0) return;
                        int upgradeLevel = getUpgradeLevelByCultureType(cultureType);
                                ArrayList<Booster> boosterList = boosterMap.containsKey(cultureType) ? new ArrayList<>(boosterMap.get(cultureType)) : new ArrayList<>();
                                CultureIncome cultureIncome = new CultureIncome(cultureType, cultureLevel, Modifiers.getModifier(), boosterList, upgradeLevel, getFountainModifier());
                        addCultureIncome(cultureIncome);
                            }
                    );
        }
    }

    private void createCultureIncome(CultureType cultureType) {
        CultureIncome cultureIncome = new CultureIncome(cultureType, 1, Modifiers.getModifier(), new ArrayList<>(), 0, getFountainModifier());
        cultureIncome.observe(this);
        addCultureIncome(cultureIncome);
    }

    public String language() {
        return this.language;
    }
    
    public ConcurrentMap<CultureType, Integer> getCulturesLog() {
        return culturesLog;
    }

    private void updateOpenRebirthMenus() {
        if (handle.getOpenInventory() != null && handle.getOpenInventory().getTopInventory() != null) {
            try {
                String title = handle.getOpenInventory().getTitle();
                if (title.contains("Подтверждение") || title.contains("Перерождение")) {
                    Bukkit.getScheduler().runTaskLater(MyLittleFarm.getInstance(), () -> {
                        new RebirthConfirmMenu(new RebirthManager()).openInventory(handle);
                    }, 1L);
                }
            } catch (Exception e) {
                try {
                    if (handle.getOpenInventory().getTopInventory().getName().contains("Подтверждение")) {
                        Bukkit.getScheduler().runTaskLater(MyLittleFarm.getInstance(), () -> {
                            new RebirthConfirmMenu(new RebirthManager()).openInventory(handle);
                        }, 1L);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void updateAllCultureHolograms() {
        for (CultureType cultureType : CultureType.values()) {
            try {
                MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(handle, cultureType);
            } catch (Exception ignored) {}
        }
    }

    private void updateOtherCultureHolograms(CultureType excludeType) {
        for (CultureType cultureType : CultureType.values()) {
            if (cultureType != excludeType) {
                try {
                    MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(handle, cultureType);
                } catch (Exception ignored) {}
            }
        }
    }

    public boolean isEnoughMoneyForPurchase(double price) {
        return balance >= price;
    }


    private void setupBoosters() {
        for (Booster booster : boosterList) {
            booster.startBooster(getCultureIncome(booster.cultureType()));
        }
    }

    private void setupOfflineData() {
        this.playerOfflineData = new PlayerOfflineData(handle.getName(), cultureIncomeSet, dataLog, longDataLog);
    }


    public void setLogoutTime() {
        longDataLog.put(DataType.LOGOUT_TIME, System.currentTimeMillis());
    }

    private Map<CultureType, List<Booster>> boosterMap() {
        Map<CultureType, List<Booster>> boosters = new HashMap<>();
        boosterList.forEach(booster -> {
            CultureType boosterCultureType = booster.cultureType();
            List<Booster> boosterArrayList = boosters.containsKey(boosterCultureType) ? boosters.get(boosterCultureType) : new ArrayList<>();
            boosterArrayList.add(booster);
            boosters.put(booster.cultureType(), boosterArrayList);
        });
        return boosters;
    }

    private void handleBlockAfterBuyCulture(List<String> locations) {
        if (locations != null) {
            for (String s : locations) {
                String[] block = s.split(" ");
                handle.sendBlockChange(new Location(Bukkit.getWorld(block[2]), Double.parseDouble(block[3]), Double.parseDouble(block[4]), Double.parseDouble(block[5])), Integer.parseInt(block[0]), Byte.parseByte(block[1]));
                MyLittleFarm.getInstance().getGamePlayer().getBlocks().get(handle)
                        .add(new BlockInfo(
                                Integer.parseInt(block[0]), Byte.parseByte(block[1]),
                                new Location(Bukkit.getWorld(block[2]), Double.parseDouble(block[3]),
                                        Double.parseDouble(block[4]), Double.parseDouble(block[5]))
                        ));
            }
        }
    }

    private void handleMiscAfterBuyCulture(int next, boolean message, CultureType cultureType) {
        checkPurchasedCultureForAchievementAndUpgrade(next, message, cultureType);
        checkPurchasedCultureForServerAchievement();
        MyLittleFarm.getInstance().getCultureRegistry().updateHolographic(handle, cultureType);
        handle.playSound(handle.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);

        updateOpenRebirthMenus();
    }

    public void checkPurchasedAnimalForServerAchievement() {
        if (getAnimalLevel(AnimalType.CHICKEN) >= 5 && getAnimalLevel(AnimalType.RABBIT) >= 5) {
            profile().giveAchievement(GameAchievement.MLF_MASTER_OF_ANIMAL);
        }
        if (getAnimalLevel(AnimalType.SHEEP) >= 5 && getAnimalLevel(AnimalType.PIG) >= 5) {
            profile().giveAchievement(GameAchievement.MLF_UNDER_THE_MEAT);
        }
        if (getAnimalLevel(AnimalType.COW) >= 5 && getAnimalLevel(AnimalType.HORSE) >= 5) {
            profile().giveAchievement(GameAchievement.MLF_GOURMEAT);
        }
    }

    public void checkPurchasedCultureForServerAchievement() {
        if (getCultureLevel(CultureType.POTATO) >= 100 && getCultureLevel(CultureType.CARROT) >= 100) {
            profile().giveAchievement(GameAchievement.MLF_VEGETABLE_GROWLE);
        }
        if (getCultureLevel(CultureType.PUMPKIN) >= 100 && getCultureLevel(CultureType.MELON) >= 100) {
            profile().giveAchievement(GameAchievement.MLF_PERFECT_COMBINATION);
        }
        if (getCultureLevel(CultureType.RED_MUSHROOM) >= 100 && getCultureLevel(CultureType.BROWN_MUSHROOM) >= 100) {
            profile().giveAchievement(GameAchievement.MLF_MUSHROOMS);
        }
        if (getCultureLevel(CultureType.ROSE) >= 100 && getCultureLevel(CultureType.LILY) >= 100) {
            profile().giveAchievement(GameAchievement.MLF_FAMOUS_GARDENER);
        }
        if (Arrays.stream(CultureType.values()).filter(CultureType::isPlant).allMatch(this::hasMax)) {
            if (!profile().getAchievements().contains(GameAchievement.MLF_LEGENDARY_FARMER.toString())) {
                profile().addCoins(6000);
                profile().addExp(2000);
            }
            profile().giveAchievement(GameAchievement.MLF_LEGENDARY_FARMER);
        }
    }

    public void checkPurchasedCultureForAchievementAndUpgrade(int next, boolean message, CultureType culture) {
        CultureRegistry cultureRegistry = MyLittleFarm.getInstance().getCultureRegistry();
        if (next == 1) {
            if (message) {
                ChatUtil.sendMessage(handle, ChatUtil.color(cultureRegistry.getMessages(culture, 0)));
            }
            giveAchievement(Achievement.getAchievement(cultureRegistry.getAchievements(culture, 0)));
            giveUpgrade(Upgrade.getUpgrade(cultureRegistry.getUpgrades(culture, 0)));
        } else if (next == 10) {
            giveAchievement(Achievement.getAchievement(cultureRegistry.getAchievements(culture, 1)));
            giveUpgrade(Upgrade.getUpgrade(cultureRegistry.getUpgrades(culture, 1)));
        } else if (next == 25) {
            giveAchievement(Achievement.getAchievement(cultureRegistry.getAchievements(culture, 2)));
            giveUpgrade(Upgrade.getUpgrade(cultureRegistry.getUpgrades(culture, 2)));
        } else if (next == 50) {
            giveAchievement(Achievement.getAchievement(cultureRegistry.getAchievements(culture, 3)));
        } else if (next == 60) {
            giveUpgrade(Upgrade.getUpgrade(cultureRegistry.getUpgrades(culture, 3)));
        } else if (next == 100) {
            giveAchievement(Achievement.getAchievement(cultureRegistry.getAchievements(culture, 4)));
            giveUpgrade(Upgrade.getUpgrade(cultureRegistry.getUpgrades(culture, 4)));
        }
    }

    @Override
    public void onChanged(Income income) {
        calculateIncome();
    }
}


