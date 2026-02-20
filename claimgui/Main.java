package dev.claimgui;

import dev.claimgui.gui.AraziMenu;
import dev.claimgui.gui.MainMenu;
import dev.claimgui.gui.YetkiliMenu;
import dev.claimgui.listener.ChatInputListener;
import dev.claimgui.manager.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private ClaimManager claimManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("PlaceholderAPI bulunamadı! Plugin devre dışı.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        claimManager = new ClaimManager(this);
        new ChatInputListener(this);

        AraziMenu araziMenu    = new AraziMenu(this, claimManager);
        YetkiliMenu yetkiliMenu = new YetkiliMenu(this, claimManager);
        new MainMenu(this, araziMenu, yetkiliMenu);

        // Async yükle
        Bukkit.getScheduler().runTaskAsynchronously(this, claimManager::loadAll);

        // Her 5 dakikada otomatik yenile
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                claimManager::loadAll, 20L * 60 * 5, 20L * 60 * 5);

        getLogger().info("GPClaimGUI v1.0.0 aktif!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GPClaimGUI kapandı.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSadece oyuncular kullanabilir.");
            return true;
        }

        switch (cmd.getName().toLowerCase()) {
            case "claim-gui" -> MainMenu.open(player, this);
            case "gpclaim-reload" -> {
                if (!player.hasPermission("claim.admin")) {
                    player.sendMessage("§cYetersiz izin.");
                    return true;
                }
                player.sendMessage("§7Yükleniyor...");
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    claimManager.loadAll();
                    Bukkit.getScheduler().runTask(this, () ->
                            player.sendMessage("§a✔ Claim verileri yenilendi!"));
                });
            }
        }
        return true;
    }

    public static Main getInstance() { return instance; }
    public ClaimManager getClaimManager() { return claimManager; }
}
