package ru.starfarm.mylittlefarm.objects.modifiers;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import ru.starfarm.core.profile.IProfileService;
import ru.starfarm.core.profile.group.DonateGroup;
import ru.starfarm.core.profile.group.Group;
import ru.starfarm.core.profile.group.StaffGroup;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Modifiers {

    private static float MODIFIER = 0F;
    private static final List<Group> GROUPS = Lists.newArrayList(
            StaffGroup.MEDIA,
            StaffGroup.DEVELOPER,
            DonateGroup.UNIQUE,
            DonateGroup.SPONSOR_PLUS,
            DonateGroup.SPONSOR,
            DonateGroup.ELITE_PLUS,
            DonateGroup.ELITE,
            DonateGroup.PREMIUM_PLUS,
            DonateGroup.PREMIUM,
            DonateGroup.VIP,
            DonateGroup.VIP_PLUS
    );

    public static void initialize(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new ModifierBar(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.MONITOR)
            public void onPlayerJoin(PlayerJoinEvent event) {
                addGroup(getPrimaryGroup(event.getPlayer()));
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onPlayerQuit(PlayerQuitEvent event) {
                removeGroup(getPrimaryGroup(event.getPlayer()));
            }

        }, plugin);
    }

    public static void addGroup(Group group) {
        if (group == null) {
            return;
        }
        if (!GROUPS.contains(group)) {
            return;
        }
        float modifier = getGroupModifier(group);
        if (modifier != 0F) {
            MODIFIER += modifier;
        } else {
            MODIFIER = 0F;
        }
        MODIFIER = Math.min(MODIFIER, 100F);
        MyLittleFarm.getInstance().getServer().getOnlinePlayers().forEach(player -> {
            PlayerResolver resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
            if (resolver != null) {
                resolver.calculateIncome();
            }
        });
    }

    public static void removeGroup(Group group) {
        if (group == null) {
            return;
        }
        if (!GROUPS.contains(group)) {
            return;
        }
        List<Group> all = Bukkit.getOnlinePlayers().stream()
                .map(Modifiers::getPrimaryGroup)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        all.remove(group);
        all.removeIf(perms -> !GROUPS.contains(group));
        MODIFIER = 0F;
        for (Group perm : all) {
            MODIFIER += getGroupModifier(perm);
        }
        MODIFIER = Math.min(MODIFIER, 100F);
        MyLittleFarm.getInstance().getServer().getOnlinePlayers().forEach(player -> {
            if (player.isOnline()) {
                PlayerResolver resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
                if (resolver != null) {
                    resolver.calculateIncome();
                }
            }
        });
    }

    private static float getGroupModifier(Group group) {
        if (group == null || !GROUPS.contains(group)) {
            return 0F;
        }
        if (group == StaffGroup.MEDIA) {
            return 3.0F;
        } else if (group == DonateGroup.UNIQUE || group == StaffGroup.DEVELOPER) {
            return 5.0F;
        } else if (group == DonateGroup.SPONSOR_PLUS) {
            return 3.5F;
        } else if (group == DonateGroup.SPONSOR) {
            return 2.75F;
        } else if (group == DonateGroup.ELITE_PLUS) {
            return 2.5F;
        } else if (group == DonateGroup.ELITE) {
            return 2.25F;
        } else if (group == DonateGroup.PREMIUM_PLUS) {
            return 2.0F;
        } else if (group == DonateGroup.PREMIUM) {
            return 1.75F;
        } else if (group == DonateGroup.VIP_PLUS) {
            return 1.0F;
        } else if (group == DonateGroup.VIP) {
            return 0.5F;
        } else return 0f;
    }

    public static float getModifier() {
        return MODIFIER;
    }

    public static UnaryOperator<Long> getLongModifier() {
        return value -> (long) (value - (value / 100 * MODIFIER));
    }

    public static UnaryOperator<Integer> getIntModifier() {
        return value -> (int) (value - (value / 100 * MODIFIER));
    }

    private static Group getPrimaryGroup(Player player) {
        if (isIgnored(player)) {
            return null;
        }
        for (Group group : GROUPS) {
            if (IProfileService.getService().getProfile(player).hasGroup(group)) {
                return group;
            }
        }
        return null;
    }

    public static boolean isIgnored(Player player) {
        return IProfileService.getService().getProfile(player).hasGroup(StaffGroup.ADMINISTRATOR);
    }
}
