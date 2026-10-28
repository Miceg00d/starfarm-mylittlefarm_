package ru.starfarm.mylittlefarm.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.command.Command;
import ru.starfarm.core.command.context.CommandContext;
import ru.starfarm.core.command.type.TypeInteger;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

public class SpeedCommand extends Command<Player> {
    public SpeedCommand() {
        super("speed", "Изменить скорость ходьбы");

        addParameter("Speed", new TypeInteger());
    }

    @Override
    public void execute(@NotNull CommandContext<Player> ctx) {
        Player player = ctx.getSender();
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        if (playerResolver.getData(DataType.ABILITIES_SPEED) == 0) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &cУ вас не куплено изменение скорости. Это можно сделать в /donate.");
            return;
        }
        int number = ctx.getArg(0);

        if (number < 1 || number > 10) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &cВведите число в диапазоне 1-10.");
            return;
        }


        player.setWalkSpeed((float) number / 10);
        ChatUtil.sendMessage(player, "&8[&bКоманды&8] &fСкорость ходьбы изменена на &a%s&f.", number);

    }
}
