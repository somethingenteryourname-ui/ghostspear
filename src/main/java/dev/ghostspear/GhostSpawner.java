package dev.ghostspear;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Spawns "ghost" copies of a player using the Mannequin entity (added in 1.21.9),
 * which renders with the player's own skin. Ghosts are frozen, invulnerable,
 * never saved to disk, and removed after a few ticks.
 */
public final class GhostSpawner {

    private final GhostSpearPlugin plugin;
    private final Set<UUID> activeGhosts = new HashSet<>();
    private boolean mannequinBroken = false;

    public GhostSpawner(GhostSpearPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawn(Player player, Location at) {
        Settings s = plugin.settings();
        if (!s.ghostEnabled) {
            return;
        }
        World world = at.getWorld();
        if (world == null) {
            return;
        }

        if (!mannequinBroken) {
            try {
                spawnMannequin(player, at, s);
                return;
            } catch (Throwable t) {
                mannequinBroken = true;
                plugin.getLogger().warning("Couldn't spawn Mannequin ghosts (" + t
                        + "). Falling back to particle ghosts. Are you running Paper 1.21.11?");
            }
        }
        spawnParticleGhost(at);
    }

    private void spawnMannequin(Player player, Location at, Settings s) {
        World world = at.getWorld();
        Location loc = at.clone();

        Consumer<Mannequin> setup = m -> {
            m.setProfile(ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
            m.setMainHand(player.getMainHand());
            m.setDescription(null);          // hide the "NPC" text under the name
            m.setCustomNameVisible(false);
            m.setAI(false);
            m.setGravity(false);
            m.setImmovable(true);
            m.setInvulnerable(true);
            m.setSilent(true);
            m.setCollidable(false);
            m.setPersistent(false);          // never saved, so no leftover ghosts after a crash
            m.setGlowing(s.ghostGlowing);
            m.setBodyYaw(loc.getYaw());
            m.getPersistentDataContainer().set(plugin.ghostKey(), PersistentDataType.BYTE, (byte) 1);

            EntityEquipment eq = m.getEquipment();
            if (eq != null) {
                eq.setItemInMainHand(player.getInventory().getItemInMainHand().clone());
                eq.setHelmet(player.getInventory().getHelmet());
                eq.setChestplate(player.getInventory().getChestplate());
                eq.setLeggings(player.getInventory().getLeggings());
                eq.setBoots(player.getInventory().getBoots());
            }
        };

        Mannequin ghost = world.spawn(loc, Mannequin.class, setup);
        UUID id = ghost.getUniqueId();
        activeGhosts.add(id);

        world.spawnParticle(Particle.SOUL, loc.clone().add(0, 1, 0), 6, 0.25, 0.5, 0.25, 0.01);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeGhosts.remove(id);
            if (ghost.isValid()) {
                Location fade = ghost.getLocation().add(0, 1, 0);
                ghost.getWorld().spawnParticle(Particle.CLOUD, fade, 10, 0.25, 0.6, 0.25, 0.01);
                ghost.remove();
            }
        }, s.ghostLifetimeTicks);
    }

    /** Backup look if Mannequins aren't available: a quick body-shaped puff of particles. */
    private void spawnParticleGhost(Location at) {
        World world = at.getWorld();
        if (world == null) return;
        for (double y = 0.1; y <= 1.9; y += 0.3) {
            world.spawnParticle(Particle.END_ROD, at.clone().add(0, y, 0), 2, 0.15, 0.05, 0.15, 0.0);
        }
        world.spawnParticle(Particle.SOUL, at.clone().add(0, 1, 0), 8, 0.25, 0.6, 0.25, 0.01);
    }

    public boolean isGhost(Entity entity) {
        return entity.getPersistentDataContainer().has(plugin.ghostKey(), PersistentDataType.BYTE);
    }

    public void removeAll() {
        for (UUID id : new HashSet<>(activeGhosts)) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) {
                e.remove();
            }
        }
        activeGhosts.clear();
    }
}
