package ru.starfarm.mylittlefarm.i18n.menu;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.ApiManager;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.mylittlefarm.i18n.item.ItemModel;
import ru.starfarm.mylittlefarm.i18n.item.MenuItem;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderType;
import ru.starfarm.mylittlefarm.i18n.json.PlaceholderValue;
import ru.starfarm.mylittlefarm.menus.MainMenu;

import java.util.*;

@Slf4j
@Getter
public class GameContainer extends InventoryContainer {

    private final Player player;
    private int rows;
    private Optional<Integer> pages;

    private final List<MenuItem> menuItems = new ArrayList<>();
    private int currentPage = 1;


    public GameContainer(Player player, JsonMenuModel menuModel) {
        super(menuModel.title(), menuModel.rows());
        this.player = player;
        this.rows = menuModel.rows();
        this.pages = menuModel.pages();
    }

    @Override
    public void drawInventory(@NotNull Player player) {
        if (pages.isPresent()) {
            if (currentPage == 1) {
                addItem(rows * 9 - 9,
                        ApiManager.newItemBuilder(Material.PAPER)
                                .name("§cВ главное меню")
                                .build(), (inventory, event) -> new MainMenu().openInventory((Player) event.getWhoClicked())
                );
            } else if (currentPage == pages.get()) {
                addItem(rows * 9 - 1,
                        ApiManager.newItemBuilder(Material.PAPER)
                                .name("§cСледующая страница")
                                .build(), (inventory, event) -> {
                            Player clickedPlayer = (Player) event.getWhoClicked();

                            currentPage++;
                            updateInventory(clickedPlayer);
                        }
                );
            } else {
                addItem(rows * 9 - 9,
                        ApiManager.newItemBuilder(Material.PAPER)
                                .name("§cПредыдущая страница")
                                .build(), (inventory, event) -> {
                            Player clickedPlayer = (Player) event.getWhoClicked();
                            currentPage--;
                            updateInventory(clickedPlayer);
                        }
                );
            }
            addItems();
        } else {
            addItem(rows * 9 - 9,
                    ApiManager.newItemBuilder(Material.PAPER)
                            .name("§cВ главное меню")
                            .build(), (inventory, event) -> new MainMenu().openInventory((Player) event.getWhoClicked())
            );
            addItems();
        }

    }

    public List<ItemModel> items() {
        return this.menuItems.stream()
                .map(MenuItem::getItemModel)
                .toList();
    }

    private void addItems() {
        if (pages.isPresent()) {
            List<MenuItem> pageItems = menuItems.stream()
                    .filter(menuItem -> menuItem.getItemModel().jsonModel().page().isPresent() && menuItem.getItemModel().jsonModel().page().get() == this.currentPage)
                    .toList();

            pageItems.forEach(menuItem -> addItem(
                    menuItem.getItemModel().jsonModel().menuSlot(null),
                    menuItem.getItemStack(),
                    menuItem.getConsumer()
            ));
        } else {
            menuItems.forEach(menuItem -> addItem(
                    menuItem.getItemModel().jsonModel().menuSlot(null),
                    menuItem.getItemStack(),
                    menuItem.getConsumer()
            ));
        }
    }

    public void putItem(MenuItem menuItem, Map<PlaceholderType, String> values) {
        ItemModel itemModel = menuItem.getItemModel();
        Map<PlaceholderType, String> handleValues = handlePlaceholderValues(this.player, itemModel, values);
        ItemStack itemStack = itemModel.itemStack(handleValues);
        menuItems.add(new MenuItem(itemModel, itemStack, menuItem.getConsumer()));
    }

    public void removeItem(ItemModel itemModel) {
        MenuItem menuItem = this.menuItems.stream()
                .filter(menuItem1 -> menuItem1.getItemModel().equals(itemModel))
                .findFirst().orElse(null);
        if (menuItem != null) this.menuItems.remove(menuItem);
        else {
            throw new IllegalArgumentException("Failed to delete the item from game container with argument: " + itemModel.toString());
        }
    }

    public void removeItems() {
        menuItems.clear();
    }

    private Map<PlaceholderType, String> handlePlaceholderValues(Player player, ItemModel itemModel, Map<PlaceholderType, String> values) {
        Map<PlaceholderType, String> placeholderValues = new HashMap<>();
        if (player != null && !values.isEmpty()) {
            values.forEach((placeholderType, value) -> {
                if (value.equalsIgnoreCase("later")) {
                    placeholderValues.put(placeholderType, new PlaceholderValue(player, placeholderType, itemModel.key()).handleValue());
                } else placeholderValues.put(placeholderType, value);
            });
        }
        return placeholderValues;
    }

}
