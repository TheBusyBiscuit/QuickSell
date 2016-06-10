package me.mrCookieSlime.QuickSell;

import me.mrCookieSlime.QuickSell.SellEvent.Type;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CitizensListener implements Listener {
	
	public CitizensListener(QuickSell plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onNPCInteract(NPCRightClickEvent e) {
		NPC npc = e.getNPC();
		if (QuickSell.getInstance().npcs.contains(String.valueOf(npc.getId()))) {
			String action = QuickSell.getInstance().npcs.getString(String.valueOf(npc.getId()));
			Shop shop = Shop.getShop(action.split(" ; ")[0]);
			if (shop == null) QuickSell.local.sendTranslation(e.getClicker(), "messages.unknown-shop", false);
			else {
				if (action.split(" ; ")[1].equalsIgnoreCase("SELL")) ShopMenu.open(e.getClicker(), shop);
				else if (shop.hasUnlocked(e.getClicker())) shop.sellall(e.getClicker(), "", Type.CITIZENS);
				else QuickSell.local.sendTranslation(e.getClicker(), "messages.no-access", false);
			}
		}
	}
	
	@EventHandler
	public void onDamage(NPCDamageByEntityEvent e) {
		NPC npc = e.getNPC();
		Entity damager = e.getDamager();
		if (damager instanceof Player && QuickSell.getInstance().npcs.contains(String.valueOf(npc.getId()))) {
			Player p = (Player) damager;
			String action = QuickSell.getInstance().npcs.getString(String.valueOf(npc.getId()));
			Shop shop = Shop.getShop(action.split(" ; ")[0]);
			if (shop == null) QuickSell.local.sendTranslation(p, "messages.unknown-shop", false);
			else shop.showPrices(p);
		}
	}
}
