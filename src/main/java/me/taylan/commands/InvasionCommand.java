package me.taylan.commands;


import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.taylan.AesirIstila;
import me.taylan.invasion.InvasionUtils;
import me.taylan.utils.FileManager;
import me.taylan.utils.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.william278.husktowns.api.HuskTownsAPI;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvasionCommand implements CommandExecutor {

    private final AesirIstila aesirIstila;
    private final InvasionUtils invasionUtils;
    private final FileManager fileManager;

    public InvasionCommand(AesirIstila aesirIstila) {
        this.aesirIstila = aesirIstila;
        this.fileManager = aesirIstila.getFileManager();
        this.invasionUtils = aesirIstila.getInvasionUtils();
        aesirIstila.getCommand("istila").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.isOp()) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("başlat")) {
                        invasionUtils.startInvasion();
                    } else if (args[0].equalsIgnoreCase("bitir")) {
                        invasionUtils.endInvasion();
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        fileManager.getConfig("mobs.yml").reload();
                        fileManager.getConfig("messages.yml").reload();
                        aesirIstila.reloadConfig();
                        player.sendMessage(MiniMessage.miniMessage().deserialize(" "));
                        player.sendMessage(MiniMessage.miniMessage().deserialize("             <red><bold>☠  AESİR İSTİLA 1.0.0  ☠"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green><i:false>✔ <gray>Mobs.yml dosyası tekrardan yüklendi. (<green>"
                                + fileManager.getConfig("mobs.yml").get().getConfigurationSection("Mobs").getKeys(false).size() + "<gray>) adet yaratık bulundu."));
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green><i:false>✔ <gray>config.yml dosyası tekrardan yüklendi. "));
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green><i:false>✔ <gray>messages.yml dosyası tekrardan yüklendi. "));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(" "));
                    }
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("tomar")) {
                        Player player1 = Bukkit.getPlayer(args[1]);
                        YamlConfiguration configuration = fileManager.getConfig("towndata.yml").get();
                        Town town = HuskTownsAPI.getInstance().getUserTown(player1).get().town();

                        if (configuration.contains("Towns." + town.getId())) {
                            if (!configuration.getBoolean("Towns." + town.getId() + ".tomar")) {
                                configuration.set("Towns." + town.getId() + ".basilanTomarSayisi", configuration.getInt("Towns." + town.getId() + ".basilanTomarSayisi") + 1);
                                configuration.set("Towns." + town.getId() + ".tomar", true);
                                aesirIstila.getTownMap().put(town.getId(), System.currentTimeMillis() / 1000 + aesirIstila.getConfig().getInt("default-tomar-süresi"));
                                town.getMembers().keySet().forEach(uuid -> {
                                    Player member = Bukkit.getPlayer(uuid);
                                    member.showTitle(Title.title(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-tomar-title")), MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-tomar-subtitle").replace("%player%", player1.getName()))));
                                });
                                fileManager.getConfig("towndata.yml").save();
                            } else {
                                player1.sendMessage(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-tomar-mevcut")));
                                player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.4F, 1F);
                            }
                        } else {
                            player1.sendMessage(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-data-null")));
                        }
                    }
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray><i:false>[<red>Aesirİstila<gray>] <red>Yanlış kullanım."));
                }
            }
            if (args[0].equalsIgnoreCase("bilgi")) {
                Optional<Member> member = HuskTownsAPI.getInstance().getUserTown(player);
                YamlConfiguration configuration = fileManager.getConfig("towndata.yml").get();
                if (member.isPresent()) {
                    Town town = member.get().town();
                    if (configuration.contains("Towns." + town.getId())) {

                        String tomarSure = "yok";
                        boolean tomar = configuration.getBoolean("Towns." + town.getId() + ".tomar");
                        int savunulanİstilaSayisi = configuration.getInt("Towns." + town.getId() + ".savunulanİstilaSayisi");
                        int basilanTomarSayisi = configuration.getInt("Towns." + town.getId() + ".basilanTomarSayisi");
                        int requiredKillCount = configuration.getInt("Towns." + town.getId() + ".requiredKillCount");
                        int killCount = configuration.getInt("Towns." + town.getId() + ".killCount");
                        if (tomar) {
                            tomarSure = TimeUtils.getRemainingTime(aesirIstila.getTownMap().get(town.getId()));
                        }


                        ChestGui gui = new ChestGui(3, "Kasabanın İstila Bilgileri");
                        StaticPane pane = new StaticPane(0, 0, 9, 3);
                        gui.setOnGlobalClick(event -> event.setCancelled(true));
                        List<Component> loreList = new ArrayList<>();

                        loreList.add(MiniMessage.miniMessage().deserialize("<gray><i:false>Kasaba İsmi: <yellow>" + town.getName()));
                        loreList.add(MiniMessage.miniMessage().deserialize("<gray><i:false>Kasaba İsmi: <yellow>" + town.getClaimCount()));
                        loreList.add(MiniMessage.miniMessage().deserialize("<gray><i:false>Kasabanın Savunduğu İstila Sayısı: <yellow>" + savunulanİstilaSayisi));
                        loreList.add(MiniMessage.miniMessage().deserialize("<gray><i:false>Kasabanın Kullandığı Savunma Tomarı Sayısı: yellow" + basilanTomarSayisi));
                        loreList.add(MiniMessage.miniMessage().deserialize(" "));

                        ItemStack item = aesirIstila.getItemHandler().createGuiItem(Material.PLAYER_HEAD, 1, "<gold><i:false>Kasaba Bilgileri", loreList);
                        GuiItem head = new GuiItem(item);
                        loreList.clear();
                    }
                }
            }
        } else if (sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("başlat")) {
                    invasionUtils.startInvasion();
                } else if (args[0].equalsIgnoreCase("bitir")) {
                    invasionUtils.endInvasion();
                } else if (args[0].equalsIgnoreCase("reload")) {
                    fileManager.getConfig("mobs.yml").reload();
                    fileManager.getConfig("messages.yml").reload();
                    aesirIstila.reloadConfig();
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(" "));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("             <red><bold>☠  AESİR İSTİLA 1.0.0  ☠"));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green><i:false>✔ <gray>Mobs.yml dosyası tekrardan yüklendi. (<green>"
                            + fileManager.getConfig("mobs.yml").get().getConfigurationSection("Mobs").getKeys(false).size() + "<gray>) adet yaratık bulundu."));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green><i:false>✔ <gray>config.yml dosyası tekrardan yüklendi. "));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green><i:false>✔ <gray>messages.yml dosyası tekrardan yüklendi. "));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(" "));
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("tomar")) {
                    Player player1 = Bukkit.getPlayer(args[1]);
                    YamlConfiguration configuration = fileManager.getConfig("towndata.yml").get();
                    Town town = HuskTownsAPI.getInstance().getUserTown(player1).get().town();

                    if (configuration.contains("Towns." + town.getId())) {
                        if (!configuration.getBoolean("Towns." + town.getId() + ".tomar")) {
                            configuration.set("Towns." + town.getId() + ".tomar", true);
                            aesirIstila.getTownMap().put(town.getId(), System.currentTimeMillis() / 1000 + 86400);
                            town.getMembers().keySet().forEach(uuid -> {
                                Player member = Bukkit.getPlayer(uuid);
                                member.showTitle(Title.title(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-tomar-title")), MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-tomar-subtitle"))));
                            });
                            fileManager.getConfig("towndata.yml").save();
                        } else {
                            player1.sendMessage(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-tomar-mevcut")));
                            player1.playSound(player1.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.4F, 1F);
                        }
                    } else {
                        player1.sendMessage(MiniMessage.miniMessage().deserialize(fileManager.getConfig("messages.yml").get().getString("istila-data-null")));
                    }
                }
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray><i:false>[<red>Aesirİstila<gray>] <red>Yanlış kullanım."));
            }
        }
        return false;
    }
}
