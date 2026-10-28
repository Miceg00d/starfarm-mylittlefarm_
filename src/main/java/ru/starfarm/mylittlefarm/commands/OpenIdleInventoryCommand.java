package ru.starfarm.mylittlefarm.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.command.Command;
import ru.starfarm.core.command.context.CommandContext;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.i18n.messages.L10nMessages;
import ru.starfarm.mylittlefarm.menus.OfflineEarningsMenu;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

public class OpenIdleInventoryCommand extends Command<Player> {

    public OpenIdleInventoryCommand() {
        super("idlereward", "Открыть меню с оффлайн заработком");
    }

    @Override
    public void execute(@NotNull CommandContext<Player> commandContext) {
        Player player = commandContext.getSender();
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        if (playerResolver.getPlayerOfflineData().hasOfflineReward() && playerResolver.getPlayerOfflineData().hasRewards()) {
            new OfflineEarningsMenu(playerResolver.language()).openMenu(playerResolver);
        } else {
            L10nMessages l10nMessages = MyLittleFarm.getInstance().getMessageConfigs().get(playerResolver.language());
            String prefix = l10nMessages.messageByPathForMap("prefix", "mlf");
            String message = l10nMessages.messageByPathForMap("idle", "reject");
            ChatUtil.sendMessage(player, prefix + message);
        }
    }
}
