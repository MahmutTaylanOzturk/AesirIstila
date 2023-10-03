package me.taylan.extensions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.taylan.AesirIstila;
import me.taylan.utils.TimeUtils;
import net.william278.husktowns.api.HuskTownsAPI;
import net.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.units.qual.A;

public class TimeExtension extends PlaceholderExpansion {

    private AesirIstila plugin; // This instance is assigned in canRegister()
    public TimeExtension(AesirIstila plugin) {
        this.plugin = plugin;
    }
    @Override
    public String getAuthor() {
        return "Taylan";
    }

    @Override
    public String getIdentifier() {
        return "istila";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }


    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("kalansure")) {
            return TimeUtils.getRemainingTime(plugin.getIstilaMap().get("invasion"));
        }
        if (params.equalsIgnoreCase("savunmakalansure")) {
            Town town = HuskTownsAPI.getInstance().getUserTown(player.getPlayer()).get().town();
            return TimeUtils.getRemainingTime(plugin.getTownMap().get(town.getId()));
        }
        return null; // Placeholder is unknown by the expansion
    }
}