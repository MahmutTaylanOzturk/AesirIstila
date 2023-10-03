package me.taylan.invasion;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.taylan.AesirIstila;
import me.taylan.utils.FileManager;
import me.taylan.utils.TimeUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.william278.husktowns.api.HuskTownsAPI;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class InvasionUtils {

    private final AesirIstila aesirIstila;
    private final FileManager fileManager;

    public InvasionUtils(AesirIstila aesirIstila) {
        this.aesirIstila = aesirIstila;
        this.fileManager = aesirIstila.getFileManager();
    }

    public void startInvasion() {
        if (aesirIstila.getIstilaMap().isEmpty()) {
            aesirIstila.getIstilaMap().put("invasion", System.currentTimeMillis() / 1000 + aesirIstila.getConfig().getInt("default-istila-süresi") * 60);
            aesirIstila.getBossBarMap().put("invasion", BossBar.bossBar(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-bossbar-kalan-sure") +
                    TimeUtils.getRemainingTime(aesirIstila.getIstilaMap().get("invasion"))), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS, Set.of(BossBar.Flag.DARKEN_SCREEN, BossBar.Flag.CREATE_WORLD_FOG)));
            aesirIstila.getServer().getOnlinePlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2F, 0.5F);
                player.showBossBar(aesirIstila.getBossBarMap().get("invasion"));
                spawnMobs();
            });

            aesirIstila.getConfig().set("istila", true);
            aesirIstila.saveConfig();
        }
    }

    public void endInvasion() {
        if (!aesirIstila.getIstilaMap().isEmpty()) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> player.hideBossBar(aesirIstila.getBossBarMap().get("invasion")));
            aesirIstila.getIstilaMap().clear();
            aesirIstila.getBossBarMap().clear();
            aesirIstila.getConfig().set("istila", false);
            aesirIstila.getConfig().set("kalan-istila-süresi", 0);
            aesirIstila.saveConfig();
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2F, 0.5F);
                player.showTitle(Title.title(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-bitti-title")), MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-bitti-subtitle"))));
            });
        }
    }

    /*public void openPvpForTown(Player player) {
        HuskTownsAPI.getInstance().editTown(player, "townName", town -> {
            final Map<Claim.Type, Rules> rules = town.getRules();
            for (Claim.Type type : rules.keySet()) {
                rules.setFlag(Flag.Defaults.PVP.getFlag(), true);
            }
            return town;
        });
    }*/

    public void spawnMobs() {
        World world = Bukkit.getWorld("world");
        YamlConfiguration configuration = fileManager.getConfig("towndata.yml").get();
        HuskTownsAPI.getInstance().getTowns().forEach(town -> {
            List<String> uuids = new ArrayList<>();
            AtomicInteger claimAmount = new AtomicInteger();
            AtomicInteger mobAmount = new AtomicInteger();
            if (configuration.getBoolean("Towns." + town.getId() + ".tomar")) {
                return;
            }
            HuskTownsAPI.getInstance().getClaims(world).forEach(townClaim -> {
                if (townClaim.town().equals(town)) {
                    if (claimAmount.get() > town.getClaimCount() / 2) return;
                    claimAmount.getAndIncrement();
                    town.getMembers().keySet().forEach(uuid -> {
                        if (Bukkit.getPlayer(uuid).isOnline()) {
                            HuskTownsAPI.getInstance().getPlugin().highlightClaim(HuskTownsAPI.getInstance().getOnlineUser(Bukkit.getPlayer(uuid)), townClaim);
                        }
                    });
                    int x = ThreadLocalRandom.current().nextInt(25);
                    int z = ThreadLocalRandom.current().nextInt(25);
                    int xborder = townClaim.claim().getChunk().getX() * 16 + x;
                    int zborder = townClaim.claim().getChunk().getZ() * 16 + z;
                    int y = world.getHighestBlockAt(xborder, zborder).getY() + 1;
                    Location loc = new Location(world, xborder, y, zborder);
                    world.spawnParticle(Particle.CLOUD, loc, 10, 0.1, 0.1, 0.1, 0.1);
                    FileConfiguration fileConfiguration = fileManager.getConfig("mobs.yml").get();
                    for (String key : fileConfiguration.getConfigurationSection("Mobs").getKeys(false)) {
                        MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob(key).orElse(null);
                        if (mob != null) {
                            for (int i = 0; i <= fileConfiguration.getInt("Mobs." + key + ".amount"); i++) {
                                ActiveMob mob2 = mob.spawn(BukkitAdapter.adapt(loc), fileConfiguration.getInt("Mobs." + key + ".level"));
                                uuids.add(mob2.getUniqueId().toString());
                                mobAmount.getAndIncrement();
                                //Bukkit.getServer().getConsoleSender().sendMessage("mob doğdu: " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
                            }
                        }
                    }
                }
            });
            if (!configuration.contains("Towns." + town.getId())) {
                configuration.set("Towns." + town.getId() + ".savunuldu", false);
                configuration.set("Towns." + town.getId() + ".tomar", false);
                configuration.set("Towns." + town.getId() + ".savunulanİstilaSayisi", 0);
                configuration.set("Towns." + town.getId() + ".basilanTomarSayisi", 0);
                configuration.set("Towns." + town.getId() + ".requiredKillCount", mobAmount.get());
                configuration.set("Towns." + town.getId() + ".killCount", 0);
                configuration.set("Towns." + town.getId() + ".mobuuids", uuids);

            }
        });
        fileManager.getConfig("towndata.yml").save();
    }
}
