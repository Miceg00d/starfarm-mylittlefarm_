package ru.starfarm.mylittlefarm.i18n.menu;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.i18n.item.ItemModel;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

@Slf4j
public abstract class GameMenu implements PropertyChangeListener {

    private final String menuKey;
    protected final String language;
    protected final MenuConfig menuConfig;
    protected JsonMenuModel jsonMenuModel;
    protected GameContainer gameContainer;


    protected GameMenu(String language, String menuKey) {
        this.menuKey = menuKey;
        this.language = language;
        this.menuConfig = MyLittleFarm.getInstance().getMenuConfigs().get(this.language.toLowerCase());
        this.jsonMenuModel = this.menuConfig.menuByKey(menuKey);
        menuConfig.addPropertyChangeListener(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameMenu gameMenu = (GameMenu) o;
        return Objects.equals(menuKey, gameMenu.menuKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(menuKey);
    }

    protected abstract void addItem(ItemModel itemModel, PlayerResolver playerResolver);


    public void propertyChange(PropertyChangeEvent evt) {
        log.info("Property change for game menu with key: " + this.menuKey);
        if (evt.getPropertyName().equals(this.jsonMenuModel.title())) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getTopInventory().getName().equals(this.jsonMenuModel.title())) {
                    player.closeInventory();
                }
            }
            JsonMenuModel menuModel = (JsonMenuModel) evt.getNewValue();
            this.jsonMenuModel = menuModel;
        }
    }

    public void createGameContainer(PlayerResolver playerResolver) {
        this.gameContainer = new GameContainer(playerResolver.getHandle(), this.jsonMenuModel);
    }

    protected abstract void openMenu(PlayerResolver playerResolver);

    public void updateMenu(Player player) {
        this.gameContainer.updateInventory(player);
    }


    public void closeMenu(Player player) {
        this.gameContainer.closeInventory(player);
    }

}
