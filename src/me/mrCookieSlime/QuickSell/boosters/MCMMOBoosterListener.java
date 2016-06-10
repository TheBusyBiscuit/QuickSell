package me.mrCookieSlime.QuickSell.boosters;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.QuickSell.QuickSell;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

public class MCMMOBoosterListener implements Listener {
	
	public MCMMOBoosterListener(QuickSell plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onXPGain(McMMOPlayerXpGainEvent e) {
		Player p = e.getPlayer();
		float xp = e.getRawXpGained();
		for (Booster booster: Booster.getBoosters(p.getName())) {
			if (booster.getType().equals(BoosterType.EXP)) {
				if (!booster.isSilent()) booster.sendMessage(p, new Variable("{XP}", String.valueOf((float)(xp * (booster.getMultiplier() - 1.0)))));
				xp = (float) (xp + xp * (booster.getMultiplier() - 1));
			}
		}
		
		e.setRawXpGained(xp);
	}

}
