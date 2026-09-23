package dev.ghostspear;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** /ghostspear give [player]  and  /ghostspear reload */
public final class GhostSpearCommand implements CommandExecutor, TabCompleter {

    private final GhostSpearPlugin plugin;

    public GhostSpearCommand(GhostSpearPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("/" + label + " give [player]  |  /" + label + " reload", NamedTextColor.GRAY));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> give(sender, label, args);
            case "reload" -> reload(sender);
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use give or reload.", NamedTextColor.RED));
        }
        return true;
    }

    private void give(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("ghostspear.give")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player '" + args[1] + "' isn't online.", NamedTextColor.RED));
                return;
            }
        } else if (sender instanceof Player self) {
            target = self;
        } else {
            sender.sendMessage(Component.text("From console use: /" + label + " give <player>", NamedTextColor.RED));
            return;
        }

        ItemStack spear = plugin.spearItem().create();
        Map<Integer, ItemStack> leftover = target.getInventory().addItem(spear);
        for (ItemStack drop : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), drop);
        }

        target.sendMessage(Component.text("You received a Ghost Spear.", NamedTextColor.AQUA));
        if (sender != target) {
            sender.sendMessage(Component.text("Gave a Ghost Spear to " + target.getName() + ".", NamedTextColor.GREEN));
        }
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("ghostspear.reload")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return;
        }
        plugin.reloadSettings();
        sender.sendMessage(Component.text("GhostSpear config reloaded. (Spears you already have keep their old name/lore.)", NamedTextColor.GREEN));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String typed = args[0].toLowerCase(Locale.ROOT);
            if (sender.hasPermission("ghostspear.give") && "give".startsWith(typed)) out.add("give");
            if (sender.hasPermission("ghostspear.reload") && "reload".startsWith(typed)) out.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("ghostspear.give")) {
            String typed = args[1].toLowerCase(Locale.ROOT);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(typed)) out.add(p.getName());
            }
        }
        return out;
    }
}
