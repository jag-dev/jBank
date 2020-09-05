package net.JaG.jBank;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BankStorage {
	private static File file;
	private static FileConfiguration banks;
	private Main m = Main.getPlugin(Main.class);
	
	public void setup() {
		if (!m.getDataFolder().exists()) { m.getDataFolder().mkdir(); }
		file = new File(m.getDataFolder(), "banks.yml");
		
		if (!file.exists()) {
			try {
				file.createNewFile();
				System.out.println("[jBank] Creating banks.yml");
			} catch (IOException e) { System.out.println("[jBanks] Cannot create banks.yml"); }
		}
		
		banks = YamlConfiguration.loadConfiguration(file);
		if (banks.getConfigurationSection("banks") == null) { banks.createSection("banks"); }
	}
	
	public static FileConfiguration getBanks() { return banks; }
	public static void saveBanks() {
		try {
			banks.save(file);
			System.out.println("[jBank] Saved banks.yml");
		} catch (IOException e) { System.out.println("[jBanks] Could not save banks.yml"); }
	}
	public static void reloadBanks() {
		banks = YamlConfiguration.loadConfiguration(file);
		System.out.println("[jBank] Reloaded banks.yml");
	}
}
