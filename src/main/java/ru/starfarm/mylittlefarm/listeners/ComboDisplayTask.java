package ru.starfarm.mylittlefarm.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

public class ComboDisplayTask extends BukkitRunnable {

    private final MyLittleFarm plugin;

    public ComboDisplayTask(MyLittleFarm plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerResolver resolver = plugin.getGamePlayer().getPlayer(player);
            if (resolver == null) continue;

            double multiplier = resolver.getComboMultiplier();

            if (multiplier >= 1.1) {
                String message = ChatUtil.format("&6&lКОМБО &e&lx" + String.format("%.1f", multiplier));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
        }
    }

    public void start() {
        this.runTaskTimerAsynchronously(plugin, 0L, 5L);
    }
}
