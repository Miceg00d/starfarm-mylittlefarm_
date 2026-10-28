package ru.starfarm.mylittlefarm.i18n.item;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.inventory.container.Container;

import java.util.function.BiConsumer;

public class MenuItem {
    private final ItemModel itemModel;
    private final ItemStack itemStack;
    private final BiConsumer<Container, InventoryClickEvent> consumer;

    public MenuItem(ItemModel itemModel, ItemStack itemStack, BiConsumer<Container, InventoryClickEvent> consumer) {
        this.itemModel = itemModel;
        this.itemStack = itemStack;
        this.consumer = consumer;
    }

    public ItemModel getItemModel() {
        return itemModel;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public BiConsumer<Container, InventoryClickEvent> getConsumer() {
        return consumer;
    }
}
