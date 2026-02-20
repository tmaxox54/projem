package dev.claimgui.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputListener implements Listener {

    private static ChatInputListener instance;
    private final JavaPlugin plugin;
    private final Map<UUID, Consumer<String>> waiting = new HashMap<>();

    public ChatInputListener(JavaPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static ChatInputListener get() { return instance; }

    public void await(Player player, String prompt, Consumer<String> callback) {
        waiting.put(player.getUniqueId(), callback);
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§l  ✏ §f" + prompt);
        player.sendMessage("§7  İptal etmek için §ciptal §7yaz.");
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    public boolean isWaiting(UUID uuid) {
        return waiting.containsKey(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!waiting.containsKey(uuid)) return;

        e.setCancelled(true);
        String input = e.getMessage().trim();
        Consumer<String> cb = waiting.remove(uuid);

        if (input.equalsIgnoreCase("iptal") || input.equalsIgnoreCase("cancel")) {
            e.getPlayer().sendMessage("§c✗ İşlem iptal edildi.");
            return;
        }

        String finalInput = input;
        Bukkit.getScheduler().runTask(plugin, () -> cb.accept(finalInput));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        waiting.remove(e.getPlayer().getUniqueId());
    }
}
