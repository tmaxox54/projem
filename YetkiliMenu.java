package dev.claimgui.gui;

import dev.claimgui.manager.ClaimData;
import dev.claimgui.manager.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class YetkiliMenu implements Listener {

    private final JavaPlugin plugin;
    private final ClaimManager manager;

    // Oyuncu UUID → açık menüdeki (trustedUUID, claimFileName, rolAdı) listesi
    private final Map<UUID, List<TrustEntry>> openMenus = new HashMap<>();

    private static final String TITLE = "§0§l✦ §8YETKİLİ YÖNETİMİ §0§l✦";

    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public YetkiliMenu(JavaPlugin plugin, ClaimManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        // Tüm claimlerdeki trusted kişileri topla
        List<ClaimData> claims = manager.getClaimsOf(player.getUniqueId());
        List<TrustEntry> entries = new ArrayList<>();

        for (ClaimData claim : claims) {
            List<UUID> trusted = claim.getAllTrusted();
            for (UUID uuid : trusted) {
                entries.add(new TrustEntry(uuid, claim));
            }
        }

        openMenus.put(player.getUniqueId(), entries);
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Kenar
        ItemStack cam = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) inv.setItem(i, cam);
        for (int i = 45; i < 54; i++) inv.setItem(i, cam);
        inv.setItem(9, cam);  inv.setItem(17, cam);
        inv.setItem(18, cam); inv.setItem(26, cam);
        inv.setItem(27, cam); inv.setItem(35, cam);
        inv.setItem(36, cam); inv.setItem(44, cam);

        // Butonlar
        inv.setItem(45, makeItem(Material.ARROW, "§c§l« §fGERİ DÖN", List.of("§7Ana menüye dön.")));
        inv.setItem(49, makeItem(Material.NETHER_STAR, "§e§l✦ §fYENİLE", List.of("§7Listeyi yenile.")));
        inv.setItem(53, makeItem(Material.LIME_DYE, "§a§l+ §fYETKİLİ EKLE",
                List.of("", "§7Claiminin içinde durarak tıkla,",
                        "§7oyuncu adını chat'e yaz.", "",
                        "§c! §7Claiminde olmak zorundasın.", "",
                        "§e§nTIKLA", "")));

        // Boşsa bilgi
        if (entries.isEmpty()) {
            inv.setItem(22, makeItem(Material.BARRIER, "§c§lYetkili Yok",
                    List.of("", "§7Hiçbir claiminde yetkili kişi yok.", "")));
        }

        // Dinamik yetkili kafaları
        for (int i = 0; i < entries.size() && i < CONTENT_SLOTS.length; i++) {
            TrustEntry entry = entries.get(i);
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.uuid);
            String name = op.getName() != null ? op.getName() : "Bilinmiyor";

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§8▸ §7Arazi: §e" + entry.claim.getWorld()
                    + " X:" + entry.claim.getCenterX()
                    + " Z:" + entry.claim.getCenterZ());
            lore.add("§8▸ §7Rol: §b" + entry.claim.getRole(entry.uuid));
            lore.add("");
            lore.add("§c● Sağ Tık §7→ Yetkiyi kaldır");
            lore.add("");

            inv.setItem(CONTENT_SLOTS[i], makeSkull(op, "§f" + name, lore));
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        List<TrustEntry> entries = openMenus.get(player.getUniqueId());

        if (slot == 45) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> MainMenu.open(player, plugin), 1L);
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> open(player), 1L);
            return;
        }

        // Yetkili ekle
        if (slot == 53) {
            player.closeInventory();
            dev.claimgui.listener.ChatInputListener.get().await(player,
                    "Yetkili vermek istediğin oyuncunun adını yaz:",
                    targetName -> {
                        player.performCommand("trust " + targetName);
                        // Claim verisini yenile
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () ->
                                dev.claimgui.Main.getInstance().getClaimManager().loadAll(), 20L);
                    });
            return;
        }

        // İçerik tıklaması - sadece sağ tık (untrust)
        if (e.isRightClick() && entries != null) {
            for (int i = 0; i < CONTENT_SLOTS.length; i++) {
                if (slot == CONTENT_SLOTS[i] && i < entries.size()) {
                    TrustEntry entry = entries.get(i);
                    OfflinePlayer op = Bukkit.getOfflinePlayer(entry.uuid);
                    String name = op.getName();
                    if (name == null) {
                        player.sendMessage("§c✗ Oyuncu adı bulunamadı.");
                        return;
                    }
                    player.closeInventory();
                    // O claimde olması lazım, önce ışınla
                    org.bukkit.World world = Bukkit.getWorld(entry.claim.getWorld());
                    if (world != null) {
                        org.bukkit.Location loc = new org.bukkit.Location(world,
                                entry.claim.getCenterX(),
                                world.getHighestBlockYAt(entry.claim.getCenterX(), entry.claim.getCenterZ()) + 1,
                                entry.claim.getCenterZ());
                        player.teleport(loc);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.performCommand("untrust " + name);
                        player.sendMessage("§a✔ §f" + name + " §7yetkisi kaldırıldı.");
                        // Yenile
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () ->
                                dev.claimgui.Main.getInstance().getClaimManager().loadAll(), 20L);
                    }, 5L);
                    return;
                }
            }
        }
    }

    private ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSkull(OfflinePlayer op, String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;
        meta.setOwningPlayer(op);
        meta.setDisplayName(name);
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private record TrustEntry(UUID uuid, ClaimData claim) {}
}
