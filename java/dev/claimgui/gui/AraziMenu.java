package dev.claimgui.gui;

import dev.claimgui.manager.ClaimData;
import dev.claimgui.manager.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AraziMenu implements Listener {

    private final JavaPlugin plugin;
    private final ClaimManager manager;
    private final Map<UUID, List<ClaimData>> openMenus = new HashMap<>();

    private static final String TITLE = "§0       ᴀʀᴀᴢɪʟᴇʀɪᴍ";

    // !! ARAZI KAFASI URL'İNİ BURAYA YAZ !!
    // minecraft-heads.com → For Developers → textures.minecraft.net/texture/...
    private static final String ARAZI_KAFA_URL = "ARAZI_KAFA_URL_BURAYA";

    // İçerik slotları - 4 satır × 7 sütun
    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
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
        for (int i = 0; i < 9; i++) inv.setItem(i, cam);
        for (int i = 45; i < 54; i++) inv.setItem(i, cam);
        inv.setItem(9, cam);  inv.setItem(17, cam);
        inv.setItem(18, cam); inv.setItem(26, cam);
        inv.setItem(27, cam); inv.setItem(35, cam);
        inv.setItem(36, cam); inv.setItem(44, cam);

        // Geri butonu
        inv.setItem(49, makeItem(Material.BARRIER, "§c§lKAPAT", List.of("§7Menüyü kapat.")));

        // Boşsa bilgi
        if (claims.isEmpty()) {
            inv.setItem(22, makeItem(Material.BARRIER, "§c§lArazi Yok",
                    List.of("", "§7Henüz hiç claim alanın yok.",
                            "§7Altın kürek ile alan belirle.", "")));
            player.openInventory(inv);
            return;
        }

        // Her claim için kafa
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
                    makeSkullFromUrl(ARAZI_KAFA_URL, "§a§l" + (i + 1) + ". §fARAZİ", lore));
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

        // Kapat
        if (slot == 49) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> MainMenu.open(player, plugin), 1L);
            return;
        }

        // İçerik slotu tıklandı mı?
        for (int i = 0; i < CONTENT_SLOTS.length; i++) {
            if (slot == CONTENT_SLOTS[i] && claims != null && i < claims.size()) {
                ClaimData claim = claims.get(i);

                if (e.isLeftClick()) {
                    // Işınlan
                    player.closeInventory();
                    World world = Bukkit.getWorld(claim.getWorld());
                    if (world == null) {
                        player.sendMessage("§c✗ Dünya bulunamadı: " + claim.getWorld());
                        return;
                    }
                    int y = world.getHighestBlockYAt(claim.getCenterX(), claim.getCenterZ()) + 1;
                    player.teleport(new Location(world, claim.getCenterX(), y, claim.getCenterZ()));
                    player.sendMessage("§a✔ §e" + claim.getWorld()
                            + " §7→ X:" + claim.getCenterX() + " Z:" + claim.getCenterZ());

                } else if (e.isRightClick()) {
                    // Sil - onay sor
                    player.closeInventory();
                    player.sendMessage("§c§l⚠ §fBu araziyi silmek istediğine emin misin?");
                    player.sendMessage("§7" + claim.getWorld()
                            + " X:" + claim.getCenterX() + " Z:" + claim.getCenterZ());
                    player.sendMessage("§7Onaylamak için §c§lonay §7yaz.");

                    dev.claimgui.listener.ChatInputListener.get().await(player,
                            "Silmek için 'onay' yaz:",
                            input -> {
                                if (input.equalsIgnoreCase("onay")) {
                                    World w = Bukkit.getWorld(claim.getWorld());
                                    if (w != null) {
                                        int y = w.getHighestBlockYAt(
                                                claim.getCenterX(), claim.getCenterZ()) + 1;
                                        player.teleport(new Location(w,
                                                claim.getCenterX(), y, claim.getCenterZ()));
                                    }
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        player.performCommand("deleteclaim");
                                        player.sendMessage("§a✔ Arazi silindi.");
                                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
                                                () -> dev.claimgui.Main.getInstance()
                                                        .getClaimManager().loadAll(), 20L);
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
