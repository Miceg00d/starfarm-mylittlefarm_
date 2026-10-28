package ru.starfarm.mylittlefarm.i18n.json;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

@Slf4j(topic = "Placeholders")
public class PlaceholderValue {

    private final Player player;
    private final PlaceholderType placeholderType;
    private final String key;

    public PlaceholderValue(Player player, PlaceholderType placeholderType, String key) {
        this.player = player;
        this.placeholderType = placeholderType;
        this.key = key;
    }

    public String handleValue() {
        if (this.placeholderType.equals(PlaceholderType.AFK_INCOME)) {
            return handleOfflineEarning();
        } else if (this.placeholderType.equals(PlaceholderType.PLAYER_NAME)) {
            return player.getName();
        } else {
            log.error("Unknown placeholder for handling");
            return "N/A";
        }
    }

    public String handleOfflineEarning() {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        CultureType cultureType = CultureType.ofItemKey(this.key);
        if (cultureType != null && CultureType.isPlant(cultureType)) {
            double afkIncome = playerResolver.getCultureIncome(cultureType).offlineEarning(playerResolver.getPlayerOfflineData().offlineTimeMillis());
            String earning = StringUtils.formatDouble(afkIncome);
            return earning;
        } else return "N/A";
    }
}
