package net.JaG.jBank;

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

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (cmd.getName().equalsIgnoreCase("bank")) {
				Player p = (Player) sender;
				double pBal = (double) eco.getBalance(p);
				ConfigurationSection pBank = (ConfigurationSection) BankStorage.getBanks().getConfigurationSection("banks");
				double bankAmount = BankStorage.getBanks().getConfigurationSection("banks").getDouble(p.getUniqueId().toString());
				String bankAmountStr = String.format("%.2f", bankAmount);
				String prefix = ChatColor.translateAlternateColorCodes('&', Main.getPlugin(Main.class).getConfig().getConfigurationSection("").getString("prefix"));
				if (args.length == 0) {
					p.sendMessage(prefix + "Current bank balance: " + ChatColor.GREEN + "$" + bankAmountStr);
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("d")) {
						p.sendMessage(prefix + "Usage: /bank deposit <amount>");
					} else if (args[0].equalsIgnoreCase("withdraw")|| args[0].equalsIgnoreCase("w")) {
						p.sendMessage(prefix + "Usage: /bank withdraw <amount>");
					}  else if (args[0].equalsIgnoreCase("help")) {
						p.sendMessage(prefix + "Help Command");
						p.sendMessage(" ");
						p.sendMessage(ChatColor.YELLOW + "/bank (deposit, d) <amount>" + ChatColor.GRAY + " - Deposit money into bank account");
						p.sendMessage(ChatColor.YELLOW + "/bank (withdraw, w) <amount>" + ChatColor.GRAY + " - Withdraw money from bank account");
						p.sendMessage(" ");
						p.sendMessage(ChatColor.GRAY + "Current bank balance: " + ChatColor.GREEN + "$" + bankAmountStr);
					}else if (args[0].equalsIgnoreCase("gui")) {
						GUI.openGUI(p);
						p.sendMessage(prefix + "Opening bank...");
						
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
									p.sendMessage(prefix + "Deposited $" + val + " into your bank");
								} else {
									p.sendMessage(prefix + "Insufficient funds");
								}
							} else {
								p.sendMessage(prefix + "Can only deposit number values");
							}
						} else if (args[0].equalsIgnoreCase("withdraw")|| args[0].equalsIgnoreCase("w")) {
							if (isDouble(args[1])) {
								double val = Double.parseDouble(args[1]);
								if ((val < bankAmount) || (val == bankAmount)) {
									eco.depositPlayer(p, p.getWorld().getName(), val);
									pBank.set(p.getUniqueId().toString(), bankAmount - val);
									BankStorage.saveBanks();
									p.sendMessage(prefix + "Withdrew $" + val + " from your bank");
								} else {
									p.sendMessage(prefix + "Insufficient bank funds");
								}
							} else {
								p.sendMessage(prefix + "Can only deposit number values");
							}
						}
					} else { p.sendMessage(prefix + "You do not have bank access"); }
				}


			}
		}
		
		
		return false;
	}

}
