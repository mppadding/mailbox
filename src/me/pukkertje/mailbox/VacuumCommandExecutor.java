package me.pukkertje.mailbox;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

/**
 * Created by puk on 17-3-2015.
 */
public class VacuumCommandExecutor implements CommandExecutor {
    private final Mailbox mailbox;

    public VacuumCommandExecutor(Mailbox mailbox) {
        this.mailbox = mailbox;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("vacuum")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player!");
            } else {
                Player player = (Player) sender;
                for(Entity e : getNearbyEntities(player.getLocation(), 10)) {
                    if(e instanceof Item) {
                        Item item = (Item) e;
                        ItemStack is = item.getItemStack();
                        sender.sendMessage(is.toString());
                    }
                }
            }
            return true;
        }

        return false;
    }

    public static Entity[] getNearbyEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        HashSet <Entity> radiusEntities = new HashSet< Entity >();

        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
                int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
                for (Entity e: new Location(l.getWorld(), x + (chX * 16), y, z + (chZ * 16)).getChunk().getEntities()) {
                    if (e.getLocation().distance(l) <= radius && e.getLocation().getBlock() != l.getBlock())
                        radiusEntities.add(e);
                }
            }
        }

        return radiusEntities.toArray(new Entity[radiusEntities.size()]);
    }

}
