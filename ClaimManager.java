package dev.claimgui.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimManager {

    private final JavaPlugin plugin;
    private final Map<UUID, List<ClaimData>> ownerClaims = new ConcurrentHashMap<>();

    public ClaimManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        ownerClaims.clear();

        File claimDir = new File(plugin.getDataFolder().getParentFile(),
                "GriefPrevention/ClaimData");

        if (!claimDir.exists() || !claimDir.isDirectory()) {
            plugin.getLogger().warning("GriefPrevention/ClaimData klasörü bulunamadı!");
            return;
        }

        File[] files = claimDir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;

        int count = 0;
        for (File f : files) {
            try {
                parse(f);
                count++;
            } catch (Exception e) {
                plugin.getLogger().warning("Claim okunamadı: " + f.getName() + " - " + e.getMessage());
            }
        }
        plugin.getLogger().info(count + " claim yüklendi.");
    }

    private void parse(File file) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String ownerStr = cfg.getString("Owner");
        if (ownerStr == null || ownerStr.isBlank()) return;

        UUID owner;
        try { owner = UUID.fromString(ownerStr.trim()); }
        catch (IllegalArgumentException e) { return; }

        String lesser  = cfg.getString("Lesser Boundary Corner");
        String greater = cfg.getString("Greater Boundary Corner");
        if (lesser == null || greater == null) return;

        String[] l = lesser.split(";");
        String[] g = greater.split(";");
        if (l.length < 4 || g.length < 4) return;

        String world = l[0];
        int x1 = Integer.parseInt(l[1]);
        int z1 = Integer.parseInt(l[3]);
        int x2 = Integer.parseInt(g[1]);
        int z2 = Integer.parseInt(g[3]);

        List<UUID> builders   = parseUUIDs(cfg.getStringList("Builders"));
        List<UUID> containers = parseUUIDs(cfg.getStringList("Containers"));
        List<UUID> accessors  = parseUUIDs(cfg.getStringList("Accessors"));
        List<UUID> managers   = parseUUIDs(cfg.getStringList("Managers"));

        ClaimData data = new ClaimData(
                world,
                Math.min(x1, x2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(z1, z2),
                file.getName(),
                builders, containers, accessors, managers
        );

        ownerClaims.computeIfAbsent(owner, k -> new ArrayList<>()).add(data);
    }

    private List<UUID> parseUUIDs(List<String> list) {
        List<UUID> result = new ArrayList<>();
        if (list == null) return result;
        for (String s : list) {
            if (s == null || s.isBlank()) continue;
            try { result.add(UUID.fromString(s.trim())); }
            catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    public List<ClaimData> getClaimsOf(UUID uuid) {
        return ownerClaims.getOrDefault(uuid, Collections.emptyList());
    }

    public int getClaimCount(UUID uuid) {
        return getClaimsOf(uuid).size();
    }
}
