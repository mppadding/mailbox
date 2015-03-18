package me.pukkertje.mailbox;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by puk on 18-3-2015.
 */
public final class LoginListener implements Listener {

    Mailbox plugin;

    public LoginListener(Mailbox plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        plugin.reloadConfig();

        Player p = event.getPlayer();
        String uuid = p.getUniqueId().toString();

        FileConfiguration config = plugin.getConfig();
        if(config.contains(p.getName())) {
            if(!config.getString(p.getName()).equalsIgnoreCase(uuid)) {
                config.set(p.getName(), uuid);
            }
        }
        for(String key : config.getKeys(false)) {
            String value = config.getString(key);
            if(value.equalsIgnoreCase(uuid)) {
                config.set(key, null);
                config.set(p.getName(), uuid);
            }
        }
        if(!config.contains(p.getName())) {
            config.set(p.getName(), uuid);
        }

        plugin.saveConfig();
        plugin.reloadConfig();
    }
}