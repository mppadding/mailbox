package me.pukkertje.mailbox;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Created by puk on 17-3-2015.
 */
public class Mailbox extends JavaPlugin {

    FileConfiguration config;

    public final Logger log = Logger.getLogger("Minecraft");

    public Economy econ = null;
    public Permission perms = null;

    @Override
    public void onEnable() {
        this.getCommand("mailbox").setExecutor(new MailboxCommandExecutor(this));

        new LoginListener(this);

        config = getConfig();

        config.addDefault("economy.create", 0);
        config.addDefault("economy.send", 0);

        config.options().copyDefaults(true);

        saveConfig();
        reloadConfig();

        if (!setupEconomy() ) {
            log.info(String.format("[%s] - Fees disabled due to no vault dependency found!", getDescription().getName()));
        }
        setupPermissions();
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {

    }
}
