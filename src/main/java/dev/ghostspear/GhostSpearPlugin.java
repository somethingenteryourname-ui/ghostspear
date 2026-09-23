package dev.ghostspear;

import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class GhostSpearPlugin extends JavaPlugin {

    private NamespacedKey spearKey;
    private NamespacedKey ghostKey;
    private Settings settings;
    private SpearItem spearItem;
    private GhostSpawner ghostSpawner;
    private PlayerState playerState;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        spearKey = new NamespacedKey(this, "ghost_spear");
        ghostKey = new NamespacedKey(this, "ghost_afterimage");

        reloadSettings();

        playerState = new PlayerState();
        spearItem = new SpearItem(this);
        ghostSpawner = new GhostSpawner(this);

        getServer().getPluginManager().registerEvents(new SpearListener(this), this);
        new ChargeTask(this).runTaskTimer(this, 1L, 1L);

        GhostSpearCommand command = new GhostSpearCommand(this);
        PluginCommand pluginCommand = getCommand("ghostspear");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        getLogger().info("GhostSpear enabled. Spear material: " + settings.material);
    }

    @Override
    public void onDisable() {
        if (ghostSpawner != null) {
            ghostSpawner.removeAll();
        }
    }

    public void reloadSettings() {
        reloadConfig();
        settings = new Settings(getConfig(), getLogger());
    }

    public NamespacedKey spearKey() { return spearKey; }
    public NamespacedKey ghostKey() { return ghostKey; }
    public Settings settings() { return settings; }
    public SpearItem spearItem() { return spearItem; }
    public GhostSpawner ghostSpawner() { return ghostSpawner; }
    public PlayerState playerState() { return playerState; }
}
