package ru.starfarm.mylittlefarm.menus.amulets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.inventory.container.InventoryContainer;
import ru.starfarm.core.util.item.ItemBuilder;
import ru.starfarm.core.util.item.ItemUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.DataType;
import ru.starfarm.mylittlefarm.enums.Product;
import ru.starfarm.mylittlefarm.objects.amulets.Amulet;
import ru.starfarm.mylittlefarm.objects.amulets.AmuletType;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class AmuletCaseMenu extends InventoryContainer {
    
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final String[] COLORS = {"§f", "§b", "§d", "§6", "§e", "§a", "§c"};
    
    private boolean isRolling = false;
    private AmuletType result = null;
    
    public AmuletCaseMenu() {
        super("§6§lОткрытие кейса", 3);
    }
    
    @Override
    public void drawInventory(@NotNull Player player) {
        PlayerResolver resolver = MyLittleFarm.getInstance().getGamePlayer().getPlayer(player);

        if (resolver.getProductCount(Product.AMULET_KEY) <= 0) {
            fillBorders(Material.STAINED_GLASS_PANE, 7, "§cУ вас нет ключей!");
            addItem(13, ItemUtil.build(Material.BARRIER, b -> {
                b.setName("§c§lНет ключей!");
                b.setLore(Arrays.asList(
                    "§7Для открытия кейса нужен ключ",
                    "§7Ключи выдаются за перерождения",
                    "§7или можно получить другими способами",
                    "",
                    "§e§lНажмите для закрытия"
                ));
            }), (c, e) -> player.closeInventory());
            return;
        }

        fillBorders(Material.STAINED_GLASS_PANE, 4, "§f");

        addItem(13, ItemUtil.build(Material.ENDER_CHEST, b -> {
            b.setName("§6§lКейс с амулетом");
            b.setLore(Arrays.asList(
                "§7Нажмите чтобы открыть кейс",
                "§7У вас есть §e" + resolver.getProductCount(Product.AMULET_KEY) + " §7ключей",
                "",
                "§e§l[ЛКМ] Открыть кейс"
            ));
        }), (c, e) -> {
            if (!isRolling) {
                openCase(player, resolver);
            }
        });

        addItem(22, ItemUtil.build(Material.BOOK, b -> {
            b.setName("§e§lИнформация о кейсах");
            b.setLore(Arrays.asList(
                "§7В кейсах можно найти амулеты:",
                "§f• Обычные амулеты §7- 25% шанс",
                "§b• Редкие амулеты §7- 45% шанс", 
                "§d• Эпические амулеты §7- 20% шанс",
                "§6• Легендарные амулеты §7- 10% шанс",
                "",
                "§7Амулеты дают бонусы к ферме"
            ));
        }), null);
    }
    
    private void openCase(Player player, PlayerResolver resolver) {
        if (resolver.getProductCount(Product.AMULET_KEY) <= 0) {
            player.closeInventory();
            return;
        }
        
        isRolling = true;

        resolver.removeProduct(Product.AMULET_KEY, 1);
        animateRoll(player, resolver);
    }
    
    private void animateRoll(Player player, PlayerResolver resolver) {
        CompletableFuture.runAsync(() -> {
            try {
                result = AmuletType.getRandomByChance();

                for (int i = 0; i < 30; i++) {
                    final int iteration = i;

                    Bukkit.getScheduler().runTask(MyLittleFarm.getInstance(), () -> {
                        updateRollingDisplayLeftToRight(player, iteration);
                    });
                    
                    Thread.sleep(80);
                }

                Bukkit.getScheduler().runTask(MyLittleFarm.getInstance(), () -> {
                    showResult(player, resolver);
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                isRolling = false;
            }
        });
    }
    
    private void updateRollingDisplayLeftToRight(Player player, int iteration) {
        // Очищаем все слоты
        for (int slot : SLOTS) {
            addItem(slot, null);
        }

        int slotsToShow = Math.min(iteration % 8 + 1, SLOTS.length);
        
        for (int i = 0; i < slotsToShow; i++) {
            int slot = SLOTS[i];

            AmuletType randomType = AmuletType.values()[ThreadLocalRandom.current().nextInt(AmuletType.values().length)];
            String color = COLORS[ThreadLocalRandom.current().nextInt(COLORS.length)];
            
            ItemStack item = ItemUtil.build(Material.DIAMOND, b -> {
                b.setName(color + "???");
                b.setLore(Arrays.asList(
                    randomType.getRarityColor() + randomType.getRarityName() + " амулет",
                    "§7Определяется..."
                ));
            });
            
            addItem(slot, item, null);
        }
    }
    
    private void showResult(Player player, PlayerResolver resolver) {
        for (int slot : SLOTS) {
            addItem(slot, null);
        }

        ItemStack resultItem = ItemUtil.build(Material.DIAMOND, b -> {
            b.setName(result.getDisplayName());
            b.setLore(Arrays.asList(
                result.getRarityColor() + result.getRarityName() + " амулет",
                "",
                "§eБонус: §a+" + (int)(result.getBonusMultiplier() * 100) + "%",
                "§7Нажмите чтобы забрать"
            ));
        });
        
        addItem(13, resultItem, (c, e) -> {
            addAmuletToPlayer(resolver, result);
            player.closeInventory();

            player.sendMessage("§e§lКейс открыт! §7Вы получили: " + result.getRarityColor() + result.getName() + " §7[" + result.getRarityName() + "]");
        });

        fillBorders(Material.STAINED_GLASS_PANE, 5, "§a");

        addItem(22, ItemUtil.build(Material.ARROW, b -> {
            b.setName("§a§lЗабрать амулет");
            b.setLore(Arrays.asList("§7Нажмите чтобы получить амулет"));
        }), (c, e) -> {
            addAmuletToPlayer(resolver, result);
            player.closeInventory();
        });
    }
    
    private void addAmuletToPlayer(PlayerResolver resolver, AmuletType type) {
        MyLittleFarm.getInstance().getLogger().info("Player " + resolver.getHandle().getName() + " received amulet: " + type.getName());

        if (resolver.getData(DataType.AMULETS_STORAGE) == 0) {
            resolver.setDataLevel(DataType.AMULETS_STORAGE, 1);
        } else {
            resolver.setDataLevel(DataType.AMULETS_STORAGE, resolver.getData(DataType.AMULETS_STORAGE) + 1);
        }
    }
    
    private void fillBorders(Material material, int data, String name) {
        ItemStack item = ItemUtil.build(material, (short)data, b -> {
            b.setName(name);
        });

        for (int i = 0; i < 9; i++) {
            addItem(i, item, null);
            addItem(i + 18, item, null);
        }

        for (int i = 0; i < 3; i++) {
            addItem(i * 9, item, null);
            addItem(i * 9 + 8, item, null);
        }
    }
    
    @AllArgsConstructor
    @Getter
    private enum AnimationFrame {
        SPIN(Material.DIAMOND, "§e???"),
        GLOW(Material.GOLD_INGOT, "§6???"),
        SPARKLE(Material.EMERALD, "§a???");
        
        private final Material material;
        private final String name;
    }
}
