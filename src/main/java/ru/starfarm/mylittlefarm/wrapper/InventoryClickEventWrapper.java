package ru.starfarm.mylittlefarm.wrapper;

import org.bukkit.event.inventory.InventoryClickEvent;
import ru.starfarm.core.util.item.ItemUtil;

public interface InventoryClickEventWrapper {

    String clickedInventory();

    String playerName();

    String itemTag();

    int slot();

    <T> T event();

    public class Base implements InventoryClickEventWrapper {

        private final InventoryClickEvent event;

        public Base(InventoryClickEvent inventoryClickEvent) {
            this.event = inventoryClickEvent;
        }

        @Override
        public String clickedInventory() {
            return this.event.getClickedInventory().getName();
        }

        @Override
        public String playerName() {
            return this.event.getWhoClicked().getName();
        }

        @Override
        public String itemTag() {
            return ItemUtil.getTag(this.event.getCurrentItem(), "mlf_item");
        }

        @Override
        public int slot() {
            return event.getSlot();
        }

        @Override
        public <T> T event() {
            return (T) this.event;
        }
    }

}
