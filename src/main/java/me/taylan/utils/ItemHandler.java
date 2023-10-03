package me.taylan.utils;


import me.taylan.AesirIstila;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemHandler {
    private AesirIstila plugin;

    public ItemHandler(AesirIstila plugin) {
        this.plugin = plugin;
    }

    public ItemStack createItem(Inventory inv, String materialString, int amount, int invSlot, String displayName,
                                String... loreString) {

        ItemStack item = new ItemStack(Material.matchMaterial(materialString), amount);
        List<String> lore = new ArrayList<>();
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName((Painter.paint(displayName)));
        for (String s : loreString) {
            lore.add(Painter.paint(s));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(invSlot - 1, item);
        return item;
    }

    public ItemStack createGuiItem(Material material, int amount, String name, List<Component> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }



}
