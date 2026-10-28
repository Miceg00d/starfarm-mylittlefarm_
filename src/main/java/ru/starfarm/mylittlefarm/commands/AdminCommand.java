package ru.starfarm.mylittlefarm.commands;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.command.Command;
import ru.starfarm.core.command.context.CommandContext;
import ru.starfarm.core.command.patameter.tab.EnumCompleter;
import ru.starfarm.core.command.patameter.tab.PlayersCompleter;
import ru.starfarm.core.command.require.Require;
import ru.starfarm.core.command.type.*;
import ru.starfarm.core.task.GlobalTaskContext;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.core.util.math.Cuboid;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.EventType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.LocationUtils;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j(topic = "MyLittleFarm")
public class AdminCommand extends Command<Player> {
    public AdminCommand() {
        super("mylittlefarm", "админские штучки", "mlf");

        addCommand(new CultureUpdateCommand());
        addCommand(new CultureGetCommand());
        addCommand(new BlockCommand());
        addCommand(new TestCommand());
        addCommand(new RemoveEventCommand());
        addCommand(new SetEventCommand());
        addCommand(new GetGameItemCommand());
        addCommand(new AddBalanceCommand());
        addRequire(Require.permission("*"));

        setPrefix("&8[&2MyLittleFarm&8] ");
        getCommands().forEach(command -> command.setPrefix(getPrefix()));
    }

    @Override
    public void execute(@NotNull CommandContext<Player> ctx) {

    }

    static class AddBalanceCommand extends Command<Player> {

        public AddBalanceCommand() {
            super("addbalance", "выдать баланс игроку");
            addParameter("player", new TypePlayer(), new PlayersCompleter());
            addParameter("count", new TypeDouble());

        }

        @Override
        public void execute(@NotNull CommandContext<Player> commandContext) {
            Player player = commandContext.getArg(0);
            if (player != null) {
                PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
                if (commandContext.getArg(1) != null) {
                    double count = commandContext.getArg(1);
                    playerResolver.addBalance(count);
                    log.info(String.format("Разработчик %s выдал игроку %s %s монет", commandContext.getSender(), player.getName(), count));
                }
            }
        }
    }

    static class GetGameItemCommand extends Command<Player> {

        public GetGameItemCommand() {
            super("getitem", "выдать кастомный предмет");
            addParameter("game-item", new TypeString(), (commandSender, list, list1) -> list1.addAll(List.of("book", "telephone", "bottle")));
            addParameter("player", new TypePlayer(), false, new PlayersCompleter());
        }

        @Override
        public void execute(@NotNull CommandContext<Player> commandContext) {
            String itemName = commandContext.getArg(0);
            if (itemName == null) {
                return;
            }
            if (commandContext.getArg(1) != null) {
                Player player = commandContext.getArg(1);
                if (player != null) {
                    player.getInventory().addItem(MyLittleFarm.getInstance().getGameItem().getItem(itemName));
                }
            } else {
                commandContext.getSender().getInventory().addItem(MyLittleFarm.getInstance().getGameItem().getItem(itemName));
            }
        }
    }

    static class SetEventCommand extends Command<Player> {
        public SetEventCommand() {
            super("setevent", "установить ивент для игрока");
            addParameter("player", new TypePlayer(), new PlayersCompleter());
            addParameter("event", new TypeEnum<>(EventType.class), new EnumCompleter(EventType.class));
        }

        @Override
        public void execute(@NotNull CommandContext<Player> commandContext) {
            Player player = commandContext.getArg(0);
            EventType eventType = commandContext.getArg(1);
            if (player != null && eventType != null) {
                PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
                playerResolver.activeEvent(eventType);
                Bukkit.getConsoleSender().sendMessage(String.format("Игроку %s был установлен ивент %s", player.getName(), eventType.getName()));
            }
        }
    }

    static class RemoveEventCommand extends Command<Player> {
        public RemoveEventCommand() {
            super("removeevent", "удалить ивент для игрока");
            addParameter("player", new TypePlayer(), new PlayersCompleter());
            addParameter("event", new TypeEnum<>(EventType.class), new EnumCompleter(EventType.class));
        }

