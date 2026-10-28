package ru.starfarm.mylittlefarm.listeners;

import lombok.val;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.mylittlefarm.MyLittleFarm;

import java.util.Arrays;

import static org.bukkit.Material.GLASS_BOTTLE;

public class GlobalCanceller implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!MyLittleFarm.BUILD_MODE.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!MyLittleFarm.BUILD_MODE.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.setCancelled(true);
        Player p = event.getPlayer();
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        event.getItem().remove();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() != null) {
            val material = event.getClickedBlock().getType();

            val blockedMaterials = Arrays.asList(Material.CHEST, Material.WORKBENCH, Material.ENCHANTMENT_TABLE,
                    Material.ANVIL, Material.FURNACE, Material.DISPENSER,
                    Material.DROPPER, Material.HOPPER, Material.BREWING_STAND, Material.TRAP_DOOR, Material.IRON_TRAPDOOR
            );

            if (blockedMaterials.contains(material)) {
                event.setCancelled(true);
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockRightClick(event);
        } else if (event.getAction() == Action.PHYSICAL) {
            handlePhysicalInteract(event);
        }
    }

    @EventHandler
    public void onBottle(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            if (mainHandItem != null && mainHandItem.getType() == GLASS_BOTTLE) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null && clickedBlock.getType() == Material.CAULDRON && clickedBlock.getData() == 3) {
                    event.setCancelled(true);
                    player.getInventory().setItemInMainHand(MyLittleFarm.getInstance().getGameItem().getItem("water_bottle"));
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event){
        if (event.getInventory().getType() != InventoryType.CRAFTING || event.getSlot() != 40) {
            return;
        }

        event.setResult(Event.Result.DENY);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING
                || !event.getInventorySlots().contains(40)) return;

        event.setResult(Event.Result.DENY);
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getNewState().getBlock().getType() == Material.SOIL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
        WorldServer world = ((CraftWorld) event.getWorld()).getHandle();
        world.getWorldData().setThundering(false);
        world.getWorldData().setThunderDuration(0);
    }
    
    @EventHandler
    public void onWaterFlow(BlockFromToEvent event) {
        if(event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.STATIONARY_WATER) {
            event.setCancelled(true);
        }
    }

    private void handleBlockRightClick(PlayerInteractEvent event) {
        event.useItemInHand();
    }

    private void handlePhysicalInteract(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY) return;

        Block block = event.getClickedBlock();
        Material type = block.getType();

        if (type == Material.SOIL) {
            event.setCancelled(true);
        }
    }
}
