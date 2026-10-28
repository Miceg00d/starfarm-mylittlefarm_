package ru.starfarm.mylittlefarm.commands;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.command.Command;
import ru.starfarm.core.command.context.CommandContext;
import ru.starfarm.core.command.type.TypeEnum;
import ru.starfarm.core.command.type.TypeInteger;
import ru.starfarm.core.profile.IProfileService;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.player.enums.Top;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

public class TopCommand extends Command<Player> {

    public TopCommand() {
        super("top", "топ");

        addParameter("Тип", new TypeEnum<>(Top.class));
        addParameter("Место", new TypeInteger(1, 2000));
        setPrefix("&8[&2MyLittleFarm&8] &7");
    }

    @Override
    public void execute(@NotNull CommandContext<Player> ctx) {
        val gamePlayer = MyLittleFarm.getInstance().getGamePlayer();
        val top = (Top) ctx.getArg(0);
        val map = gamePlayer.top.get(top).entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        val place = (Integer) ctx.getArg(1);

        if (!map.containsKey(place)) {
            ctx.sendMessage("&cДанное место в топе еще свободно :)");
            return;
        }
        String player = map.get(place);
        if (Bukkit.getPlayerExact(player) == null)
            ctx.sendMessage("%s &aлидирует в топе по &2%s &aна &2%s &aместе.",
                    IProfileService.getService().getProfile(player).getColoredName(),
                    top.name, place
            );
        else {
            PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(Bukkit.getPlayerExact(player));

            String value;
            if (top == Top.REBIRTH) {
                value = String.valueOf(playerResolver.getRebirths());
            } else if (top == Top.INCOME) {
                value = StringUtils.formatDouble(playerResolver.getIncome());
            } else {
                value = StringUtils.formatDouble(playerResolver.getBalance());
            }

            ctx.sendMessage("%s &aлидирует в топе по &2%s &aна &2%s &aместе. &7(&2%s&7)",
                    IProfileService.getService().getProfile(player).getColoredName(),
                    top.name, place, value
            );
        }
    }
}
