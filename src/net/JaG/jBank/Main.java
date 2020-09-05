package net.JaG.jBank;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin {
	public static Economy economy;
	public Permission perms;
	private BankStorage b;
	InterestTask it = new InterestTask(this);
	int interestTime = (((getConfig().getInt("hoursToInterest")*20)*60)*60);
	public LuckPerms api;
	
	public void onEnable() {
		if (!setupEconomy()) { System.out.println("[WARNING] jBank did NOT find an economy hook"); }
		else { System.out.println("[jBank] Hooked into economy"); }
		if (!setupLP()) { System.out.println("[WARNING] jBank did not hook into LuckPerms"); }
		else { System.out.println("[jBank] Hooked into LuckPerms"); }
		if (!setupPermissions()) { System.out.println("[jBank] Registered permissions"); }
		else { System.out.println("[WARNING] Could not load permissions"); }
		Bukkit.getServer().getPluginManager().registerEvents(new BankListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new GUI(this), this);
		getCommand("bank").setExecutor(new BankCommand());
		loadBanks();
		BankStorage.saveBanks();
		configDefaults();
		this.saveDefaultConfig();
		it.runTaskTimerAsynchronously(this, interestTime, interestTime);
		
	}
	public void onDisable() { BankStorage.saveBanks(); }
	public void loadBanks() {
		b = new BankStorage();
		b.setup();
	}
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
    
    private boolean setupLP() {
    	RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    	if (provider != null) {
    	    api = provider.getProvider();
    	    return true;
    	}
    	return false;
    }
    
    public void configDefaults() {
    	this.getConfig().addDefault("prefix", "&6Bank &8>&7 ");
    	this.getConfig().addDefault("hoursToInterest", 3);
    	this.getConfig().addDefault("interest.default", 0.10);
    	
    	this.getConfig().options().copyDefaults(true);
    }

}
