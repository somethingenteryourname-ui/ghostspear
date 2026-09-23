package dev.ghostspear;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Logger;

/** Snapshot of config.yml values. Rebuilt on /ghostspear reload. */
public final class Settings {

    // item
    public final Material material;
    public final String name;
    public final List<String> lore;
    public final boolean unbreakable;
    public final boolean glint;

    // dash
    public final boolean dashRequireSneak;
    public final double dashStrength;
    public final double dashVerticalBoost;
    public final double dashMaxVertical;
    public final int dashCooldownTicks;
    public final int noFallTicks;

    // ghost
    public final boolean ghostEnabled;
    public final int ghostLifetimeTicks;
    public final int trailCount;
    public final int trailIntervalTicks;
    public final boolean ghostGlowing;

    // charge
    public final boolean chargeRequireDash;
    public final int boostWindowTicks;
    public final double chargeDamage;
    public final double reach;
    public final double hitboxSize;
    public final boolean hitPlayers;
    public final boolean hitMobs;
    public final boolean consumeBoostOnHit;

    // messages
    public final String msgDashCooldown;
    public final String msgDashUsed;
    public final String msgPierce;

    public Settings(FileConfiguration c, Logger log) {
        material = resolveMaterial(
                c.getString("item.material", "NETHERITE_SPEAR"),
                c.getString("item.fallback-material", "TRIDENT"),
                log);
        name = c.getString("item.name", "<aqua>Ghost Spear");
        lore = c.getStringList("item.lore");
        unbreakable = c.getBoolean("item.unbreakable", true);
        glint = c.getBoolean("item.glint", true);

        dashRequireSneak = c.getBoolean("dash.require-sneak", false);
        dashStrength = c.getDouble("dash.strength", 2.2);
        dashVerticalBoost = c.getDouble("dash.vertical-boost", 0.25);
        dashMaxVertical = Math.abs(c.getDouble("dash.max-vertical", 0.9));
        dashCooldownTicks = Math.max(0, c.getInt("dash.cooldown-ticks", 30));
        noFallTicks = Math.max(0, c.getInt("dash.no-fall-damage-ticks", 60));

        ghostEnabled = c.getBoolean("ghost.enabled", true);
        ghostLifetimeTicks = Math.max(1, c.getInt("ghost.lifetime-ticks", 14));
        trailCount = Math.max(0, c.getInt("ghost.trail-count", 3));
        trailIntervalTicks = Math.max(1, c.getInt("ghost.trail-interval-ticks", 2));
        ghostGlowing = c.getBoolean("ghost.glowing", true);

        chargeRequireDash = c.getBoolean("charge.require-dash", true);
        boostWindowTicks = Math.max(1, c.getInt("charge.boost-window-ticks", 40));
        chargeDamage = c.getDouble("charge.damage", 1000.0);
        reach = c.getDouble("charge.reach", 3.5);
        hitboxSize = c.getDouble("charge.hitbox-size", 0.6);
        hitPlayers = c.getBoolean("charge.hit-players", true);
        hitMobs = c.getBoolean("charge.hit-mobs", true);
        consumeBoostOnHit = c.getBoolean("charge.consume-boost-on-hit", true);

        msgDashCooldown = c.getString("messages.dash-cooldown", "<gray>Dash ready in <white><seconds>s");
        msgDashUsed = c.getString("messages.dash-used", "<aqua>Phantom Dash!");
        msgPierce = c.getString("messages.pierce", "<dark_aqua>Soul Pierce!");
    }

    private static Material resolveMaterial(String primary, String fallback, Logger log) {
        Material m = primary == null ? null : Material.matchMaterial(primary);
        if (m != null && m.isItem()) {
            return m;
        }
        log.warning("item.material '" + primary + "' isn't a valid item on this server, using fallback '" + fallback + "'.");
        Material f = fallback == null ? null : Material.matchMaterial(fallback);
        return (f != null && f.isItem()) ? f : Material.TRIDENT;
    }
}
