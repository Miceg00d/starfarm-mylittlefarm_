package ru.starfarm.mylittlefarm.objects.modifiers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import ru.starfarm.core.util.format.ChatUtil;

public class ModifierBar implements Listener {
    private final BossBar bar;
    public ModifierBar(final Plugin plugin) {
        String[] text = {
                "&aОбщий модификатор производства от групп игроков: &b%.2f%%",
                "&eОбщий модификатор производства от групп игроков: &b%.2f%%"
        };
        double modifier = Modifiers.getModifier();
        this.bar = Bukkit.createBossBar(ChatUtil.color(String.format(text[1], modifier)), BarColor.RED, BarStyle.SEGMENTED_6);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            int tick = 0;

            @Override
            public void run() {
                if (this.tick == text.length) {
                    this.tick = 0;
                }
                final double modifier = Modifiers.getModifier();
                final String title = ChatUtil.color(String.format(text[this.tick++], modifier));
                ModifierBar.this.bar.setTitle(title);
            }
        }, 20L, 60L);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.bar.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.bar.removePlayer(event.getPlayer());
    }

}