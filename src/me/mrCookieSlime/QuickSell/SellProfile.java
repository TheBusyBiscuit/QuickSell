package me.mrCookieSlime.QuickSell;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.QuickSell.SellEvent.Type;

import org.bukkit.entity.Player;

public class SellProfile {
	
	public static Map<UUID, SellProfile> profiles = new HashMap<UUID, SellProfile>();
	
	UUID uuid;
	Config cfg;
	List<String> transactions;
	
	public SellProfile(Player p) {
		uuid = p.getUniqueId();
		transactions = new ArrayList<String>();
		cfg = new Config(new File("data-storage/QuickSell/transactions/" + p.getUniqueId() + ".log"));
		profiles.put(uuid, this);
		
		if (QuickSell.cfg.getBoolean("shop.enable-logging")) {
			for (String transaction: cfg.getKeys()) {
				transactions.add(cfg.getString(transaction));
			}
		}
	}
	
	public static SellProfile getProfile(Player p) {
		return profiles.containsKey(p.getUniqueId()) ? profiles.get(p.getUniqueId()): new SellProfile(p);
	}
	
	public void unregister() {
		save();
		profiles.remove(uuid);
	}

	public void save() {
		cfg.save();
	}
	
	public void storeTransaction(Type type, int soldItems, double money) {
		long timestamp = System.currentTimeMillis();
		String string = String.valueOf(timestamp) + " __ " + type.toString() + " __ " + String.valueOf(soldItems) + " __ " + String.valueOf(money);
		cfg.setValue(String.valueOf(timestamp), string);
		transactions.add(string);
	}
	
	public List<String> getTransactions() {
		return transactions;
	}

	public Transaction getRecentTransactions(int amount) {
		int items = 0;
		double money = 0;
		for (int i = (transactions.size() - amount); i < transactions.size(); i++) {
			items = items + Transaction.getItemsSold(transactions.get(i));
			money = money + Transaction.getMoney(transactions.get(i));
		}
		return new Transaction(System.currentTimeMillis(), Type.UNKNOWN, items, money);
	}

}
