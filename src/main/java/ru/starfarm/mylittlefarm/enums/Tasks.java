package ru.starfarm.mylittlefarm.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Tasks {

    BREAK_1500_GRASS(Rarity.COMMON, TaskType.BREAK_GRASS, 1500, playerResolver -> playerResolver.addBalance(playerResolver.getBalance() * 0.1)),
    BREAK_50_HAY(Rarity.RARE, TaskType.BREAK_HAYS, 50, playerResolver -> playerResolver.addBalance(playerResolver.getBalance() * 0.1)),
    ;

    Rarity rarity;
    TaskType type;
    long value;
    Consumer<PlayerResolver> reward;

    @Nullable
    public static Tasks getByOrdinal(int ordinal) {
        for (Tasks task : Tasks.values()) {
            if (ordinal == task.ordinal())
                return task;
        }

        return null;
    }

    public static Tasks getRandom() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        return Arrays.stream(Tasks.values())
                .filter(tasks -> tasks.rarity.ordinal() >= Rarity.RARE.ordinal())
                .filter(tasks -> tasks.rarity.getChance() >= random.nextDouble())
                .findAny()
                .orElseGet(() -> Arrays.stream(Tasks.values())
                        .filter(tasks -> tasks.rarity == Rarity.COMMON)
                        .findAny()
                        .orElseThrow(() -> new RuntimeException("Expected common rarity but we haven't."))
                );
    }
}
