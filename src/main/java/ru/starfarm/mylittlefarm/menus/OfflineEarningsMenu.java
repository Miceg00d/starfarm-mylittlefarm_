package ru.starfarm.mylittlefarm.menus;

import lombok.extern.slf4j.Slf4j;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.i18n.item.ItemModel;
import ru.starfarm.mylittlefarm.i18n.item.MenuItem;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderType;
import ru.starfarm.mylittlefarm.i18n.menu.GameMenu;
import ru.starfarm.mylittlefarm.i18n.messages.L10nMessages;
import ru.starfarm.mylittlefarm.objects.income.CultureIncome;
import ru.starfarm.mylittlefarm.player.PlayerOfflineData;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OfflineEarningsMenu extends GameMenu {

    private static volatile OfflineEarningsMenu instance = null;

    public OfflineEarningsMenu(String language) {
        super(language, "idle");
    }

    @Override
    protected void addItem(ItemModel itemModel, PlayerResolver playerResolver) {
        Map<PlaceholderType, String> values = new HashMap<>();
        CultureType cultureType = CultureType.ofItemKey(itemModel.key());
        PlayerOfflineData playerOfflineData = playerResolver.getPlayerOfflineData();
        if (cultureType != null && CultureType.isPlant(cultureType)) {
            CultureIncome cultureIncome = playerResolver.getCultureIncome(cultureType);
            double afkIncome = playerOfflineData.earningForOffline(cultureIncome);
            String earning = StringUtils.formatDouble(afkIncome);
            values.put(PlaceholderType.AFK_INCOME, earning);

            this.gameContainer.putItem(new MenuItem(itemModel, null, (container, event) -> {
                playerOfflineData.claimReward(cultureIncome, itemModel.jsonModel().name());
                this.gameContainer.removeItem(itemModel);
                this.gameContainer.updateInventory(playerResolver.getHandle());
            }
            ), values);
        }
    }


    @Override
    public void openMenu(PlayerResolver playerResolver) {
        super.createGameContainer(playerResolver);
        if (!playerResolver.getPlayerOfflineData().hasOfflineReward()) {
            L10nMessages messages = MyLittleFarm.getInstance().getMessageConfigs().get(playerResolver.language());
            ChatUtil.sendMessage(playerResolver.getHandle(), messages.messageByPathForMap("prefix", "mlf") + messages.messageByPathForMap("idle", "reject"));
            return;
        }
        this.jsonMenuModel.items().forEach(itemModel -> {
            if (itemModel.key().equals("claim_reward")) {
                this.gameContainer.putItem(new MenuItem(itemModel, null, (container, event) -> {
                    playerResolver.getPlayerOfflineData().claimAllRewards(this.gameContainer.items());
                    this.gameContainer.removeItems();
                    closeMenu(playerResolver.getHandle());
                }), Map.of());
            } else if (playerResolver.getCultureLevel(CultureType.ofItemKey(itemModel.key())) > 0 && playerResolver.hasReward(CultureType.ofItemKey(itemModel.key()))) {
                addItem(itemModel, playerResolver);
            }
        });
        this.gameContainer.openInventory(playerResolver.getHandle());
    }

}
