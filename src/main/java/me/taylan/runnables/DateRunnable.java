package me.taylan.runnables;

import me.taylan.AesirIstila;
import me.taylan.invasion.InvasionUtils;
import me.taylan.utils.FileManager;
import me.taylan.utils.TimeUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.william278.husktowns.api.HuskTownsAPI;
import net.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;

public class DateRunnable extends BukkitRunnable {

    private final AesirIstila aesirIstila;
    private final InvasionUtils invasionUtils;
    private final FileManager fileManager;

    public DateRunnable(AesirIstila aesirIstila) {
        this.aesirIstila = aesirIstila;
        this.fileManager = aesirIstila.getFileManager();
        this.invasionUtils = aesirIstila.getInvasionUtils();
    }

    @Override
    public void run() {
        aesirIstila.getTownMap().keySet().forEach(s -> {
            if (System.currentTimeMillis() / 1000 >= aesirIstila.getTownMap().get(s)) {
                YamlConfiguration configuration = fileManager.getConfig("towndata.yml").get();
                Town town = HuskTownsAPI.getInstance().getTown(s).get();
                if (configuration.contains("Towns." + town.getId())) {
                    configuration.set("Towns." + town.getId() + ".tomar", false);
                    fileManager.getConfig("towndata.yml").save();
                }
                town.getMembers().keySet().forEach(uuid -> {
                    Player member = Bukkit.getPlayer(uuid);
                    member.showTitle(Title.title(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-savunma-bitis-title")), MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-savunma-bitis-subtitle"))));
                });
                aesirIstila.getTownMap().remove(s);
            }
        });

        if (aesirIstila.getIstilaMap().isEmpty()) {
            LocalTime now = LocalTime.now();
            if (now.getHour() == aesirIstila.getConfig().getInt("istila-baslangic-saat") &&
                    now.getMinute() == aesirIstila.getConfig().getInt("istila-baslangic-dakika")) {
                invasionUtils.startInvasion();
            }
        } else {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> player.hideBossBar(aesirIstila.getBossBarMap().get("invasion")));
            if (System.currentTimeMillis() / 1000 >= aesirIstila.getIstilaMap().get("invasion")) {
                invasionUtils.endInvasion();
            }
            aesirIstila.getBossBarMap().put("invasion", BossBar.bossBar(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-bossbar-kalan-sure") +
                    TimeUtils.getRemainingTime(aesirIstila.getIstilaMap().get("invasion"))), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS));
            Bukkit.getServer().getOnlinePlayers().forEach(player -> player.showBossBar(aesirIstila.getBossBarMap().get("invasion")));
        }
    }

}


