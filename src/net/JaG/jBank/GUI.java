package net.JaG.jBank;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class GUI implements Listener {
	static Main m;
	public GUI(Main main) {GUI.m = main; }
	
	public static void openGUI(Player p) {
		Inventory gui = Bukkit.getServer().createInventory(p, 9, "Bank Information");
		
		ItemStack empty = new ItemStack(Material.AIR);
		
		ItemStack bal = new ItemStack(Material.GOLD_INGOT);
		ItemMeta balMeta = bal.getItemMeta();
		balMeta.setDisplayName(ChatColor.GOLD + "Bank");
		ArrayList<String> bankLore = new ArrayList<String>();
		double bankAmount = BankStorage.getBanks().getConfigurationSection("banks").getDouble(p.getUniqueId().toString());
		String bankAmountStr = String.format(ChatColor.GRAY + "Balance:" + ChatColor.GREEN + " $%.2f", bankAmount);
		bankLore.add(bankAmountStr);
		balMeta.setLore(bankLore);
		bal.setItemMeta(balMeta);
		
		ItemStack interest = new ItemStack(Material.BOOK);
		ItemMeta intMeta = interest.getItemMeta();
		intMeta.setDisplayName(ChatColor.WHITE + "Interest");
		ArrayList<String> intLore = new ArrayList<String>();
		intLore.add(interestRate(p));
		intMeta.setLore(intLore);
		interest.setItemMeta(intMeta);
		
		ItemStack depo = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 13);
		ItemMeta depoMeta = depo.getItemMeta();
		depoMeta.setDisplayName(ChatColor.GREEN + "Deposit");
		ArrayList<String> depoLore = new ArrayList<String>();
		depoLore.add(ChatColor.GRAY + "Deposit full balance into");
		depoLore.add(ChatColor.GRAY + "account");
		depoMeta.setLore(depoLore);
		depo.setItemMeta(depoMeta);
		
		ItemStack with = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
		ItemMeta withMeta = with.getItemMeta();
		withMeta.setDisplayName(ChatColor.RED + "Withdraw");
		ArrayList<String> withLore = new ArrayList<String>();
		withLore.add(ChatColor.GRAY + "Withdraw full account into");
		withLore.add(ChatColor.GRAY + "balance");
		withMeta.setLore(withLore);
		with.setItemMeta(withMeta);
		
		gui.setItem(0, empty);
		gui.setItem(1, bal);
		gui.setItem(2, empty);
		gui.setItem(3, interest);
		gui.setItem(4, empty);
		gui.setItem(5, empty);
		gui.setItem(6, depo);
		gui.setItem(7, with);
		gui.setItem(8, empty);
		
		
		p.openInventory(gui);
	}
	
	public static String interestRate(Player p) {
		ArrayList<Double> ints = new ArrayList<Double>();
		for (String group : m.getConfig().getConfigurationSection("interest").getKeys(true)) {
			if (p.hasPermission("group." + group)) {
				ints.add(m.getConfig().getConfigurationSection("interest").getDouble(group));
			}
		}
		double max = (Collections.max(ints)*100);
		String intLore = String.format(ChatColor.GRAY + "Current Rate:" + ChatColor.YELLOW + " %d%%", (int)max);
		return intLore;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!e.getInventory().getTitle().equalsIgnoreCase("Bank Information")) return;
		final ItemStack clicked = e.getCurrentItem();
		if (clicked == null || clicked.getType() == Material.AIR) { return; }
		if (clicked.getType() == Material.STAINED_GLASS_PANE && clicked.getItemMeta().hasLore()) {
			Player p = (Player) e.getWhoClicked();
			if (clicked.getItemMeta().getDisplayName().contains("Deposit")) {
				p.performCommand("bank d " + Main.economy.getBalance(p));
				p.closeInventory();
			} else if (clicked.getItemMeta().getDisplayName().contains("Withdraw")) {
				double bankAmount = BankStorage.getBanks().getConfigurationSection("banks").getDouble(p.getUniqueId().toString());
				bankAmount = (double) Math.round(bankAmount * 100) / 100;
				p.performCommand("bank w " + bankAmount);
				p.closeInventory();
			}
			
		}
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onDrag(InventoryDragEvent e) {
		if (e.getInventory().getTitle().equalsIgnoreCase("Bank Information")) e.setCancelled(true);
	}
}
