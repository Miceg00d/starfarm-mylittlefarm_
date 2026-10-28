package ru.starfarm.mylittlefarm.commands;

import lombok.val;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.command.Command;
import ru.starfarm.core.command.context.CommandContext;
import ru.starfarm.core.command.patameter.tab.PlayersCompleter;
import ru.starfarm.core.command.require.Require;
import ru.starfarm.core.command.type.TypePlayer;
import ru.starfarm.core.profile.group.StaffGroup;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;

public class AvistCommand extends Command<Player> {
    public AvistCommand() {
        super("avist", "Показать игрока для вас.");

        addParameter("Player", new TypePlayer(), new PlayersCompleter());
        addRequire(Require.groups(StaffGroup.HELPER));
    }

    @Override
    public void execute(@NotNull CommandContext<Player> ctx) {
        val target = (Player) ctx.getArg(0);

        ctx.getSender().showPlayer(MyLittleFarm.getInstance(), target);
        ChatUtil.sendMessage(ctx.getSender(), "&8[&bКоманды&8] &fВы начали слежку за игроком %s, он вас не видит.", target.getName());
    }
}