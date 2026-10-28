package ru.starfarm.mylittlefarm.objects;

import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.ApiManager;
import ru.starfarm.mylittlefarm.menus.BookMenu;
import ru.starfarm.mylittlefarm.menus.MainMenu;
import ru.starfarm.mylittlefarm.utils.ItemStackUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by Anton Znikin 28.08.2021.
 **/
public class GameItem {

    private @Getter Map<String, Function<Object[], ItemStack>> creators;
    private @Getter Map<String, BiConsumer<PlayerInteractEvent, ItemStack>> interacts;

    public GameItem() {
        init();
    }

    public ItemStack getItem(String name, Object... params) {
        Function<Object[], ItemStack> creator = creators.get(name.toLowerCase());
        if (creator == null)
            return null;
        return setTagName(creator.apply(params), name.toLowerCase());
    }

    public String getItemName(ItemStack item) {
        if (item != null) {
            NBTTagString nbt = ItemStackUtils.getTag(ApiManager.newItemBuilder(item), "mlf_item");
            String name;
            if (nbt != null && (name = nbt.c_()) != null) {
                if (creators.containsKey(name.toLowerCase()))
                    return name;
            }
        }
        return "";
    }

    // register items

    public void init() {
        creators = new HashMap<>();
        interacts = new HashMap<>();

        registerItem("telephone", objects ->
                        ApiManager.newItemBuilder(Material.NETHER_BRICK_ITEM)
                                .name("§5Телефон")
                                .build(),
                (event, itemStack) -> {
                    Player player = event.getPlayer();
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        new MainMenu().openInventory(player);
                    }
                });

        registerItem("book", objects ->
                        ApiManager.newItemBuilder(Material.BOOK)
                                .name("§aБухгалтерия")
                                .build(),
                (event, itemStack) -> {
                    Player player = event.getPlayer();

                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        new BookMenu().openInventory(player);
                    }
                });
        registerItem("diary", objects ->
                        ApiManager.newItemBuilder(Material.BOOK_AND_QUILL)
                                .name("§aДневник")
                                .build(),
                (event, itemStack) -> {
                    Player player = event.getPlayer();

                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        // SOON...
                    }
                });
        registerItem("fishing", objects ->
                        ApiManager.newItemBuilder(Material.FISHING_ROD)
                                .name("§aУдочка")
                                .build()
        );
        registerItem("bottle", objects ->
                        ApiManager.newItemBuilder(Material.GLASS_BOTTLE)
                                .name("§aПузырёк")
                                .build(),
                (event, itemStack) -> {
                    val player = event.getPlayer();
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (event.getItem() != null && event.getItem().getType() == Material.GLASS_BOTTLE) {
                            if (event.getClickedBlock().getRelative(event.getBlockFace()).getType() == Material.CAULDRON) {
                                event.setCancelled(true);
                                player.getInventory().setItemInMainHand(getItem("water_bottle"));
                                player.updateInventory();
                                return;
                            }
                        }

                        if (event.getItem() != null && event.getItem().getType().toString().toUpperCase().contains("BOTTLE")) {
                            event.setCancelled(true);
                        }
                    }
                });
        registerItem("water_bottle", objects ->
                        ApiManager.newItemBuilder(Material.POTION)
                                .name("§aПузырёк c водой")
                                .build(),
                (event, itemStack) -> {
                });
    }

    public void registerItem(String name, Function<Object[], ItemStack> creator, BiConsumer<PlayerInteractEvent, ItemStack> interact) {
        creators.put(name.toLowerCase(), creator);
        if (interact != null)
            interacts.put(name.toLowerCase(), interact);
    }

    public void registerItem(String name, Function<Object[], ItemStack> creator) {
        creators.put(name.toLowerCase(), creator);
    }

    public void registerItem(String name, ItemStack item, BiConsumer<PlayerInteractEvent, ItemStack> interact) {
        registerItem(name, (o) -> item.clone(), interact);
    }

    protected ItemStack setTagName(ItemStack item, String name) {
        return ItemStackUtils.setTag(ApiManager.newItemBuilder(item), "mlf_item", new NBTTagString(name.toLowerCase())).build();
    }

    public void interact(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
            String name;

            if ((name = getItemName(event.getItem())).length() > 1) {
                BiConsumer<PlayerInteractEvent, ItemStack> interact = interacts.get(name.toLowerCase());
                if (interact == null)
                    return;

                interact.accept(event, event.getItem());
            }
        }
    }
}
