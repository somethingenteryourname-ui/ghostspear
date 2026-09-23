package dev.ghostspear;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Every tick: if a player is holding right-click (charging) with the Ghost Spear
 * during their post-dash boost window, anything right in front of them gets
 * "Soul Pierced" for massive damage.
 */
public final class ChargeTask extends BukkitRunnable {

    private final GhostSpearPlugin plugin;

    public ChargeTask(GhostSpearPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Settings s = plugin.settings();
        PlayerState state = plugin.playerState();
        int now = Bukkit.getCurrentTick();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isHandRaised()) continue;
            if (!plugin.spearItem().is(player.getActiveItem())) continue;
            if (!player.hasPermission("ghostspear.use")) continue;
            if (player.getGameMode() == GameMode.SPECTATOR) continue;

            UUID id = player.getUniqueId();
            if (s.chargeRequireDash && !state.isBoosted(id, now)) continue;
            if (!state.canPierce(id, now)) continue;

            LivingEntity target = findTarget(player, s);
            if (target == null) continue;

            pierce(player, target, s, state, now);
        }
    }

    private LivingEntity findTarget(Player player, Settings s) {
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();

        RayTraceResult hit = world.rayTraceEntities(eye, direction, s.reach, s.hitboxSize,
                entity -> isValidTarget(player, entity, s));
        if (hit == null || !(hit.getHitEntity() instanceof LivingEntity target)) {
            return null;
        }

        // Don't pierce through walls
        double distance = hit.getHitPosition().distance(eye.toVector());
        RayTraceResult wall = world.rayTraceBlocks(eye, direction, distance, FluidCollisionMode.NEVER, true);
        if (wall != null && wall.getHitBlock() != null) {
            return null;
        }
        return target;
    }

    private boolean isValidTarget(Player attacker, Entity entity, Settings s) {
        if (entity.equals(attacker)) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        if (living.isDead() || !living.isValid()) return false;
        if (entity instanceof ArmorStand) return false;
        if (plugin.ghostSpawner().isGhost(entity)) return false;

        if (entity instanceof Player target) {
            if (!s.hitPlayers) return false;
            GameMode gm = target.getGameMode();
            return gm != GameMode.CREATIVE && gm != GameMode.SPECTATOR;
        }
        return s.hitMobs;
    }

    private void pierce(Player player, LivingEntity target, Settings s, PlayerState state, int now) {
        UUID id = player.getUniqueId();
        state.markPierce(id, now);

        // Skip the usual "just got hit" immunity so the pierce always lands
        target.setNoDamageTicks(0);
        // Goes through the normal damage event, so PvP-off areas / region plugins still block it
        target.damage(s.chargeDamage, player);

        if (s.consumeBoostOnHit) {
            state.consumeBoost(id);
        }

        World world = target.getWorld();
        Location center = target.getLocation().add(0, target.getHeight() / 2.0, 0);
        world.spawnParticle(Particle.SOUL, center, 25, 0.3, 0.5, 0.3, 0.05);
        world.spawnParticle(Particle.CRIT, center, 30, 0.4, 0.5, 0.4, 0.3);
        world.spawnParticle(Particle.SWEEP_ATTACK, center, 1, 0, 0, 0, 0);
        world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.7f);
        world.playSound(center, Sound.ITEM_TRIDENT_HIT, 1.0f, 0.6f);

        player.sendActionBar(SpearItem.text(s.msgPierce));
    }
}
