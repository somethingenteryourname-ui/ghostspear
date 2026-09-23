package dev.ghostspear;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks per-player dash cooldowns, boost windows and fall protection, in server ticks. */
public final class PlayerState {

    private final Map<UUID, Integer> lastDash = new HashMap<>();
    private final Map<UUID, Integer> boostUntil = new HashMap<>();
    private final Map<UUID, Integer> noFallUntil = new HashMap<>();
    private final Map<UUID, Integer> lastPierce = new HashMap<>();
    private final Map<UUID, Integer> lastBlockUse = new HashMap<>();

    /** Ticks left before this player can dash again (0 = ready). */
    public int dashCooldownLeft(UUID id, int now, int cooldown) {
        Integer last = lastDash.get(id);
        if (last == null) return 0;
        return Math.max(0, (last + cooldown) - now);
    }

    public void markDash(UUID id, int now, int boostWindow, int noFallTicks) {
        lastDash.put(id, now);
        boostUntil.put(id, now + boostWindow);
        noFallUntil.put(id, now + noFallTicks);
    }

    public boolean isBoosted(UUID id, int now) {
        Integer until = boostUntil.get(id);
        return until != null && now <= until;
    }

    public void consumeBoost(UUID id) {
        boostUntil.remove(id);
    }

    public boolean hasFallProtection(UUID id, int now) {
        Integer until = noFallUntil.get(id);
        return until != null && now <= until;
    }

    /** Small internal cooldown so one charge can't hit ten times in ten ticks. */
    public boolean canPierce(UUID id, int now) {
        Integer last = lastPierce.get(id);
        return last == null || now - last >= 10;
    }

    public void markPierce(UUID id, int now) {
        lastPierce.put(id, now);
    }

    public void markBlockUse(UUID id, int now) {
        lastBlockUse.put(id, now);
    }

    public boolean usedBlockThisTick(UUID id, int now) {
        Integer last = lastBlockUse.get(id);
        return last != null && now - last <= 1;
    }

    public void clear(UUID id) {
        lastDash.remove(id);
        boostUntil.remove(id);
        noFallUntil.remove(id);
        lastPierce.remove(id);
        lastBlockUse.remove(id);
    }
}