        @Override
        public void execute(@NotNull CommandContext<Player> commandContext) {
            Player player = commandContext.getArg(0);
            EventType eventType = commandContext.getArg(1);
            if (player != null && eventType != null) {
                PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
                if (!playerResolver.hasActiveEvent(eventType)) {
                    Bukkit.getConsoleSender().sendMessage(String.format("У данного игрока не активен указанный ивент ивент %s", player.getName(), eventType.getName()));
                } else {
                    playerResolver.removeActiveEvent(eventType);
                    Bukkit.getConsoleSender().sendMessage(String.format("У игрока %s был удалён ивент %s", player.getName(), eventType.getName()));
                }
            }
        }
    }

    static class CultureGetCommand extends Command<Player> {

        public CultureGetCommand() {
            super("get");

            addParameter("culture", new TypeEnum<>(CultureType.class));
            addParameter("number", new TypeInteger(1, 100));
        }

        @Override
        public void execute(@NotNull CommandContext<Player> ctx) {
            CultureType culture = ctx.getArg(0);
            int number = ctx.getArg(1);

            ctx.sendMessage("&7Цена культуры %s:%s = &a%s", culture.name(), number, StringUtils.formatDouble(MyLittleFarm.getInstance().getCultureRegistry().getCulture(culture, number).price()));
        }
    }

    static class CultureUpdateCommand extends Command<Player> {

        public CultureUpdateCommand() {
            super("culture");

            addParameter("culture", new TypeEnum<>(CultureType.class));
            addParameter("price", new TypeDouble());
            addParameter("modifier", new TypeDouble());
            addParameter("restart", new TypeBoolean());
        }

        @Override
        public void execute(@NotNull CommandContext<Player> ctx) {
            CultureType culture = ctx.getArg(0);
            AtomicReference<Double> price = new AtomicReference<>(ctx.getArg(1));
            AtomicReference<Double> newPrice = new AtomicReference<>((double) 0);
            double modifier = ctx.getArg(2);
            boolean isRestart = ctx.getArg(3);

            ConfigurationSection section = MyLittleFarm.getInstance().getConfig().getConfigurationSection("cultures");
            ConfigurationSection keySection = section.getConfigurationSection(culture.name());

            keySection.getKeys(false).forEach((i) -> {
                if (!NumberUtils.isNumber(i)) {
                    return;
                }
                ConfigurationSection cultureSection = keySection.getConfigurationSection(i);
                try {
                    cultureSection.set("price", price.get());
                    MyLittleFarm.getInstance().getCultureRegistry().getCulture(culture, Integer.parseInt(i)).setPrice(price.get());
                    MyLittleFarm.getInstance().getConfig().save(new File(MyLittleFarm.getInstance().getDataFolder(), "config.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                newPrice.set((price.get() * modifier) + price.get());
                price.set(newPrice.get());
            });

            ctx.sendMessage("&aВы обновили культуру &3%s.", culture.name());
            if (isRestart) {
                ChatUtil.broadcast("&8[&bСистема&8] &aПерезагрузка сервера в связи с новым найдены обновлением.");
                GlobalTaskContext.getContext().after(60, task -> Bukkit.shutdown());
            }
        }
    }

    static class TestCommand extends Command<Player> {

        public TestCommand() {
            super("test");

            addParameter("type", new TypeEnum<>(CultureType.class));
        }

        @Override
        public void execute(@NotNull CommandContext<Player> commandContext) {
            MyLittleFarm.getInstance().getGamePlayer().getPlayer(commandContext.getSender()).setCultureLevel(commandContext.getArg(0), 1);
        }
    }

    static class BlockCommand extends Command<Player> {
        public BlockCommand() {
            super("block");

            addParameter("Player", new TypePlayer());
            addParameter("Material", new TypeEnum<>(Material.class));
        }

        @Override
        public void execute(@NotNull CommandContext<Player> ctx) {
            Player player = ctx.getArg(0);
            Material material = ctx.getArg(1);
            Cuboid cuboid = Cuboid.atLocations(
                    LocationUtils.fromString("world 168.4 0 85.7"),
                    LocationUtils.fromString("world -39.3 250 244")
            );

            cuboid.blockStream().filter(block -> block.getType() != Material.AIR)
                    .forEach(block -> player.sendBlockChange(block.getLocation(), material, (byte) 0));
        }
    }
}
