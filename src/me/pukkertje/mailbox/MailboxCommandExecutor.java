package me.pukkertje.mailbox;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by puk on 17-3-2015.
 */
public class MailboxCommandExecutor implements CommandExecutor {
    private final Mailbox mailbox;

    public MailboxCommandExecutor(Mailbox mailbox) {
        this.mailbox = mailbox;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("mailbox")) {
            FileConfiguration config = mailbox.getConfig();
            boolean perms = mailbox.perms != null;

            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player!");
            } else {
                Player player = (Player) sender;
                if(args.length == 0) {
                    player.sendMessage("/mailbox shows help");
                    player.sendMessage("/mailbox ? or /mailbox help shows help");
                    player.sendMessage("/mailbox fees shows fees for sending to and creating mailboxes");
                    player.sendMessage("/mailbox create <player> creates a mailbox for <player>, charges may apply");
                    player.sendMessage("/mailbox send <player> sends item in hand to <player>'s mailbox, charges may apply");
                }
                if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("fees")) {
                        if(perms && !mailbox.perms.has(player, "mailbox.fees")) {
                            player.sendMessage("You don't have permission to do that!");
                            return true;
                        }

                        int create = config.getInt("economy.create");
                        int send = config.getInt("economy.send");

                        player.sendMessage("Creation cost is " + create);
                        player.sendMessage("Send cost is " + send);

                    } else {
                        player.sendMessage("/mailbox shows help");
                        player.sendMessage("/mailbox ? or /mailbox help shows help");
                        player.sendMessage("/mailbox fees shows fees for sending to and creating mailboxes");
                        player.sendMessage("/mailbox create <player> creates a mailbox for <player>, charges may apply");
                        player.sendMessage("/mailbox send <player> sends item in hand to <player>'s mailbox, charges may apply");
                    }
                } else if(args.length == 2) {
                    boolean econ = mailbox.econ != null;
                    if(args[0].equalsIgnoreCase("send")) {
                        if(perms && !mailbox.perms.has(player, "mailbox.send")) {
                            player.sendMessage("You don't have permission to do that!");
                            return true;
                        }

                        int cost = config.getInt("economy.send");
                        if(cost != 0) {
                            if(econ && !mailbox.econ.has(player, cost)) {
                                player.sendMessage("You don't have enough money to do this, you need atleast " + cost);
                                return true;
                            }
                        }

                        String uuid = config.getString(args[1]);
                        if(config.contains(uuid)) {
                            World world = mailbox.getServer().getWorld(UUID.fromString(config.getString(uuid + ".world")));

                            double x = config.getDouble(uuid + ".x");
                            double y = config.getDouble(uuid + ".y");
                            double z = config.getDouble(uuid + ".z");

                            Location loc = new Location(world, x, y, z);

                            if (loc.getBlock().getType().equals(Material.CHEST)) {
                                ItemStack is = player.getItemInHand();

                                if(is.getType().equals(Material.AIR)) {
                                    sender.sendMessage("You can't send your hand to someone!");
                                    return true;
                                }

                                Chest chest = (Chest) loc.getBlock().getState();
                                HashMap<Integer, ItemStack> hm = chest.getInventory().addItem(is);

                                if(!hm.isEmpty()) {
                                    player.setItemInHand(hm.get(0));

                                    ItemStack ret = hm.get(0);
                                    int amount = ret.getAmount();
                                    String name = ret.getItemMeta().hasDisplayName() ? ret.getItemMeta().getDisplayName() :
                                            ret.getType().toString().replace("_", " ").toLowerCase();

                                    sender.sendMessage("Some items couldn't be send to player, because his mailbox is full. " +
                                            amount + " " + name + " hasn't been sent.");
                                } else {
                                    player.setItemInHand(new ItemStack(Material.AIR, 0));
                                    sender.sendMessage("Items sent to player!");
                                    mailbox.econ.withdrawPlayer(player, cost);
                                }

                                return true;

                            } else {
                                config.set(uuid, null);
                                mailbox.saveConfig();
                                mailbox.reloadConfig();
                                sender.sendMessage("This player's mailbox in invalid!");
                                return true;
                            }
                        } else {
                            sender.sendMessage("This player doesn't have a mailbox!");
                            return true;
                        }
                    } else if(args[0].equalsIgnoreCase("create")) {
                        if(perms && !mailbox.perms.has(player, "mailbox.create")) {
                            player.sendMessage("You don't have permission to do that!");
                            return true;
                        }

                        int cost = config.getInt("economy.create");
                        if(cost != 0) {
                            if(econ && !mailbox.econ.has(player, cost)) {
                                player.sendMessage("You don't have enough money to do this, you need atleast " + cost);
                                return true;
                            }
                        }

                        String uuid = config.getString(args[1]);

                        if(config.contains(uuid)) {
                            sender.sendMessage("This player already has an mailbox");
                        } else {
                            Set<Material> set = new HashSet<Material>();
                            set.add(Material.AIR);

                            Block b = player.getTargetBlock(set, 5);

                            if(b.getType().equals(Material.CHEST)) {
                                config.set(uuid + ".world", b.getWorld().getUID().toString());
                                config.set(uuid + ".x", b.getLocation().getX());
                                config.set(uuid + ".y", b.getLocation().getY());
                                config.set(uuid + ".z", b.getLocation().getZ());

                                sender.sendMessage("Mailbox created!");

                                mailbox.saveConfig();
                                mailbox.reloadConfig();

                                Player target = (Bukkit.getServer().getPlayer(args[1]));
                                if(target != null) {
                                    sender.sendMessage(player.getName() + " created a mailbox for you");
                                }

                                mailbox.econ.withdrawPlayer(player, cost);

                                return true;
                            } else {
                                sender.sendMessage("That isn't a chest!");

                                return true;
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

}
