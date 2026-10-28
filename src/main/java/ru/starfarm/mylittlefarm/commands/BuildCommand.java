package ru.starfarm.mylittlefarm.commands;

import lombok.val;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.command.Command;
import ru.starfarm.core.command.context.CommandContext;
import ru.starfarm.core.command.require.Require;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;

public class BuildCommand extends Command<Player> {

    public BuildCommand() {
        super("build", "Вкл/выкл редактирование карты");

        addRequire(Require.permission("*"));
    }

    @Override
    public void execute(@NotNull CommandContext<Player> ctx) {
        val player = ctx.getSender();

        boolean contains = MyLittleFarm.BUILD_MODE.contains(player);
        if (contains) {
            MyLittleFarm.BUILD_MODE.remove(player);
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] §cБольше вы не можете вносить изменения на карте!");
        } else {
            MyLittleFarm.BUILD_MODE.add(player);
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] §aТеперь вы можете вносить изменения на карте!");
        }
    }
}
