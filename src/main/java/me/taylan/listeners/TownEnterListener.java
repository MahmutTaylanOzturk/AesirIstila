package me.taylan.listeners;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.taylan.AesirIstila;
import me.taylan.utils.FileManager;
import me.taylan.utils.TimeUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.william278.husktowns.api.HuskTownsAPI;
import net.william278.husktowns.events.PlayerEnterTownEvent;
import net.william278.husktowns.events.PlayerLeaveTownEvent;
import net.william278.husktowns.events.TownDisbandEvent;
import net.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TownEnterListener implements Listener {

    private AesirIstila plugin;
    private FileManager fileManager;

    public TownEnterListener(AesirIstila plugin) {
        this.plugin = plugin;
        this.fileManager = plugin.getFileManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void enterTown(TownDisbandEvent event) {
        YamlConfiguration configuration = fileManager.getConfig("towndata.yml").get();
        Town town = event.getTown();
        configuration.set("Towns." + town.getId(), null);
        fileManager.getConfig("towndata.yml").save();
    }

    @EventHandler
    public void enterTown(PlayerEnterTownEvent event) {
        Player player = event.getPlayer();
        Town town = event.getEnteredTownClaim().town();
        if (!plugin.getIstilaMap().isEmpty()) {
            YamlConfiguration configuration = fileManager.getConfig("towndata.yml").get();
            int requiredKillCount = configuration.getInt("Towns." + town.getId() + ".requiredKillCount");
            int killCount = configuration.getInt("Towns." + town.getId() + ".killCount");

            BossBar bossBar = BossBar.bossBar(MiniMessage.miniMessage().deserialize("<gold><i:false>Öldürmen Gereken İstilacı <white>" + killCount + "<gray>/<white>" + requiredKillCount), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
            player.showBossBar(bossBar);
        }
    }

    @EventHandler
    public void mobKill(MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        LivingEntity entity1 = event.getKiller();
        if (!(entity1 instanceof Player player)) return;
        YamlConfiguration configuration = fileManager.getConfig("towndata.yml").get();
        Town town = HuskTownsAPI.getInstance().getUserTown(player).get().town();
        if (configuration.contains("Towns." + town.getId())) {
            if (configuration.getBoolean("Towns." + town.getId() + ".savunuldu")) return;
            List<String> uuids = configuration.getStringList("Towns." + town.getId() + ".mobuuids");
            if (uuids.contains(entity.getUniqueId().toString())) {
                configuration.set("Towns." + town.getId() + ".killCount", configuration.getInt("Towns." + town.getId() + ".killCount") + 1);
                if (configuration.getInt("Towns." + town.getId() + ".killCount") >= configuration.getInt("Towns." + town.getId() + ".requiredKillCount")) {
                    configuration.set("Towns." + town.getId() + ".savunuldu", true);
                    configuration.set("Towns." + town.getId() + ".savunulanİstilaSayisi", configuration.getInt("Towns." + town.getId() + ".savunulanİstilaSayisi") + 1);
                    town.getMembers().keySet().forEach(uuid -> {
                        Player member = Bukkit.getPlayer(uuid);
                        member.showTitle(Title.title(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-savunma-basarili")), MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-bitti-subtitle"))));
                        member.playSound(member.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.4F, 1F);
                        member.playSound(member.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3F, 0.1F);
                    });

                }
                fileManager.getConfig("towndata.yml").save();
            }

        }
    }

    @EventHandler
    public void leaveTown(PlayerLeaveTownEvent event) {
        if (!plugin.getIstilaMap().isEmpty()) {
            Player player = event.getPlayer();
            Town town = event.getLeft();
        }
    }
}
