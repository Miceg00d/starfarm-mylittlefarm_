package ru.starfarm.mylittlefarm.i18n;

import ru.starfarm.mylittlefarm.wrapper.InventoryClickEventWrapper;

public class GameMenuTest {


}

interface FakeInventoryClickEvent extends InventoryClickEventWrapper {

    class Base implements FakeInventoryClickEvent {

        private final String clickedInventory;
        private final String playerName;
        private final String itemTag;
        private final int slot;

        public Base(String clickedInventory, String playerName, String itemTag, int slot) {
            this.clickedInventory = clickedInventory;
            this.playerName = playerName;
            this.itemTag = itemTag;
            this.slot = slot;
        }


        @Override
        public String clickedInventory() {
            return this.clickedInventory;
        }

        @Override
        public String playerName() {
            return this.playerName;
        }

        @Override
        public String itemTag() {
            return this.itemTag;
        }

        @Override
        public int slot() {
            return this.slot;
        }

        @Override
        public <T> T event() {
            return null;
        }
    }

}
