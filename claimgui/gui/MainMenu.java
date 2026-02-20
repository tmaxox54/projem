package dev.claimgui.gui;

import dev.claimgui.listener.ChatInputListener;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class MainMenu implements Listener {

    private static final String TITLE = "§0       ᴀʀᴀᴢɪ ᴍᴇɴüsü";

    private final JavaPlugin plugin;
    private final AraziMenu araziMenu;
    private final YetkiliMenu yetkiliMenu;

    public MainMenu(JavaPlugin plugin, AraziMenu araziMenu, YetkiliMenu yetkiliMenu) {
        this.plugin = plugin;
        this.araziMenu = araziMenu;
        this.yetkiliMenu = yetkiliMenu;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void open(Player player, JavaPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        String remaining = PlaceholderAPI.setPlaceholders(player, "%griefprevention_remainingclaims%");
        String total     = PlaceholderAPI.setPlaceholders(player, "%griefprevention_claims%");
        String bonus     = PlaceholderAPI.setPlaceholders(player, "%griefprevention_bonusclaims%");
        String accrued   = PlaceholderAPI.setPlaceholders(player, "%griefprevention_accruedclaims%");
        String rawOwner  = PlaceholderAPI.setPlaceholders(player, "%griefprevention_currentclaim_ownername%");
        String curOwner  = rawOwner.equalsIgnoreCase("Unclaimed") ? "Claimsiz" : rawOwner;

        // Slot 10 - Claim Bilgileri (SARI KAFA)
        // !! URL'yi buraya yaz: https://textures.minecraft.net/texture/XXXXX
        inv.setItem(10, makeSkullFromUrl(
                "SARI_KAFA_URL_BURAYA",
                "§6§lCLAIM BİLGİLERİ",
                List.of("",
                        "§8▸ §7Toplam Blok:   §e§l" + total,
                        "§8▸ §7Kalan Blok:    §a§l" + remaining,
                        "§8▸ §7Birikmiş:      §b§l" + accrued,
                        "§8▸ §7Bonus:         §d§l" + bonus,
                        "",
                        "§8▸ §7Şu an üstünde: §f" + curOwner,
                        "")));

        // Slot 13 - Claim Sat (PİSTON)
        inv.setItem(13, makeItem(Material.PISTON, "§c§lCLAIM SAT",
                List.of("",
                        "§7Claim bloğunu paraya çevir.",
                        "§8▸ §7Miktar yaz → para yatır",
                        "",
                        "§e§nTIKLA", "")));

        // Slot 15 - Yetkililer (MAVİ KAFA)
        // !! URL'yi buraya yaz: https://textures.minecraft.net/texture/XXXXX
        inv.setItem(15, makeSkullFromUrl(
                "MAVI_KAFA_URL_BURAYA",
                "§b§lYETKİLİLER",
                List.of("",
                        "§7Claimindeki yetkilileri yönet.",
                        "§8▸ §7Ekle / Kaldır",
                        "",
                        "§e§nTIKLA", "")));

        // Slot 19 - Arazilerim (ÇİMEN BLOK)
        inv.setItem(19, makeItem(Material.GRASS_BLOCK, "§a§lARAZİLERİM",
                List.of("",
                        "§7Sahip olduğun alanları gör.",
                        "§8▸ §aSol tık §7→ Işınlan",
                        "§8▸ §cSağ tık §7→ Sil",
                        "",
                        "§e§nTIKLA", "")));

        // Slot 22 - Claim Satın Al (ALTIN BLOK - pistonun altı)
        inv.setItem(22, makeItem(Material.GOLD_BLOCK, "§6§lCLAIM SATIN AL",
                List.of("",
                        "§7Para ile claim bloğu satın al.",
                        "§8▸ §7Miktar gir → ödeme otomatik",
                        "",
                        "§7Kalan bloğun: §e" + remaining,
                        "",
                        "§e§nTIKLA", "")));

        // Slot 24 - Claim Gönder (COBBLESTONE)
        inv.setItem(24, makeItem(Material.COBBLESTONE, "§7§lCLAIM GÖNDER",
                List.of("",
                        "§7Başka oyuncuya claim bloğu gönder.",
                        "§8▸ §7İsim → Miktar gir",
                        "",
                        "§7Kalan bloğun: §e" + remaining,
                        "",
                        "§e§nTIKLA", "")));

        // Slot 49 - Kapat (BARİYER)
        inv.setItem(49, makeItem(Material.BARRIER, "§c§lKAPAT",
                List.of("§7Menüyü kapat.")));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        switch (e.getRawSlot()) {
            case 19 -> {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> araziMenu.open(player), 1L);
            }
            case 15 -> {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> yetkiliMenu.open(player), 1L);
            }
            case 13 -> {
                player.closeInventory();
                ChatInputListener.get().await(player,
                        "Kaç claim bloğu satmak istiyorsun?",
                        amountStr -> {
                            try {
                                int amount = Integer.parseInt(amountStr);
                                if (amount <= 0) throw new NumberFormatException();
                                player.performCommand("sellclaimblocks " + amount);
                            } catch (NumberFormatException ex) {
                                player.sendMessage("§c✗ Geçersiz miktar.");
                            }
                        });
            }
            case 22 -> {
                player.closeInventory();
                ChatInputListener.get().await(player,
                        "Kaç claim bloğu satın almak istiyorsun?",
                        amountStr -> {
                            try {
                                int amount = Integer.parseInt(amountStr);
                                if (amount <= 0) throw new NumberFormatException();
                                player.performCommand("buyclaimblocks " + amount);
                            } catch (NumberFormatException ex) {
                                player.sendMessage("§c✗ Geçersiz miktar.");
                            }
                        });
            }
            case 24 -> {
                player.closeInventory();
                ChatInputListener.get().await(player,
                        "Bloğu göndermek istediğin oyuncunun adını yaz:",
                        targetName -> ChatInputListener.get().await(player,
                                "Kaç blok göndermek istiyorsun?",
                                amountStr -> {
                                    try {
                                        int amount = Integer.parseInt(amountStr);
                                        if (amount <= 0) throw new NumberFormatException();
                                        player.performCommand("giveclaimblocks " + targetName + " " + amount);
                                        player.sendMessage("§a✔ §e" + amount + " §7blok §f" + targetName + "§7'ye gönderildi.");
                                    } catch (NumberFormatException ex) {
                                        player.sendMessage("§c✗ Geçersiz miktar.");
                                    }
                                }));
            }
            case 49 -> player.closeInventory();
        }
    }

    private static ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeSkullFromUrl(String url, String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;
        if (!url.contains("BURAYA") && !url.isEmpty()) {
            try {
                String texture = java.util.Base64.getEncoder().encodeToString(
                        ("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes()
                );
                com.mojang.authlib.GameProfile profile =
                        new com.mojang.authlib.GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures",
                        new com.mojang.authlib.properties.Property("textures", texture));
                java.lang.reflect.Field f = meta.getClass().getDeclaredField("profile");
                f.setAccessible(true);
                f.set(meta, profile);
            } catch (Exception ignored) {}
        }
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }
}
