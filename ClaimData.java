package dev.claimgui.manager;

import java.util.List;
import java.util.UUID;

public class ClaimData {
    private final String world;
    private final int minX, minZ, maxX, maxZ;
    private final String fileName;
    private final List<UUID> builders;
    private final List<UUID> containers;
    private final List<UUID> accessors;
    private final List<UUID> managers;

    public ClaimData(String world, int minX, int minZ, int maxX, int maxZ,
                     String fileName, List<UUID> builders, List<UUID> containers,
                     List<UUID> accessors, List<UUID> managers) {
        this.world = world;
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.fileName = fileName;
        this.builders = builders;
        this.containers = containers;
        this.accessors = accessors;
        this.managers = managers;
    }

    public String getWorld()    { return world; }
    public int getCenterX()     { return (minX + maxX) / 2; }
    public int getCenterZ()     { return (minZ + maxZ) / 2; }
    public int getMinX()        { return minX; }
    public int getMinZ()        { return minZ; }
    public int getMaxX()        { return maxX; }
    public int getMaxZ()        { return maxZ; }
    public int getArea()        { return Math.abs(maxX - minX) * Math.abs(maxZ - minZ); }
    public String getDimensions() { return Math.abs(maxX - minX) + "x" + Math.abs(maxZ - minZ); }
    public String getFileName() { return fileName; }
    public List<UUID> getBuilders()   { return builders; }
    public List<UUID> getContainers() { return containers; }
    public List<UUID> getAccessors()  { return accessors; }
    public List<UUID> getManagers()   { return managers; }

    /** Tüm trusted UUID'leri tek liste olarak döner */
    public List<UUID> getAllTrusted() {
        java.util.Set<UUID> all = new java.util.LinkedHashSet<>();
        all.addAll(managers);
        all.addAll(builders);
        all.addAll(containers);
        all.addAll(accessors);
        return new java.util.ArrayList<>(all);
    }

    public String getRole(UUID uuid) {
        if (managers.contains(uuid))   return "Yönetici";
        if (builders.contains(uuid))   return "İnşaatçı";
        if (containers.contains(uuid)) return "Konteyner";
        if (accessors.contains(uuid))  return "Erişimci";
        return "Yetkili";
    }
}
