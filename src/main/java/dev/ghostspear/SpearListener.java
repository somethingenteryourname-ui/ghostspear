package dev.ghostspear;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public final class SpearListener implements Listener {

    private final GhostSpearPlugin plugin;

    public SpearListener(GhostSpearPlugin plugin) {
        this.plugin = plugin;
    }

    /** Right-clicking a block (door, chest...) also swings the arm - remember it so that doesn't trigger a dash. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            plugin.playerState().markBlockUse(event.getPlayer().getUniqueId(), Bukkit.getCurrentTick());
        }
    }

    /** Swinging the Ghost Spear (the jab / left-click) = Phantom Dash. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }
        Player player = event.getPlayer();
        if (!plugin.spearItem().is(player.getInventory().getItemInMainHand())) {
            return;
        }
        if (!player.hasPermission("ghostspear.use") || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (player.isHandRaised()) {
            return; // charging, not jabbing
        }

        Settings s = plugin.settings();
        PlayerState state = plugin.playerState();
        UUID id = player.getUniqueId();
        int now = Bukkit.getCurrentTick();

        if (state.usedBlockThisTick(id, now)) {
            return;
        }
        if (s.dashRequireSneak && !player.isSneaking()) {
            return;
        }

        int left = state.dashCooldownLeft(id, now, s.dashCooldownTicks);
        if (left > 0) {
            String seconds = String.format("%.1f", left / 20.0);
            player.sendActionBar(MiniMessage.miniMessage().deserialize(
                    s.msgDashCooldown, Placeholder.unparsed("seconds", seconds)));
            return;
        }

        dash(player, s, now);
    }

    private void dash(Player player, Settings s, int now) {
        Location start = player.getLocation();
        World world = player.getWorld();

        Vector velocity = start.getDirection().multiply(s.dashStrength);
        double y = velocity.getY() + s.dashVerticalBoost;
        velocity.setY(Math.max(-s.dashMaxVertical, Math.min(s.dashMaxVertical, y)));
        player.setVelocity(velocity);

        plugin.playerState().markDash(player.getUniqueId(), now, s.boostWindowTicks, s.noFallTicks);

        // Ghost where you started...
        plugin.ghostSpawner().spawn(player, start);
        // ...and a few more along the path
        for (int i = 1; i <= s.trailCount; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !player.isDead()) {
                    plugin.ghostSpawner().spawn(player, player.getLocation());
                }
            }, (long) i * s.trailIntervalTicks);
        }

        world.playSound(start, Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 1.3f);
        world.playSound(start, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.6f);
        world.spawnParticle(Particle.CLOUD, start.clone().add(0, 0.2, 0), 15, 0.3, 0.1, 0.3, 0.05);
        world.spawnParticle(Particle.SWEEP_ATTACK, start.clone().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0.0);

        player.sendActionBar(SpearItem.text(s.msgDashUsed));
    }

    /** No fall damage right after a dash, and ghosts can never be hurt. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (plugin.ghostSpawner().isGhost(event.getEntity())) {
            event.setCancelled(true);
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && event.getEntity() instanceof Player player
                && plugin.playerState().hasFallProtection(player.getUniqueId(), Bukkit.getCurrentTick())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.playerState().clear(event.getPlayer().getUniqueId());
    }
}
