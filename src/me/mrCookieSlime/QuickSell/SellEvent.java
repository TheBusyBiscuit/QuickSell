package me.mrCookieSlime.QuickSell;

import org.bukkit.entity.Player;

public interface SellEvent {
	
	public enum Type {
		
		SELL,
		SELLALL,
		AUTOSELL, 
		CITIZENS,
		UNKNOWN;
		
	}
	
	public void onSell(Player p, Type type, int itemsSold, double money);
}
