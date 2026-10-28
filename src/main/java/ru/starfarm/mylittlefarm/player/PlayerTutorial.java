package ru.starfarm.mylittlefarm.player;

import org.bukkit.entity.Player;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.enums.DataType;

public class PlayerTutorial {

    private final PlayerResolver playerResolver;

    public PlayerTutorial(PlayerResolver playerResolver) {
        this.playerResolver = playerResolver;
    }

    public void handleTutorial() {
        Player player = playerResolver.getHandle();
        int step = playerResolver.getData(DataType.TUTORIAL);
        if (step == 0) {
            if (playerResolver.getCultureLevel(CultureType.WHEAT) == 0) {
                ChatUtil.sendMessage(player, "&5Баба Маня&f: Привет, миленький! У меня для тебя есть задание, сходить купить 1 пшеничку! Она продается у Василича, он находиться позади!");
                return;
            }
            playerResolver.setDataLevel(DataType.TUTORIAL, 1);
        }
    }


}
