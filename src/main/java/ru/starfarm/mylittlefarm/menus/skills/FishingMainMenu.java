package ru.starfarm.mylittlefarm.menus.skills;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.Skill;
import ru.starfarm.mylittlefarm.enums.UpgradeType;
import ru.starfarm.mylittlefarm.objects.skills.FishingRegistry;
import ru.starfarm.mylittlefarm.objects.skills.Require;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;

public class FishingMainMenu extends InventoryContainer {
    public FishingMainMenu() {
        super("Рыболовство", 5);
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        FishingRegistry registry = MyLittleFarm.getInstance().getFishingRegistry();

        Require rod = registry.getFishingRod(playerResolver.getUpgradeLevel(UpgradeType.FISHING_LEVEL) + 1);
        Require line = registry.getFishingLine(playerResolver.getUpgradeLevel(UpgradeType.FISHING_LEVEL) + 1);

        addItem(10, ApiManager.newItemBuilder(Material.FISHING_ROD)
                .name("&aУлучшить удочку")
                .lore(
                        playerResolver.getUpgradeLevel(UpgradeType.FISHING_LEVEL) >= 10 ?
                                Collections.singletonList(
                                        "&cУ вас максимальный уровень прокачки."
                                )
                                :
                                Arrays.asList(
                                        "",
                                        ChatUtil.format("&7Уровень удочки: &a%s &7-> &e%s", playerResolver.getUpgradeLevel(UpgradeType.FISHING_LEVEL), playerResolver.getUpgradeLevel(UpgradeType.FISHING_LEVEL) + 1),
                                        "",
                                        ChatUtil.format("&7Необходимый навык рыболовства: &a%s ур", rod.getNeedLevel()),
                                        ChatUtil.format("&7Стоимость прокачки: &a%s $", StringUtils.formatDouble(rod.getPrice())),
                                        "",
                                        "&7Чем выше уровень вашей удочки,",
                                        "&7тем более высокого уровня",
                                        "&7рыбу вы можете поймать.",
                                        "",
                                        playerResolver.getBalance() < rod.getPrice() && playerResolver.getSkillInfo(Skill.FISHING).getLevel() < rod.getNeedLevel() ? "&cДанное улучшение недоступно" : "&eНажмите, чтобы улучшить"
                                )
                )
                .build(), (__, event) -> {
            if (playerResolver.getUpgradeLevel(UpgradeType.FISHING_LEVEL) >= 10 || playerResolver.getBalance() < rod.getPrice() && playerResolver.getSkillInfo(Skill.FISHING).getLevel() < rod.getNeedLevel()) {
                return;
            }

            playerResolver.addUpgradeLevel(UpgradeType.FISHING_LEVEL, 1);
            playerResolver.removeBalance(rod.getPrice());
            ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &fВы успешно улучшили &eУдочку &fдо &a%s уровня&f.", playerResolver.getUpgradeLevel(UpgradeType.FISHING_LEVEL));

            new FishingMainMenu().openInventory(player);
        });

        addItem(12, ApiManager.newItemBuilder(Material.STRING)
                .name("&aУлучшить леску")
                .lore(
                        playerResolver.getUpgradeLevel(UpgradeType.FISHING_LEVEL) >= 10 ?
                                Collections.singletonList(
                                        "&cУ вас максимальный уровень прокачки."
                                )
                                :
                                Arrays.asList(
                                        "",
                                        ChatUtil.format("&7Уровень лески: &a%s &7-> &e%s", playerResolver.getUpgradeLevel(UpgradeType.FISHING_LINE), playerResolver.getUpgradeLevel(UpgradeType.FISHING_LINE) + 1),
                                        ChatUtil.format("&7Макс. масса: &a%s &7-> &e%s", registry.getFishingLine(playerResolver.getUpgradeLevel(UpgradeType.FISHING_LINE)).getWeight(), line.getWeight()),
                                        "",
                                        ChatUtil.format("&7Необходимый навык рыболовства: &a%s ур", line.getNeedLevel()),
                                        ChatUtil.format("&7Стоимость прокачки: &a%s $", StringUtils.formatDouble(line.getPrice())),
                                        "",
                                        "&7Чем выше уровень вашей лески,",
                                        "&7тем более большую массу",
                                        "&7рыбы вы можете поймать.",
                                        "",
                                        playerResolver.getBalance() < line.getPrice() && playerResolver.getSkillInfo(Skill.FISHING).getLevel() < line.getNeedLevel() ? "&cДанное улучшение недоступно" : "&eНажмите, чтобы улучшить"
                                )
                )
                .build(), (__, event) -> {
            if (playerResolver.getUpgradeLevel(UpgradeType.FISHING_LINE) >= 10 || playerResolver.getBalance() < line.getPrice() && playerResolver.getSkillInfo(Skill.FISHING).getLevel() < line.getNeedLevel()) {
                return;
            }

            playerResolver.addUpgradeLevel(UpgradeType.FISHING_LINE, 1);
            playerResolver.removeBalance(line.getPrice());
            ChatUtil.sendMessage(player, "&8[&2MyLittleFarm&8] &fВы успешно улучшили &eЛеску &fдо &a%s уровня&f.", playerResolver.getUpgradeLevel(UpgradeType.FISHING_LINE));

            new FishingMainMenu().openInventory(player);
        });

        addItem(14, ApiManager.newItemBuilder(Material.RAW_FISH)
                .name("&aОткрытые рыбы")
                .lore(
                        "&7Нажмите, чтобы посмотреть",
                        "&7открытых вами рыб."
                )
                .build(), (__, event) -> new FishingListMenu().openInventory(player)
        );
    }
}
