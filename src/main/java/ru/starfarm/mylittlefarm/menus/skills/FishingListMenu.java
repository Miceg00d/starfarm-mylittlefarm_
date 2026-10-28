package ru.starfarm.mylittlefarm.menus.skills;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.Skill;
import ru.starfarm.mylittlefarm.objects.skills.Fish;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.player.info.SkillInfo;

import java.util.Arrays;
import java.util.Collections;

public class FishingListMenu extends InventoryContainer {
    public FishingListMenu() {
        super("Открытые рыбы", 5);
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        SkillInfo skillInfo = playerResolver.getSkillInfo(Skill.FISHING);
        Arrays.asList(Fish.values()).forEach(fish -> {
            boolean has = skillInfo.other.containsKey(fish.name());

            addItem(fish.slot, ApiManager.newItemBuilder(has ? fish.material : Material.BARRIER)
                    .name(has ? "&a".concat(fish.name) : "&cЗаблокировано")
                    .lore(
                            has ?
                            Arrays.asList(
                                    "",
                                    "&7Поймано рыб: &a" + skillInfo.other.getOrDefault(fish.name(), 0)
                            )
                            :
                            Collections.singletonList(
                                    "&7Данный вид рыб вам недоступен."
                            )
                    )
                    .build()
            );
        });

        addItem(36, ApiManager.newItemBuilder(Material.PAPER)
                .name("&aНазад")
                .build(), (__, event) -> new FishingMainMenu().openInventory(player)
        );
    }

}
