package cm.ptks.craftflowers.commands;

import cm.ptks.craftflowers.CraftFlowers;
import cm.ptks.craftflowers.flower.FlowerPot;
import cm.ptks.craftflowers.gui.CraftFlowersGui;
import cm.ptks.craftflowers.gui.SavedFlowerListGui;
import cm.ptks.craftflowers.languages.I18n;
import cm.ptks.craftflowers.languages.Messages;
import cm.ptks.craftflowers.storage.SavedFlowerPot;
import cm.ptks.craftflowers.util.Text;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftFlowersCommand implements CommandExecutor, TabCompleter {

    private final CraftFlowers plugin;

    public CraftFlowersCommand(CraftFlowers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length > 0 && (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v"))) {
            Player player = commandSender instanceof Player ? (Player) commandSender : null;
            commandSender.sendMessage(Text.prefixed(Messages.getCommandVersion(player, plugin.getPluginMeta().getVersion())));
            commandSender.sendMessage(Text.prefixed(Messages.getCommandServerVersion(player, plugin.getServer().getName() + " " + plugin.getServer().getVersion())));
            return true;
        }

        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;

        if (args.length == 0) {
            if (!player.hasPermission("craftflowers.use")) {
                player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.NO_PERMISSION_USE)));
                return true;
            }
            CraftFlowersGui.openGui(player);
            return true;
        }


        if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
            player.sendMessage(Text.prefixed(Messages.getCommandVersion(player, plugin.getPluginMeta().getVersion())));

            player.sendMessage(Text.prefixed(Messages.getCommandAuthor(player, plugin.getPluginMeta().getAuthors().get(0))));
            player.sendMessage(Text.prefixed(Messages.getCommandWebsite(player, plugin.getPluginMeta().getWebsite())));
            return true;
        }

        if (args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("s")) {
            if (!player.hasPermission("craftflowers.save")) {
                player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.NO_PERMISSION_SAVE), NamedTextColor.RED));
                return true;
            }

            if (args.length != 1 && args.length <= 2) {
                FlowerPot flowerPot = FlowerPot.parsePot(player.getInventory().getItemInMainHand());

                if (player.getInventory().getItemInMainHand().getType().equals(Material.FLOWER_POT) && flowerPot != null) {
                    String name = args[1].replaceAll("[^A-Za-z0-9]", "");

                    plugin.getExecutorService().submit(() -> {
                        plugin.getFlowerStorage().saveFlower(name, player.getUniqueId(), flowerPot);
                        player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.SUCCESS_SAVE)));
                    });
                } else {
                    player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.NEED_HOLD_CF_FLOWERPOT), NamedTextColor.RED));
                }
                return true;
            }

            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.SAVE_SYNTAX), NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("l")) {
            if (!player.hasPermission("craftflowers.load")) {
                player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.NO_PERMISSION_LOAD), NamedTextColor.RED));
                return true;
            }

            if (args.length != 1 && args.length <= 2) {
                plugin.getExecutorService().submit(() -> {
                    SavedFlowerPot flower = plugin.getFlowerStorage().getFlower(args[1], player.getUniqueId());
                    if (flower == null) {
                        player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.LOAD_NOT_FOUND)));
                        return;
                    }
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.getInventory().addItem(flower.getFlowerPot().createItemStack());
                        player.sendMessage(Text.prefixed(Messages.getCommandSuccessLoad(player, args[1])));
                    });
                });
                return true;
            }

            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.LOAD_SYNTAX), NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("d")) {
            if (!player.hasPermission("craftflowers.delete")) {
                player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.NO_PERMISSION_DELETE), NamedTextColor.RED));
                return true;
            }

            if (args.length != 1 && args.length <= 2) {
                plugin.getExecutorService().submit(() -> {
                    if (!plugin.getFlowerStorage().deleteFlower(args[1], player.getUniqueId())) {
                        player.sendMessage(Text.prefixed(Messages.getCommandCantDelete(player, args[1])));
                        return;
                    }
                    player.sendMessage(Text.prefixed(Messages.getCommandSuccessDelete(player, args[1])));
                });
                return true;
            }

            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.DELETE_SYNTAX), NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("craftflowers.use")) {
                player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.NO_PERMISSION_LIST)));
                return true;
            }
            plugin.getExecutorService().submit(() -> {
                List<SavedFlowerPot> savedFlowers = plugin.getFlowerStorage().getSavedFlowers(player.getUniqueId());
                if (savedFlowers.size() == 0) {
                    player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.NO_SAVED_FLOWERS)));
                    return;
                }
                plugin.getServer().getScheduler().runTask(plugin, () -> SavedFlowerListGui.openGui(player, savedFlowers));
            });
            return true;
        }


        if (!args[0].equalsIgnoreCase("help") && !args[0].equalsIgnoreCase("h")) {
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.SYNTAX), NamedTextColor.GREEN));
        } else {
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.OPEN_GUI), NamedTextColor.GREEN));
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.SAVE), NamedTextColor.GREEN));
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.LOAD), NamedTextColor.GREEN));
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.DELETE), NamedTextColor.GREEN));
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.INFO), NamedTextColor.GREEN));
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.VERSION), NamedTextColor.GREEN));
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.LIST), NamedTextColor.GREEN));
            player.sendMessage(Text.prefixed(I18n.translate(player, Messages.COMMANDS.HELP_MESSAGES.HELP), NamedTextColor.GREEN));
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length > 1)
            return null;
        return Stream.of("help", "info", "version", "list", "load", "delete", "save").filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
    }
}