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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MainMenu implements Listener {

    private static final String TITLE = "§0§l✦ §8CLAIM KONTROL PANELİ §0§l✦";

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

        // Kenar - siyah cam
        ItemStack cam = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) inv.setItem(i, cam);
        for (int i = 45; i < 54; i++) inv.setItem(i, cam);
        inv.setItem(9, cam);  inv.setItem(17, cam);
        inv.setItem(18, cam); inv.setItem(26, cam);
        inv.setItem(27, cam); inv.setItem(35, cam);
        inv.setItem(36, cam); inv.setItem(44, cam);

        // Yenile
        inv.setItem(49, makeItem(Material.NETHER_STAR, "§e§l✦ §fYENİLE",
                List.of("", "§7Menüyü yenile.", "")));

        // Placeholder değerleri
        String remaining = PlaceholderAPI.setPlaceholders(player, "%griefprevention_remainingclaims%");
        String total     = PlaceholderAPI.setPlaceholders(player, "%griefprevention_claims%");
        String bonus     = PlaceholderAPI.setPlaceholders(player, "%griefprevention_bonusclaims%");
        String accrued   = PlaceholderAPI.setPlaceholders(player, "%griefprevention_accruedclaims%");
        String curOwner  = PlaceholderAPI.setPlaceholders(player, "%griefprevention_currentclaim_ownername%");

        // Bilgi bloğu - merkez
        inv.setItem(22, makeItem(Material.GOLD_BLOCK, "§6§l✦ KİŞİSEL CLAIM BİLGİLERİ",
                List.of("",
                        "§8▸ §7Toplam Blok:   §e§l" + total,
                        "§8▸ §7Kalan Blok:    §a§l" + remaining,
                        "§8▸ §7Birikmiş:      §b§l" + accrued,
                        "§8▸ §7Bonus:         §d§l" + bonus,
                        "",
                        "§8▸ §7Şu an üstünde: §f" + curOwner,
                        "",
                        "§7Claim almak için §ealtın kürek§7 kullan.",
                        "")));

        // ARAZİLER - sol
        inv.setItem(11, makeItem(Material.GRASS_BLOCK, "§a§l» §fARAZİLERİM",
                List.of("",
                        "§7Sahip olduğun alanları gör.",
                        "§8▸ §aSol tık §7→ Işınlan",
                        "§8▸ §cSağ tık §7→ Sil",
                        "",
                        "§e§nTIKLA", "")));

        // YETKİLİLER - sağ
        inv.setItem(33, makeItem(Material.PLAYER_HEAD, "§b§l» §fBÖLGE YETKİLİLERİ",
                List.of("",
                        "§7Claimindeki yetkilileri yönet.",
                        "§8▸ §bSol tık §7→ Listeyi gör",
                        "",
                        "§e§nTIKLA", "")));

        // BLOK GÖNDER
        inv.setItem(29, makeItem(Material.ENDER_PEARL, "§d§l» §fBLOK GÖNDER",
                List.of("",
                        "§7Başka oyuncuya claim bloğu gönder.",
                        "§8▸ §7İsim → Miktar gir",
                        "",
                        "§7Kalan bloğun: §e" + remaining,
                        "",
                        "§e§nTIKLA", "")));

        // BLOK SATIN AL
        inv.setItem(15, makeItem(Material.EMERALD, "§2§l» §aBLOK SATIN AL",
                List.of("",
                        "§7Para ile claim bloğu satın al.",
                        "§8▸ §7Miktar gir → ödeme otomatik",
                        "",
                        "§7Kalan bloğun: §e" + remaining,
                        "",
                        "§e§nTIKLA", "")));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        switch (e.getRawSlot()) {
            case 11 -> { // Araziler
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> araziMenu.open(player), 1L);
            }
            case 33 -> { // Yetkililer
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> yetkiliMenu.open(player), 1L);
            }
            case 29 -> { // Blok gönder
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
                                        player.sendMessage("§a✔ §f" + targetName + " §7kişisine §e" + amount + " §7blok gönderildi.");
                                    } catch (NumberFormatException ex) {
                                        player.sendMessage("§c✗ Geçersiz miktar! Sadece pozitif sayı yaz.");
                                    }
                                }));
            }
            case 15 -> { // Blok satın al
                player.closeInventory();
                ChatInputListener.get().await(player,
                        "Kaç claim bloğu satın almak istiyorsun?",
                        amountStr -> {
                            try {
                                int amount = Integer.parseInt(amountStr);
                                if (amount <= 0) throw new NumberFormatException();
                                player.performCommand("buyclaimblocks " + amount);
                            } catch (NumberFormatException ex) {
                                player.sendMessage("§c✗ Geçersiz miktar! Sadece pozitif sayı yaz.");
                            }
                        });
            }
            case 49 -> { // Yenile
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> open(player, plugin), 1L);
            }
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
}
