package ru.starfarm.mylittlefarm.objects.amulets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amulet {
    private UUID id;
    private AmuletType type;
    private long createdAt;
    private boolean isActive;
    
    public Amulet(AmuletType type) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.isActive = false;
    }
    
    public String getDisplayName() {
        return type.getDisplayName();
    }
    
    public String getFormattedName() {
        return type.getRarityColor() + type.getName() + " §7[" + type.getRarityName() + "]";
    }
    
    public double getBonusMultiplier() {
        return type.getBonusMultiplier();
    }
}
