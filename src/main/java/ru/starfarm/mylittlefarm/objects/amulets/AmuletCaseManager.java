package ru.starfarm.mylittlefarm.objects.amulets;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.starfarm.core.util.format.ChatUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.enums.Product;
import ru.starfarm.mylittlefarm.menus.amulets.AmuletChestMenu;
import ru.starfarm.mylittlefarm.menus.amulets.AmuletCaseMenu;
import ru.starfarm.mylittlefarm.player.PlayerResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
public class AmuletCaseManager implements Listener {
    
    private final MyLittleFarm plugin;
    private final Map<Location, Boolean> caseLocations;
    
    public AmuletCaseManager(MyLittleFarm plugin) {
        this.plugin = plugin;
        this.caseLocations = new ConcurrentHashMap<>();

        loadCaseLocations();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        log.info("Loaded {} amulet case locations!", caseLocations.size());
    }
    
    private void loadCaseLocations() {

        plugin.getConfig().set("amulet_case_locations", null);
        plugin.saveConfig();

        Location defaultLoc = new Location(Bukkit.getWorlds().get(0), 13.0, 87.0, 94.0);
        addCaseLocation(defaultLoc);
        log.info("Created default amulet case location at: {}", defaultLoc);
    }
    
    public void addCaseLocation(Location location) {
        String locStr = location.getWorld().getName() + "," + 
                        location.getX() + "," + 
                        location.getY() + "," + 
                        location.getZ();
        
        java.util.List<String> locations = plugin.getConfig().getStringList("amulet_case_locations");
        if (!locations.contains(locStr)) {
            locations.add(locStr);
            plugin.getConfig().set("amulet_case_locations", locations);
            plugin.saveConfig();
        }

        caseLocations.put(location, true);

        createCaseHologram(location);
    }
    
    private void createCaseHologram(Location location) {
        log.info("Creating case at: {}", location);

        location.getBlock().setType(Material.CHEST);

        Location signLoc = location.clone().add(0, 1, 0);
        signLoc.getBlock().setType(Material.SIGN_POST); // Стоячий знак

        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signLoc.getBlock().getState();
        sign.setLine(0, "§6§lКейс с амулетами");
        sign.setLine(1, "§7Нажмите чтобы");
        sign.setLine(2, "§7открыть!");
        sign.update();
        
        log.info("Placed chest at: {}", location);
        log.info("Placed sign at: {}", signLoc);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        log.info("Interact event: action={}, block={}", event.getAction(), 
            event.getClickedBlock() != null ? event.getClickedBlock().getType() : "null");
            
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.CHEST) return;
        
        Location blockLocation = clickedBlock.getLocation();
        log.info("Clicked chest at: {}", blockLocation);

        for (Location caseLoc : caseLocations.keySet()) {
            log.info("Checking case location: {}", caseLoc);
            
            if (Math.abs(caseLoc.getX() - blockLocation.getX()) < 1 &&
                Math.abs(caseLoc.getY() - blockLocation.getY()) < 1 &&
                Math.abs(caseLoc.getZ() - blockLocation.getZ()) < 1) {
                
                log.info("Found matching case! Opening menu...");
                event.setCancelled(true);
                
                Player player = event.getPlayer();
                PlayerResolver resolver = plugin.getGamePlayer().getPlayer(player);
                
                if (resolver == null) return;
                
                int keys = resolver.getProductCount(Product.AMULET_KEY);
                
                if (keys > 0) {
                    new AmuletChestMenu(player).openInventory(player);
                } else {
                    player.sendMessage(ChatUtil.format("&c&lОшибка! &7У вас нет ключей для открытия кейса."));
                    player.sendMessage(ChatUtil.format("&7Получайте ключи за перерождения или другими способами."));
                }
                break;
            }
        }
    }
    
    public void removeCaseLocation(Location location) {
        caseLocations.remove(location);

        String locStr = location.getWorld().getName() + "," + 
                        location.getX() + "," + 
                        location.getY() + "," + 
                        location.getZ();
        
        java.util.List<String> locations = plugin.getConfig().getStringList("amulet_case_locations");
        locations.remove(locStr);
        plugin.getConfig().set("amulet_case_locations", locations);
        plugin.saveConfig();
    }
}
