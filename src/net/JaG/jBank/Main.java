package net.JaG.jBank;

import java.util.ArrayList;

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
    	ArrayList<String> defaultBalLore = new ArrayList<String>();
    	defaultBalLore.add("&7Balance: &a$<bal>");
    	ArrayList<String> defaultIntLore = new ArrayList<String>();
    	defaultIntLore.add("&7Current Rate: &f<rate>");
    	ArrayList<String> defaultDepoLore = new ArrayList<String>();
    	defaultDepoLore.add("&7Deposit full balance into");
    	defaultDepoLore.add("&7account");
    	ArrayList<String> defaultWithLore = new ArrayList<String>();
    	defaultWithLore.add("&7Withdraw full bank value into");
    	defaultWithLore.add("&7account");
    	ArrayList<String> defaultHelpCmd = new ArrayList<String>();
    	defaultHelpCmd.add("&6Bank &8>&7 &a[Bal: $<bal>]&f [Interest: <rate>%]");
    	defaultHelpCmd.add(" ");
    	defaultHelpCmd.add("&e/bank (deposit, d) <amount>&7 - Put money into bank");
    	defaultHelpCmd.add("&e/bank (withdraw, w) <amount>&7 - Take money from bank");
    	
    	this.getConfig().addDefault("hoursToInterest", 3);
    	this.getConfig().addDefault("interest.default", 0.10);
    	
    	this.getConfig().addDefault("chat.prefix", "&6Bank &8>&7 ");
    	this.getConfig().addDefault("chat.bankBalance", "You have &a$<bal>&7 in your bank account");
    	this.getConfig().addDefault("chat.depositSyntax", "Usage: /bank (deposit, d) <amount>");
    	this.getConfig().addDefault("chat.withdrawSyntax", "Usage: /bank (withdraw, w) <amount>");
    	this.getConfig().addDefault("chat.openBank", "Opening bank...");
    	this.getConfig().addDefault("chat.depositMsg", "Deposited &a$<value>&7 into bank account");
    	this.getConfig().addDefault("chat.withdrawMsg", "Withdrew &a$<value>&7 from bank account");
    	this.getConfig().addDefault("chat.interestMsg", "&7You gained &a$<interest>&7 as bank interest");
    	this.getConfig().addDefault("chat.badValue", "Can only deposit number values");
    	this.getConfig().addDefault("chat.noFunds", "Insufficient funds");
    	this.getConfig().addDefault("chat.noAccess", "You do not have bank access");
    	this.getConfig().addDefault("chat.helpCmd", defaultHelpCmd);
    	
    	this.getConfig().addDefault("gui.balance.name", "&6Bank Balance");
    	this.getConfig().addDefault("gui.balance.lore", defaultBalLore);
    	this.getConfig().addDefault("gui.interest.name", "&fInterest Rate");
    	this.getConfig().addDefault("gui.interest.id", "340");
    	this.getConfig().addDefault("gui.interest.lore", defaultIntLore);
    	this.getConfig().addDefault("gui.deposit.name", "&aDeposit");
    	this.getConfig().addDefault("gui.deposit.id", "160:13");
    	this.getConfig().addDefault("gui.deposit.lore", defaultDepoLore);
    	this.getConfig().addDefault("gui.withdraw.name", "&cWithdraw");
    	this.getConfig().addDefault("gui.withdraw.id", "160:14");
    	this.getConfig().addDefault("gui.withdraw.lore", defaultWithLore);
    	
    	this.getConfig().options().copyDefaults(true);
    }

}
