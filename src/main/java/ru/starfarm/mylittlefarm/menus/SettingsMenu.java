package ru.starfarm.mylittlefarm.menus;

import lombok.NonNull;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

public class SettingsMenu extends InventoryContainer {

    public SettingsMenu() {
        super("Настройки фермы", 3);
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        PlayerResolver playerResolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        addItem(22,
                ApiManager.newItemBuilder(Material.PAPER)
                        .name("§cВ главное меню")
                        .build(), (inventory, event) -> new MainMenu().openInventory((Player) event.getWhoClicked())
        );

        addItem(10,
                ApiManager.newItemBuilder(Material.MONSTER_EGG)
                        .name(ChatUtil.format("§eРежим посещений: %s", playerResolver.getData(DataType.VISIT) == 2 ? "§cникто" : playerResolver.getData(DataType.VISIT) == 1 ? "§dдрузья" : "§aвсе"))
                        .lore(playerResolver.getData(DataType.VISIT) == 2 ?
                                new String[] {
                                        "§7Сейчас к вам на ферму в",
                                        "§7гости не может зайти ни один игрок,",
                                        "§7кроме администрации проекта.",
                                        "§7Нажми, если хочешь изменить это."
                                }
                                :
                                playerResolver.getData(DataType.VISIT) == 1 ?
                                        new String[] {
                                                "§7Сейчас к вам на ферму могут",
                                                "§7зайти лишь друзья.",
                                                "§7Нажми, если хочешь изменить это."
                                        }
                                        :
                                        new String[] {
                                                "§7Сейчас к вам на ферму может",
                                                "§7зайти абсолютно любой игрок.",
                                                "§7Нажми, если хочешь изменить это."
                        })
                        .durability(playerResolver.getData(DataType.VISIT) == 2 ? EntityType.SPIDER.getTypeId() : playerResolver.getData(DataType.VISIT) == 1 ? EntityType.OCELOT.getTypeId() : EntityType.CREEPER.getTypeId())
                        .build(), (inventory, event) -> {
                    val clickedPlayer = (Player) event.getWhoClicked();
                    val itemStack = event.getCurrentItem();
                    val gamePlayer = MyLittleFarm.getInstance().getGamePlayer().getPlayer(clickedPlayer);

                    if (gamePlayer.getData(DataType.VISIT) == 0) {
                        gamePlayer.setDataLevel(DataType.VISIT, 1);
                    } else if (gamePlayer.getData(DataType.VISIT) == 1) {
                        gamePlayer.setDataLevel(DataType.VISIT, 2);
                    } else {
                        gamePlayer.setDataLevel(DataType.VISIT, 0);
                    }
                    updateInventory(clickedPlayer);
                });

        addItem(13,
                ApiManager.newItemBuilder(Material.MONSTER_EGG)
                        .name(ChatUtil.format("§eОтображение голограмм: %s", playerResolver.getData(DataType.HOLOGRAM) == 1 ? "§cотключено" : "§aвключено"))
                        .addLore(playerResolver.getData(DataType.HOLOGRAM) == 1 ?
                                "§7Сейчас у вас отключено отображение" : "§7Сейчас у вас включено отображение"
                        )
                        .addLore("§7голограмм при копании травы.")
                        .addLore("§7Нажми, если хочешь изменить это.")
                        .durability(playerResolver.getData(DataType.HOLOGRAM) == 1 ? EntityType.SPIDER.getTypeId() : EntityType.CREEPER.getTypeId())
                        .build(), (inventory, event) -> {
                    val clickedPlayer = (Player) event.getWhoClicked();
                    val itemStack = event.getCurrentItem();
                    val gamePlayer = MyLittleFarm.getInstance().getGamePlayer().getPlayer(clickedPlayer);

                    if (gamePlayer.getData(DataType.HOLOGRAM) == 0) {
                        gamePlayer.setDataLevel(DataType.HOLOGRAM, 1);
                    } else {
                        gamePlayer.setDataLevel(DataType.HOLOGRAM, 0);
                    }
                    updateInventory(clickedPlayer);
                });

        addItem(16,
                ApiManager.newItemBuilder(Material.MONSTER_EGG)
                        .name(ChatUtil.format("§eЧат: %s", playerResolver.getData(DataType.CHAT) == 1 ? "§cотключен" : "§aвключен"))
                        .addLore(playerResolver.getData(DataType.CHAT) == 1 ?
                                "§7Нажми для включения" : "§7Нажми для отключения"
                        )
                        .durability(playerResolver.getData(DataType.CHAT) == 1 ? EntityType.SPIDER.getTypeId() : EntityType.CREEPER.getTypeId())
                        .build(), (inventory, event) -> {
                    val clickedPlayer = (Player) event.getWhoClicked();
                    val itemStack = event.getCurrentItem();
                    val gamePlayer = MyLittleFarm.getInstance().getGamePlayer().getPlayer(clickedPlayer);

                    if (gamePlayer.getData(DataType.CHAT) == 0) {
                        gamePlayer.setDataLevel(DataType.CHAT, 1);
                    } else {
                        gamePlayer.setDataLevel(DataType.CHAT, 0);
                    }
                    updateInventory(clickedPlayer);
                });
    }

}
