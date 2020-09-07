package net.JaG.jBank;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;

public class BankCommand implements CommandExecutor {
	private Economy eco = Main.economy;
	
	public static boolean isDouble(String s) { 
		try { Double.parseDouble(s); } 
		catch(NumberFormatException e) { return false; }
		return true;
	}
	public static String doChatMsg(String str) { return ChatColor.translateAlternateColorCodes('&', str); }
	public static String doChatMsg(String str, String holder, String holderNew) {
		String chatString = str;
		if (chatString.contains(holder)) {
			chatString = chatString.replace(holder, holderNew);
		}
		
		return ChatColor.translateAlternateColorCodes('&', chatString);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (cmd.getName().equalsIgnoreCase("bank")) {
				Player p = (Player) sender;
				double pBal = (double) eco.getBalance(p);
				ConfigurationSection pBank = (ConfigurationSection) BankStorage.getBanks().getConfigurationSection("banks");
				double bankAmount = BankStorage.getBanks().getConfigurationSection("banks").getDouble(p.getUniqueId().toString());
				String bankAmountStr = String.format("%.2f", bankAmount);
				String prefix = ChatColor.translateAlternateColorCodes('&', Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("prefix"));
				if (args.length == 0) {
					String bal = doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("bankBalance"), "<bal>", bankAmountStr);
					p.sendMessage(prefix + bal);
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("d")) {
						String use = doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("depositSyntax"));
						p.sendMessage(prefix + use);
					} else if (args[0].equalsIgnoreCase("withdraw")|| args[0].equalsIgnoreCase("w")) {
						String use = doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("withdrawSyntax"));
						p.sendMessage(prefix + use);
					}  else if (args[0].equalsIgnoreCase("help")) {
						for (String key : Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getStringList("helpCmd")) {
							if (key.contains("<bal>") || key.contains("<rate>")) {
								key = doChatMsg(key, "<bal>", bankAmountStr);
								
								ArrayList<Double> ints = new ArrayList<Double>();
								for (String group : Main.getPlugin(Main.class).getConfig().getConfigurationSection("interest").getKeys(true)) {
									if (p.hasPermission("group." + group)) {
										ints.add(Main.getPlugin(Main.class).getConfig().getConfigurationSection("interest").getDouble(group));
									}
								}
								double max = (Collections.max(ints)*100);
								int currentRate = (int) max;
								p.sendMessage(doChatMsg(key, "<rate>", Integer.toString(currentRate)));
								
								
							} else {
								p.sendMessage(doChatMsg(key));
							}
						}
					} else if (args[0].equalsIgnoreCase("gui")) {
						GUI.openGUI(p);
						String msg = doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("openBank"));
						p.sendMessage(prefix + msg);
						
					} else {
						p.sendMessage(prefix + "Invalid arguments");
					}
				} else if (args.length == 2) {
					if (p.hasPermission("bank.use")) {
						if (args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("d")) {
							if (isDouble(args[1])) {
								double val = Double.parseDouble(args[1]);
								if (val <= pBal) {
									eco.withdrawPlayer(p, p.getWorld().getName(), val);
									pBank.set(p.getUniqueId().toString(), bankAmount + val);
									BankStorage.saveBanks();
									String msg = doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("depositMsg"), "<value>", Double.toString(val));
									p.sendMessage(prefix + msg);
								} else {
									p.sendMessage(prefix + doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("noFunds")));
								}
							} else {
								p.sendMessage(prefix + doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("badValue")));
							}
						} else if (args[0].equalsIgnoreCase("withdraw")|| args[0].equalsIgnoreCase("w")) {
							if (isDouble(args[1])) {
								double val = Double.parseDouble(args[1]);
								if ((val < bankAmount) || (val == bankAmount)) {
									eco.depositPlayer(p, p.getWorld().getName(), val);
									pBank.set(p.getUniqueId().toString(), bankAmount - val);
									BankStorage.saveBanks();
									String msg = doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("withdrawMsg"), "<value>", Double.toString(val));
									p.sendMessage(prefix + msg);
								} else {
									p.sendMessage(prefix + doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("noFunds")));
								}
							} else {
								p.sendMessage(prefix + doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("badValue")));
							}
						}
					} else { p.sendMessage(prefix + doChatMsg(Main.getPlugin(Main.class).getConfig().getConfigurationSection("chat").getString("noAccess"))); }
				}


			}
		}
		
		
		return false;
	}

}
