package net.JaG.jBank;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BankListener implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if ( BankStorage.getBanks().getConfigurationSection("banks").get(e.getPlayer().getUniqueId().toString()) == null) {
			BankStorage.getBanks().getConfigurationSection("banks").set(e.getPlayer().getUniqueId().toString(), 0.0);
			BankStorage.saveBanks();
		}
		
	}
}
