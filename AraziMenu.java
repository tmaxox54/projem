package dev.claimgui.gui;

import dev.claimgui.manager.ClaimData;
import dev.claimgui.manager.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AraziMenu implements Listener {

    private final JavaPlugin plugin;
    private final ClaimManager manager;
    // Oyuncu → açık menüdeki claim listesi (sıra korunuyor)
    private final Map<UUID, List<ClaimData>> openMenus = new HashMap<>();

    private static final String TITLE = "§0§l✦ §8ARAZİLERİM §0§l✦";

    // İçerik slotları (kenar hariç iç alan)
    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public AraziMenu(JavaPlugin plugin, ClaimManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        List<ClaimData> claims = new ArrayList<>(manager.getClaimsOf(player.getUniqueId()));
        openMenus.put(player.getUniqueId(), claims);

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Kenar - siyah cam
        ItemStack cam = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++)  inv.setItem(i, cam);
        for (int i = 45; i < 54; i++) inv.setItem(i, cam);
        inv.setItem(9, cam);  inv.setItem(17, cam);
        inv.setItem(18, cam); inv.setItem(26, cam);
        inv.setItem(27, cam); inv.setItem(35, cam);
        inv.setItem(36, cam); inv.setItem(44, cam);

        // Geri ve yenile
        inv.setItem(45, makeItem(Material.ARROW, "§c§l« §fGERİ DÖN", List.of("§7Ana menüye dön.")));
        inv.setItem(49, makeItem(Material.NETHER_STAR, "§e§l✦ §fYENİLE", List.of("§7Listeyi yenile.")));

        // Claim sayısı bilgi
        if (claims.isEmpty()) {
            inv.setItem(22, makeItem(Material.BARRIER, "§c§lArazi Yok",
                    List.of("", "§7Henüz hiç claim alanın yok.",
                            "§7Altın kürek ile alan belirle.", "")));
        }

        // Dinamik claim slotları
        for (int i = 0; i < claims.size() && i < CONTENT_SLOTS.length; i++) {
            ClaimData c = claims.get(i);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§8▸ §7Dünya: §a" + c.getWorld());
            lore.add("§8▸ §7Konum: §eX:" + c.getCenterX() + " §7/ §eZ:" + c.getCenterZ());
            lore.add("§8▸ §7Boyut: §b" + c.getDimensions() + " §7= §b" + c.getArea() + " §7blok");
            lore.add("§8▸ §7Yetkili: §f" + c.getAllTrusted().size() + " §7kişi");
            lore.add("");
            lore.add("§a● Sol Tık §7→ Işınlan");
            lore.add("§c● Sağ Tık §7→ §lSİL");
            lore.add("");
            inv.setItem(CONTENT_SLOTS[i],
                    makeItem(Material.GRASS_BLOCK, "§a§l▸ §fARAZİ #" + (i + 1), lore));
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        List<ClaimData> claims = openMenus.get(player.getUniqueId());

        // Geri
        if (slot == 45) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> MainMenu.open(player, plugin), 1L);
            return;
        }

        // Yenile
        if (slot == 49) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> open(player), 1L);
            return;
        }

        // İçerik slotu tıklandı mı?
        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            if (slot == CONTENT_SLOTS[i] && claims != null && i < claims.size()) {
                ClaimData claim = claims.get(i);

                if (e.isLeftClick()) {
                    // Işınlan
                    player.closeInventory();
                    org.bukkit.World world = Bukkit.getWorld(claim.getWorld());
                    if (world == null) {
                        player.sendMessage("§c✗ Dünya bulunamadı: " + claim.getWorld());
                        return;
                    }
                    Location loc = new Location(world, claim.getCenterX(), 100, claim.getCenterZ());
                    // Y koordinatını yüksekten bul (düşme engeli)
                    loc.setY(world.getHighestBlockYAt(claim.getCenterX(), claim.getCenterZ()) + 1);
                    player.teleport(loc);
                    player.sendMessage("§a✔ §f" + claim.getWorld() + " §7→ §eX:" + claim.getCenterX() + " Z:" + claim.getCenterZ());

                } else if (e.isRightClick()) {
                    // Sil - önce onay sor
                    player.closeInventory();
                    player.sendMessage("§c§l⚠ §fBu araziyi silmek istediğine emin misin?");
                    player.sendMessage("§7Arazi: §e" + claim.getWorld() + " X:" + claim.getCenterX() + " Z:" + claim.getCenterZ());
                    player.sendMessage("§7Onaylamak için §c§lonay §7yaz, iptal için §aiptal§7.");

                    dev.claimgui.listener.ChatInputListener.get().await(player,
                            "Silmek için 'onay' yaz:", input -> {
                                if (input.equalsIgnoreCase("onay")) {
                                    // GP'nin deleteclaim komutu oyuncu o claimde durmalı
                                    // O yüzden önce ışınlayıp sonra komutu çalıştırıyoruz
                                    World w = Bukkit.getWorld(claim.getWorld());
                                    if (w != null) {
                                        Location l = new Location(w, claim.getCenterX(),
                                                w.getHighestBlockYAt(claim.getCenterX(), claim.getCenterZ()) + 1,
                                                claim.getCenterZ());
                                        player.teleport(l);
                                    }
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        player.performCommand("deleteclaim");
                                        player.sendMessage("§a✔ Arazi silindi.");
                                    }, 10L);
                                } else {
                                    player.sendMessage("§a✔ İptal edildi.");
                                }
                            });
                }
                return;
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
}
