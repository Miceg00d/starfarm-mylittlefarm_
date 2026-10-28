package ru.starfarm.mylittlefarm.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.command.Command;
import ru.starfarm.core.command.context.CommandContext;
import ru.starfarm.mylittlefarm.menus.StatisticMenu;

public class StatisticCommand extends Command<Player> {
    public StatisticCommand() {
        super("stats", "Показать вашу статистику");
    }

    @Override
    public void execute(@NotNull CommandContext<Player> ctx) {
        new StatisticMenu().openInventory(ctx.getSender());
    }
}
