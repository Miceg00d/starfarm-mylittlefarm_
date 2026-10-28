package ru.starfarm.mylittlefarm.rebirth;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.Product;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

public class RebirthManager {

    public long getCost(PlayerResolver res) {
        long baseCost = (long) (1000000L * Math.pow(1.5, res.getRebirths()));
        double discount = 1.0 - (res.getData(DataType.RB_UPGRADE_REBIRTH_DISCOUNT) * 0.006);
        return (long) (baseCost * discount);
    }

    public void process(Player player) {
        PlayerResolver res = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        if (res == null) return;

        long cost = getCost(res);

        if (res.getBalance() < cost) {
            ChatUtil.sendMessage(player, "&8[&bFarm&8] &cНедостаточно денег! Нужно: &e$" + StringUtils.formatDouble(cost));
            return;
        }

        int currentTutorialStep = res.getData(DataType.TUTORIAL);

        int tokenBonus = res.getData(DataType.RB_UPGRADE_TOKEN_BONUS);
        int totalTokensReward = (tokenBonus + 1) * 5;

        res.resetFarmProgress();
        res.setDataLevel(DataType.TUTORIAL, currentTutorialStep);

        res.setRebirths(res.getRebirths() + 1);
        res.setRbTokens(res.getRbTokens() + totalTokensReward);
        res.addProductLevel(Product.AMULET_KEY, 1);

        resetGrassAdvancements(player);

        MyLittleFarm.getInstance().getGamePlayer().save(res);
        MyLittleFarm.getInstance().getGamePlayer().reloadScoreboard(player);

        player.sendTitle("§b§lВы переродились", "§fВы получили §d" + totalTokensReward + " Токен. §e+1 ключ от кейса!", 10, 60, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f);
        player.closeInventory();
    }
    
    private void resetGrassAdvancements(Player player) {
        Advancement advancement = Bukkit.getAdvancement(new NamespacedKey("mlf", "grass_upgrade_1"));
        if (advancement != null) {
            player.getAdvancementProgress(advancement).revokeCriteria("impossible");
        }
    }
}
