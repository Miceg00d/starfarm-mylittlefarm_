package ru.starfarm.mylittlefarm.objects.amulets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AmuletType {
    PROSPERITY("Амулет процветания", "§fАмулет процветания", 20, 0.20, 40.0),
    HARVEST("Амулет урожая", "§fАмулет урожая", 15, 0.15, 35.0),
    FARMER("Амулет фермера", "§fАмулет фермера", 25, 0.25, 45.0),

    LUCK("Амулет удачи", "§bАмулет удачи", 10, 0.10, 25.0),
    TIME_AMULET("Временной амулет", "§bВременной амулет", 30, 0.30, 55.0),
    WEALTH("Амулет богатства", "§bАмулет богатства", 35, 0.35, 60.0),

    MASTER("Амулет мастера", "§dАмулет мастера", 40, 0.40, 70.0),
    ELEMENTAL("Амулет стихий", "§dАмулет стихий", 45, 0.45, 75.0),

    LEGENDARY("Легендарный амулет", "§6Легендарный амулет", 50, 0.50, 85.0),
    DIVINE("Божественный амулет", "§6Божественный амулет", 60, 0.60, 95.0);
    
    private final String name;
    private final String displayName;
    private final int dropChance;
    private final double bonusMultiplier;
    private final double value;
    
    public static AmuletType getRandomByChance() {
        int random = (int) (Math.random() * 1000);
        
        if (random < 5) return DIVINE; // 0.5%
        if (random < 15) return LEGENDARY; // 1%
        if (random < 50) return ELEMENTAL; // 3.5%
        if (random < 100) return MASTER; // 5%
        if (random < 200) return WEALTH; // 10%
        if (random < 300) return TIME_AMULET; // 10%
        if (random < 450) return LUCK; // 15%
        if (random < 600) return FARMER; // 15%
        if (random < 750) return HARVEST; // 15%
        return PROSPERITY; // 25%
    }
    
    public String getRarityColor() {
        if (this == DIVINE || this == LEGENDARY) return "§6";
        if (this == MASTER || this == ELEMENTAL) return "§d";
        if (this == LUCK || this == TIME_AMULET || this == WEALTH) return "§b";
        return "§f";
    }
    
    public String getRarityName() {
        if (this == DIVINE || this == LEGENDARY) return "Легендарный";
        if (this == MASTER || this == ELEMENTAL) return "Эпический";
        if (this == LUCK || this == TIME_AMULET || this == WEALTH) return "Редкий";
        return "Обычный";
    }
}
