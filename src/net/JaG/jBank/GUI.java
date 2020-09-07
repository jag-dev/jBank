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
	
	@SuppressWarnings("deprecation")
	public static void openGUI(Player p) {
		Inventory gui = Bukkit.getServer().createInventory(p, 9, "Bank Information");
		
		ItemStack empty = new ItemStack(Material.AIR);
		
		String b_rawItem = m.getConfig().getConfigurationSection("gui.balance").getString("id");
		if (!b_rawItem.contains(":")) { b_rawItem = b_rawItem + ":0"; }
		String[] b_itemData = b_rawItem.split(":", 2);
		ItemStack bal = new ItemStack(Material.getMaterial(Integer.parseInt(b_itemData[0])), 1, (byte) Integer.parseInt(b_itemData[1]));
		ItemMeta balMeta = bal.getItemMeta();
		balMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', m.getConfig().getConfigurationSection("gui.balance").getString("name")));
		ArrayList<String> bankLore = new ArrayList<String>();
		double bankAmount = BankStorage.getBanks().getConfigurationSection("banks").getDouble(p.getUniqueId().toString());
		for (String key : m.getConfig().getConfigurationSection("gui.balance").getStringList("lore")) {
			if (key.contains("<bal>")) { 
				String bankAmountStr = String.format("%.2f", bankAmount);
				key = key.replace("<bal>", bankAmountStr);
				key = ChatColor.translateAlternateColorCodes('&', key);
				bankLore.add(key);
				
			} else { 
				bankLore.add(ChatColor.translateAlternateColorCodes('&', key));
			}
		}
		balMeta.setLore(bankLore);
		bal.setItemMeta(balMeta);
		
		String i_rawItem = m.getConfig().getConfigurationSection("gui.interest").getString("id");
		if (!i_rawItem.contains(":")) { i_rawItem = i_rawItem + ":0"; }
		String[] i_itemData = i_rawItem.split(":", 2);
		ItemStack interest = new ItemStack(Material.getMaterial(Integer.parseInt(i_itemData[0])), 1, (byte) Integer.parseInt(i_itemData[1]));
		ItemMeta intMeta = interest.getItemMeta();
		intMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', m.getConfig().getConfigurationSection("gui.interest").getString("name")));
		intMeta.setLore(interestLore(p));
		interest.setItemMeta(intMeta);
		
		String d_rawItem = m.getConfig().getConfigurationSection("gui.deposit").getString("id");
		if (!d_rawItem.contains(":")) { d_rawItem = d_rawItem + ":0"; }
		String[] d_itemData = d_rawItem.split(":", 2);
		ItemStack depo = new ItemStack(Material.getMaterial(Integer.parseInt(d_itemData[0])), 1, (byte) Integer.parseInt(d_itemData[1]));
		ItemMeta depoMeta = depo.getItemMeta();
		depoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', m.getConfig().getConfigurationSection("gui.deposit").getString("name")));
		ArrayList<String> depoLore = new ArrayList<String>();
		for (String key : m.getConfig().getConfigurationSection("gui.deposit").getStringList("lore")) {
			depoLore.add(ChatColor.translateAlternateColorCodes('&', key));
		}
		depoMeta.setLore(depoLore);
		depo.setItemMeta(depoMeta);
		
		String w_rawItem = m.getConfig().getConfigurationSection("gui.withdraw").getString("id");
		if (!w_rawItem.contains(":")) { w_rawItem = w_rawItem + ":0"; }
		String[] w_itemData = w_rawItem.split(":", 2);
		ItemStack with = new ItemStack(Material.getMaterial(Integer.parseInt(w_itemData[0])), 1, (byte) Integer.parseInt(w_itemData[1]));
		ItemMeta withMeta = with.getItemMeta();
		withMeta.setDisplayName(ChatColor.RED + "Withdraw");
		ArrayList<String> withLore = new ArrayList<String>();
		for (String key : m.getConfig().getConfigurationSection("gui.withdraw").getStringList("lore")) {
			withLore.add(ChatColor.translateAlternateColorCodes('&', key));
		}
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
	
	public static ArrayList<String> interestLore(Player p) {
		ArrayList<Double> ints = new ArrayList<Double>();
		for (String group : m.getConfig().getConfigurationSection("interest").getKeys(true)) {
			if (p.hasPermission("group." + group)) {
				ints.add(m.getConfig().getConfigurationSection("interest").getDouble(group));
			}
		}
		double max = (Collections.max(ints)*100);
		int currentRate = (int) max;
		ArrayList<String> value = new ArrayList<String>();
		
		for (String key : m.getConfig().getConfigurationSection("gui.interest").getStringList("lore")) {
			if (key.contains("<rate>")) {
				String rateStr = String.format("%d%%", currentRate);
				key = key.replace("<rate>", rateStr);
				key = ChatColor.translateAlternateColorCodes('&', key);
				value.add(key);
			} else { value.add(ChatColor.translateAlternateColorCodes('&', key)); }
		}
		return value;
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
