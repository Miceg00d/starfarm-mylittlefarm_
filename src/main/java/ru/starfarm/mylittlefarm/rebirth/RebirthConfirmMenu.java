package ru.starfarm.mylittlefarm.rebirth;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.item.ItemBuilder;
import ru.starfarm.core.util.item.ItemUtil;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.utils.StringUtils;
import java.util.Arrays;
import java.util.function.Consumer;

public class RebirthConfirmMenu extends InventoryContainer {
    private final RebirthManager manager;

    public RebirthConfirmMenu(RebirthManager manager) {
        super("Подтверждение", 3);
        this.manager = manager;
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver res = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        long cost = manager.getCost(res);

        addItem(13, ItemUtil.build(Material.NETHER_STAR, (Consumer<ItemBuilder>) b -> {
            b.setName("§c§lПодтвердить Перерождение");
            b.setLore(Arrays.asList(
                    "§7Стоимость: §a$" + StringUtils.formatDouble(cost),
                    "§7Вы получите: §d" + ((res.getData(ru.starfarm.mylittlefarm.enums.DataType.RB_UPGRADE_TOKEN_BONUS) + 1) * 5) + " Токен.",
                    "",
                    "§4ВНИМАНИЕ: §cВЕСЬ ПРОГРЕСС ВАШЕЙ ФЕРМЫ ИСЧЕЗНЕТ!",
                    "",
                    "§e[Нажмите для подтверждения]"
            ));
        }), (c, e) -> manager.process(player));

        addItem(18, ItemUtil.build(Material.ARROW, (Consumer<ItemBuilder>) b -> b.setName("§7Назад")), (c, e) -> new RebirthMainMenu().openInventory(player));
    }
}
