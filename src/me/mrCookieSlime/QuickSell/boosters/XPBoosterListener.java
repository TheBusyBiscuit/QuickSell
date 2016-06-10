package me.mrCookieSlime.QuickSell.boosters;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.QuickSell.QuickSell;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class XPBoosterListener implements Listener {
	
	public XPBoosterListener(QuickSell plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onXPGain(PlayerExpChangeEvent e) {
		Player p = e.getPlayer();
		int xp = e.getAmount();
		for (Booster booster: Booster.getBoosters(p.getName())) {
			if (booster.getType().equals(BoosterType.EXP)) {
				if (!booster.isSilent()) booster.sendMessage(p, new Variable("{XP}", String.valueOf((float)(xp * (booster.getMultiplier() - 1.0)))));
				xp = (int) (xp + xp * (booster.getMultiplier() - 1));
			}
		}
		
		e.setAmount(xp);
	}

}
