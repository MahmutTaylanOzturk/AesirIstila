package me.taylan;

import me.taylan.commands.InvasionCommand;
import me.taylan.extensions.TimeExtension;
import me.taylan.invasion.InvasionUtils;
import me.taylan.listeners.TownEnterListener;
import me.taylan.runnables.DateRunnable;
import me.taylan.utils.FileManager;
import me.taylan.utils.ItemHandler;
import me.taylan.utils.TimeUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public final class AesirIstila extends JavaPlugin {
    private static AesirIstila instance;

    private ItemHandler itemHandler;
    private FileManager fileManager;


    public AesirIstila() {
        instance = this;
    }

    public static AesirIstila getInstance() {
        return instance;
    }

    private final HashMap<String, Long> istilaMap = new HashMap<>();
    private final HashMap<Integer, Long> townMap = new HashMap<>();
    private final HashMap<String, BossBar> bossBarMap = new HashMap<>();

    public ItemHandler getItemHandler() {
        return itemHandler;
    }

    public HashMap<String, BossBar> getBossBarMap() {
        return bossBarMap;
    }

    private InvasionUtils invasionUtils;

    public HashMap<String, Long> getIstilaMap() {
        return istilaMap;
    }

    public InvasionUtils getInvasionUtils() {
        return invasionUtils;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public HashMap<Integer, Long> getTownMap() {
        return townMap;
    }

    @Override
    public void onEnable() {
        itemHandler = new ItemHandler(this);
        fileManager = new FileManager(this);
        File config = new File("plugins/AesirIstila", "config.yml");
        if (!config.exists()) {
            saveDefaultConfig();
        }
        fileManager.getConfig("mobs.yml").copyDefaults(true).save();
        fileManager.getConfig("towndata.yml").copyDefaults(true).save();
        fileManager.getConfig("messages.yml").copyDefaults(true).save();
        if (getConfig().getBoolean("istila")) {
            istilaMap.put("invasion", getConfig().getLong("kalan-istila-süresi"));
            bossBarMap.put("invasion", BossBar.bossBar(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-bossbar-kalan-sure") +
                    TimeUtils.getRemainingTime(istilaMap.get("invasion"))), 1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS, Set.of(BossBar.Flag.DARKEN_SCREEN, BossBar.Flag.CREATE_WORLD_FOG)));
        }
        invasionUtils = new InvasionUtils(this);
        new InvasionCommand(this);
        new TownEnterListener(this);
        new DateRunnable(this).runTaskTimer(this, 0, 20);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TimeExtension(this).register();
        }
        send("-------------------------------");
        send("<gray> Aesirİstila <green>Aktif!");
        send("<gray> Versiyon: <yellow>1.0.0");
        send("<gray> Yapımcı: <green>Taylan");
        send("-------------------------------");
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("istila")) {
            getConfig().set("kalan-istila-süresi", istilaMap.get("invasion"));
            saveConfig();
        }
        fileManager.getConfig("mobs.yml").save();
        fileManager.getConfig("towndata.yml").save();
        fileManager.getConfig("messages.yml").save();
        send("-------------------------------");
        send("<gray> Aesirİstila <red>Deaktif!");
        send("<gray> Versiyon: <yellow>1.0.0");
        send("<gray> Yapımcı: <green>Taylan");
        send("-------------------------------");
    }

    public void send(String string) {
        getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(string));
    }
}
