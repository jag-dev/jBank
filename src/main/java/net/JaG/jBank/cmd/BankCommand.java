package net.JaG.jBank.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.JaG.jBank.utils.BankStorage;
import net.JaG.jBank.utils.GUI;
import net.JaG.jBank.JBank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;

public class BankCommand implements CommandExecutor {
	private Economy eco = JBank.economy;
	private JBank m;
	public BankCommand(JBank main) { this.m = main; }
	
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
	
	public OfflinePlayer getBalSpot(int spot) {
		int topBalAmount = m.getConfig().getInt("topBalAmount");

		List<OfflinePlayer> topBalances = Arrays.stream(Bukkit.getOfflinePlayers()).sorted((p1, p2) -> 
			Double.compare(getBankBalance(p2.getUniqueId()), 
			getBankBalance(p1.getUniqueId())))
			.limit(topBalAmount)
			.collect(Collectors.toList());
		
		return topBalances.get(spot);
	}
	
	public double getBankBalance(UUID uuid) {
		return BankStorage.getBanks().getConfigurationSection("banks").getDouble(uuid.toString());
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
				String prefix = ChatColor.translateAlternateColorCodes('&', JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("prefix"));
				if (args.length == 0) {
					String bal = doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("bankBalance"), "<bal>", bankAmountStr);
					p.sendMessage(prefix + bal);
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("d")) {
						String use = doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("depositSyntax"));
						p.sendMessage(prefix + use);
					} else if (args[0].equalsIgnoreCase("withdraw")|| args[0].equalsIgnoreCase("w")) {
						String use = doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("withdrawSyntax"));
						p.sendMessage(prefix + use);
					}  else if (args[0].equalsIgnoreCase("help")) {
						for (String key : JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getStringList("helpCmd")) {
							if (key.contains("<bal>") || key.contains("<rate>")) {
								key = doChatMsg(key, "<bal>", bankAmountStr);
								
								ArrayList<Double> ints = new ArrayList<Double>();
								for (String group : JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("interest").getKeys(true)) {
									if (p.hasPermission("group." + group)) {
										ints.add(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("interest").getDouble(group));
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
						String msg = doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("openBank"));
						p.sendMessage(prefix + msg);
						
					} else if (args[0].equalsIgnoreCase("top")) {
						p.sendMessage(prefix + "Top Bank Balances");
						p.sendMessage(" ");
							for (int i = 0; i < m.getConfig().getInt("topBalAmount"); i++) {
								if (i >= BankStorage.getBanks().getConfigurationSection("banks").getKeys(false).size()) { continue; }
								OfflinePlayer place = getBalSpot(i);
								String topBalMsg = m.getConfig().getConfigurationSection("chat").getString("topBalFormat");
								if (topBalMsg.contains("<player>")) { topBalMsg = topBalMsg.replace("<player>", place.getName()); }
								if (topBalMsg.contains("<bal")) { topBalMsg = topBalMsg.replace("<bal>", Double.toString(getBankBalance(place.getUniqueId()))); }
								if (topBalMsg.contains("<place>")) { topBalMsg = topBalMsg.replace("<place>", Integer.toString(i+1)); }
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', topBalMsg));
							}
						p.sendMessage(" ");
					} else if (args[0].equalsIgnoreCase("reload") && p.hasPermission("bank.reload")) {
						m.reloadConfig();
						BankStorage.reloadBanks();
						p.sendMessage(prefix + "Configuration reloaded");
					} else {
						p.sendMessage(prefix + "Invalid arguments");
					}
				} else if (args.length == 2) {
					if (p.hasPermission("bank.use")) {
						if (args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("d")) {
							if (isDouble(args[1])) {
								double val = Double.parseDouble(args[1]);
								if (args[1].contains(".")) {
									String[] valCheck = args[1].split("\\.", 2);
									 if (valCheck[1].length() >= 3) {
										 p.sendMessage(prefix + "Only use two decimal places");
										 return false;
									 }
								}
								if (val <= pBal) {
									eco.withdrawPlayer(p, p.getWorld().getName(), val);
									pBank.set(p.getUniqueId().toString(), bankAmount + val);
									BankStorage.saveBanks();
									String msg = doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("depositMsg"), "<value>", Double.toString(val));
									p.sendMessage(prefix + msg);
								} else {
									p.sendMessage(prefix + doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("noFunds")));
								}
							} else {
								p.sendMessage(prefix + doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("badValue")));
							}
						} else if (args[0].equalsIgnoreCase("withdraw")|| args[0].equalsIgnoreCase("w")) {
							if (isDouble(args[1])) {
								double val = Double.parseDouble(args[1]);
								if (args[1].contains(".")) {
									String[] valCheck = args[1].split("\\.", 2);
									 if (valCheck[1].length() >= 3) {
										 p.sendMessage(prefix + "Only use two decimal places");
										 return false;
									 }
								}
								
								if ((val < bankAmount) || (val == bankAmount)) {
									eco.depositPlayer(p, p.getWorld().getName(), val);
									pBank.set(p.getUniqueId().toString(), bankAmount - val);
									BankStorage.saveBanks();
									String msg = doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("withdrawMsg"), "<value>", Double.toString(val));
									p.sendMessage(prefix + msg);
								} else {
									p.sendMessage(prefix + doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("noFunds")));
								}
							} else {
								p.sendMessage(prefix + doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("badValue")));
							}
						}
					} else { p.sendMessage(prefix + doChatMsg(JBank.getPlugin(JBank.class).getConfig().getConfigurationSection("chat").getString("noAccess"))); }
				}


			}
		}
		
		
		return false;
	}

}
