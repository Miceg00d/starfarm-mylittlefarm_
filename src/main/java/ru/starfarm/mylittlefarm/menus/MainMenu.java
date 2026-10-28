package ru.starfarm.mylittlefarm.menus;

import lombok.NonNull;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.menus.bonus.BonusMenu;
import ru.starfarm.mylittlefarm.player.PlayerResolver;
import ru.starfarm.mylittlefarm.rebirth.RebirthMainMenu;

public class MainMenu extends InventoryContainer {

    public MainMenu() {
        super("Баба Маня", 5);
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);
        addItem(4,
                ApiManager.newItemBuilder(Material.FENCE)
                        .name("§aПостройка зданий")
                        .addLore(
                                "§7В этой менюшке ты можешь купить",
                                "§7на свою ферму здания, предоставляющие",
                                "§7в хозяйстве различные приятные бонусы."
                        )
                        .build(), (baseInventory, event) -> new ConstructionBuildingsMenu(true).openInventory((Player) event.getWhoClicked())
        );
        addItem(24,
                ApiManager.newItemBuilder(Material.EYE_OF_ENDER)
                        .name("§aЦели")
                        .addLore(
                                "§bДостигнуто: §3" + playerResolver.getData(DataType.ACHIEVEMENTS) + "/77",
                                "",
                                "§7Цели - негласные задания, за",
                                "§7выполнения которых на своей ферме",
                                "§7вы будете получать приятное",
                                "§7денежное вознаграждение."
                        )
                        .build(), (inventory, event) -> new AchievementsMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(20,
                ApiManager.newItemBuilder(Material.BEACON)
                        .name("§aУлучшения")
                        .addLore(
                                "§bПриобретено: §3" + playerResolver.getData(DataType.UPGRADES) + "/94",
                                "",
                                "§7Улучшения значительно помогают",
                                "§7в развитии вашей фермы, ведь",
                                "§7они сильно увеличивают",
                                "§7получаемый с производства доход."
                        )
                        .build(), (inventory, event) -> new UpgradesMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(12,
                ApiManager.newItemBuilder(Material.SPRUCE_DOOR_ITEM)
                        .name("§aПоход в гости")
                        .addLore(
                                "§7Нажми, чтобы отправиться",
                                "§7на ферму другого игрока."
                        )
                        .build(), (baseInventory, event) -> {
                    val clickedPlayer = (Player) event.getWhoClicked();
                    ChatUtil.sendMessage(clickedPlayer, "&5Баба Маня: &fТак, а теперь скажи мне, к кому на ферму ты хочешь отправиться?");
                    MyLittleFarm.getInstance().getGamePlayer().getSearch().add(clickedPlayer);
                    clickedPlayer.closeInventory();
                }
        );

        addItem(14,
                ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK)
                        .name("§aНастройки")
                        .addLore(
                                "§7Нажми для открытия меню",
                                "§7настроек своей фермы."
                        ).build(), (inventory, event) -> new SettingsMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(22,
                ApiManager.newItemBuilder(Material.NETHER_STAR)
                        .name("§b§lМеню Перерождений")
                        .addLore("§7Нажми, чтобы открыть меню")
                        .addLore("§7сброса прогресса и прокачки")
                        .addLore("§7вечных бонусов за токены.")
                        .build(), (baseInventory, event) -> new RebirthMainMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(30,
                ApiManager.newItemBuilder(Material.QUARTZ)
                        .name("§aЗадания")
                        .build(), (baseInventory, event) -> new BonusMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(32,
                ApiManager.newItemBuilder(Material.QUARTZ)
                        .name("§aДополнительные возможности")
                        .addLore(
                                "§7Нажми, чтобы узнать о дополнительных",
                                "§7возможностях, которые можно получить",
                                "§7на этом режиме с покупкой серверных групп."
                        )
                        .build(), (baseInventory, event) -> new DonateMenu().openInventory((Player) event.getWhoClicked())
        );
    }
}
