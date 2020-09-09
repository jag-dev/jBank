package net.JaG.jBank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import org.apache.commons.lang.UnhandledException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class InterestTask extends BukkitRunnable {
	Main m;
	public InterestTask(Main main) { this.m = main; }
	private ArrayList<Double> ints = new ArrayList<Double>();

	@Override
	public void run() {
		ints.add(0.0);
		for (String key : BankStorage.getBanks().getConfigurationSection("banks").getKeys(true)) {
			if (Bukkit.getServer().getOnlinePlayers().contains(Bukkit.getServer().getPlayer(UUID.fromString(key)))) {
				Player p = Bukkit.getServer().getPlayer(UUID.fromString(key));
				for (String group : m.getConfig().getConfigurationSection("interest").getKeys(true)) {
					if (p.hasPermission("group." + group)) {
						ints.add(m.getConfig().getConfigurationSection("interest").getDouble(group));
					}
				}
				
				double preInt = BankStorage.getBanks().getConfigurationSection("banks").getDouble(key);
				try { 
					BankStorage.getBanks().getConfigurationSection("banks").set(key,
						(BankStorage.getBanks().getConfigurationSection("banks").getDouble(key)*(1+Collections.max(ints))));
				} catch (UnhandledException e) { System.out.println("[Warning] Unable to apply interest at this time"); }
				BankStorage.saveBanks();
				double interestN = (BankStorage.getBanks().getConfigurationSection("banks").getDouble(key)-preInt);
				String interestMsg = m.getConfig().getConfigurationSection("chat").getString("interestMsg");
				String interest = String.format("%.2f", interestN);
				interestMsg.replace("<interest>", interest);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', m.getConfig().getConfigurationSection("chat").getString("prefix")) + interestMsg);
				
				
			} else {
				OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(UUID.fromString(key));
				
				for (String group : m.getConfig().getConfigurationSection("interest").getKeys(true)) {
					if (m.perms.playerHas(null, p, "group." + group)) {
						ints.add(m.getConfig().getConfigurationSection("interest").getDouble(group));
					}
				}
				
				try { 
					BankStorage.getBanks().getConfigurationSection("banks").set(key,
						(BankStorage.getBanks().getConfigurationSection("banks").getDouble(key)*(1+Collections.max(ints))));
				} catch (UnhandledException e) { System.out.println("[Warning] Unable to apply interest at this time"); }
				BankStorage.saveBanks();
				
			}
		}
		
	}

}
