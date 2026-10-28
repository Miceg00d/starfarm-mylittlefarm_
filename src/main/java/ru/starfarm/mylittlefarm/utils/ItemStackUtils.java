package ru.starfarm.mylittlefarm.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.starfarm.core.util.item.ItemBuilder;

import java.util.Map;

@UtilityClass
public class ItemStackUtils {
    private final ReflectionUtil.FieldAccessor<Map<String, NBTBase>> acc = ReflectionUtil.fieldAccessor(ReflectionUtil.findField(NBTTagCompound.class, Map.class));

    public boolean hasName(ItemStack itemStack, String name) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equals(name);
    }

    public ItemBuilder setTag(ItemBuilder itemBuilder, String tag, NBTBase value) {
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemBuilder.build());
        NBTTagCompound nmsTag = nms.getTag();
        if (nmsTag == null)
            nmsTag = new NBTTagCompound();
        nmsTag.set(tag, value);
        nms.setTag(nmsTag);
        return new ItemBuilder(CraftItemStack.asBukkitCopy(nms));
    }

    public <T extends NBTBase> T getTag(ItemBuilder itemBuilder, String tag) {
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemBuilder.build());
        NBTTagCompound nmsTag = nms.getTag();
        if (nmsTag == null)
            nmsTag = new NBTTagCompound();
        return nmsTag.get(tag) == null ? null : (T) nmsTag.get(tag);
    }
}