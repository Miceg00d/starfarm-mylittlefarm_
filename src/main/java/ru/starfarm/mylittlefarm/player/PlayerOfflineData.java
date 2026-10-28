package ru.starfarm.mylittlefarm.player;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.format.message.MessageBuilder;
import ru.starfarm.core.util.format.message.click.ClickEvent;
import ru.starfarm.core.util.format.message.click.ClickEventType;
import ru.starfarm.core.util.format.message.hover.HoverEvent;
import ru.starfarm.core.util.format.message.hover.HoverEventType;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.i18n.item.ItemModel;
import ru.starfarm.mylittlefarm.i18n.messages.L10nMessages;
import ru.starfarm.mylittlefarm.objects.income.CultureIncome;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class PlayerOfflineData {

    private final String playerName;
    private final Set<CultureIncome> playerCultureIncomeSet;
    private final ConcurrentMap<DataType, Integer> dataLog;
    private final ConcurrentMap<DataType, Long> longDataLog;
    private final PlayerResolver playerResolver;
    private final long joinTime;
    private final Map<CultureIncome, Double> earningsMap = new ConcurrentHashMap<>();

    public PlayerOfflineData(String playerName, Set<CultureIncome> playerCultureIncomeSet, ConcurrentMap<DataType, Integer> dataLog, ConcurrentMap<DataType, Long> longDataLog) {
        this.playerName = playerName;
        this.playerCultureIncomeSet = new HashSet<>(playerCultureIncomeSet);
        this.dataLog = dataLog;
        this.longDataLog = longDataLog;
        this.playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(Bukkit.getPlayer(this.playerName));
        this.joinTime = System.currentTimeMillis();
        handleJoin();
    }


    public boolean hasOfflineReward() {
        if (!dataLog.containsKey(DataType.ABILITIES_MAX_OFFLINE_HOURS)) {
            log.info("No access to offline");
            return false;
        } else return offlineTimeMillis() >= 5000;
    }

    public boolean hasRewards() {
        return !playerCultureIncomeSet.isEmpty();
    }

    public void claimAllRewards(List<ItemModel> itemModels) {
        itemModels.forEach(itemModel -> {
            if (itemModel.key().equals("claim_reward")) return;
            CultureIncome cultureIncome = playerResolver.getCultureIncome(CultureType.ofItemKey(itemModel.key()));
            claimReward(cultureIncome, itemModel.jsonModel().name());
        });
    }

    public void claimReward(CultureIncome cultureIncome, String cultureName) {
        if (!this.playerCultureIncomeSet.contains(cultureIncome)) {
            return;
        }
        Player player = this.playerResolver.getHandle();
        L10nMessages messages = MyLittleFarm.getInstance().getMessageConfigs().get(this.playerResolver.language());
        String earnedIncome = StringUtils.formatDouble(earningsMap.get(cultureIncome));

        this.playerCultureIncomeSet.remove(cultureIncome);
        playerResolver.addBalance(earningsMap.get(cultureIncome));
        ChatUtil.sendMessage(player, String.format("§e" + cultureName + messages.messageByPathForMap("idle", "reward_received") + earnedIncome));
        log.info(String.format("Player %s claim offline reward: %s with %s earnings", this.playerName, cultureName, earnedIncome));
    }

    public long offlineTimeMillis() {
        long logoutTime = longData(DataType.LOGOUT_TIME);
        if (logoutTime == 0) return 0;
        long playerOfflineTime = this.joinTime - logoutTime;
        long playerMaxOfflineIncomeMillis = TimeUnit.HOURS.toMillis(data(DataType.ABILITIES_MAX_OFFLINE_HOURS));
        return Math.min(playerOfflineTime, playerMaxOfflineIncomeMillis);
    }

    public double earningForOffline(CultureIncome cultureIncome) {
        return earningsMap.get(cultureIncome);
    }

    private double incomeForOffline(CultureIncome cultureIncome) {
        double idleIncome = cultureIncome.generalMoneyAfterOffline(offlineTimeMillis());
        return idleIncome;
    }

    private void handleOfflineReward() {
        this.playerCultureIncomeSet.forEach(cultureIncome -> this.earningsMap.put(cultureIncome.clone(), incomeForOffline(cultureIncome)));
    }

    private void handleJoin() {
        if (hasOfflineReward()) {
            Player player = Bukkit.getPlayer(playerName);
            handleOfflineReward();

            String language = this.playerResolver.language();
            L10nMessages messages = MyLittleFarm.getInstance().getMessageConfigs().get(language);
            if (messages == null) {
                log.error("Messages is null with language: " + language);
            }
            StringBuilder sb = new StringBuilder()
                    .append(messages.messageByPathForMap("prefix", "mlf"))
                    .append(messages.messageByPathForMap("idle", "join"));
            ChatUtil.sendMessage(player, sb.toString());

            String rewardMessage = messages.messageByPathForMap("common", "clickable_reward");
            MessageBuilder message = new MessageBuilder("§a[" + rewardMessage + "]", new HoverEvent(HoverEventType.SHOW_TEXT, "§eClick there!"), new ClickEvent(ClickEventType.RUN_COMMAND, "/idlereward"));
            message.send(player);
            log.info(String.format("Offline earning per %s seconds have been calculated for player %s", TimeUnit.MILLISECONDS.toSeconds(offlineTimeMillis()), this.playerName));
        }
    }

    private int data(DataType dataType) {
        return dataLog.containsKey(dataType) ? dataLog.get(dataType) : 0;
    }

    private long longData(DataType dataType) {
        return longDataLog.containsKey(dataType) ? longDataLog.get(dataType) : 0;
    }


}
