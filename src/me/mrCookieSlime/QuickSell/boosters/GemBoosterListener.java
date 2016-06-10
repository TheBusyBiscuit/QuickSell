package me.mrCookieSlime.QuickSell.boosters;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.PrisonGems.GemPickupEvent;
import me.mrCookieSlime.QuickSell.QuickSell;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GemBoosterListener implements Listener {
	
	public GemBoosterListener(QuickSell plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPickup(GemPickupEvent e) {
		Player p = e.getPlayer();
		int gems = e.getAmount();
		
		for (Booster booster: Booster.getBoosters(p.getName())) {
			if (booster.getType().equals(BoosterType.PRISONGEMS)) {
				if (!booster.isSilent()) booster.sendMessage(p, new Variable("{GEMS}", String.valueOf((int)(gems * (booster.getMultiplier() - 1.0)))));
				gems = (int) (gems + gems * (booster.getMultiplier() - 1));
			}
		}
		
		e.setAmount(gems);
	}

}
