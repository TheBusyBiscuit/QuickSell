package me.mrCookieSlime.QuickSell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PriceInfo {
	
	Shop shop;
	Map<String, Double> prices;
	Map<String, ItemStack> info;
	List<String> order;
	int amount;
	
	private static final Map<String, PriceInfo> map = new HashMap<String, PriceInfo>();
	
	public PriceInfo(Shop shop) {
		this.shop = shop;
		this.prices = new HashMap<String, Double>();
		this.order = new ArrayList<String>();
		this.amount = QuickSell.cfg.getInt("shops." + shop.getID() + ".amount");
		
		for (String key: QuickSell.cfg.getConfiguration().getConfigurationSection("shops." + shop.getID() + ".price").getKeys(false)) {
			if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + shop.getID() + ".price." + key) > 0.0) prices.put(key, QuickSell.cfg.getDouble("shops." + shop.getID() + ".price." + key) / amount);
		}
		
		for (String parent: QuickSell.cfg.getStringList("shops." + shop.getID() + ".inheritance")) {
			loadParent(parent);
		}
		
		info = new HashMap<String, ItemStack>();
		for (String item: prices.keySet()) {
			if (info.size() >= 54) break;
			if (Material.getMaterial(item) != null) {
				info.put(item, new CustomItem(Material.getMaterial(item), "&r" + StringUtils.formatItemName(new ItemStack(Material.getMaterial(item)), false), "", "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)), "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)));
				order.add(item);
			}
			else if (item.split("-").length > 1) {
				if (Material.getMaterial(item.split("-")[0]) != null) {
					if (!item.split("-")[1].equals("nodata")) {
						info.put(item, new CustomItem(new CustomItem(Material.getMaterial(item.split("-")[0]), item.split("-")[1], "", "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)), "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)), getAmount()));
						order.add(item);
					}
					else {
						info.put(item, new CustomItem(Material.getMaterial(item.split("-")[0]), "&r" + StringUtils.formatItemName(new ItemStack(Material.getMaterial(item.split("-")[0])), false), "", "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)), "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)));
						order.add(item);
					}
				}
				else System.err.println("[QuickSell] Could not recognize Item String: \"" + item + "\"");
			}
			else System.err.println("[QuickSell] Could not recognize Item String: \"" + item + "\"");
		}
		
		map.put(shop.getID(), this);
	}
	
	private void loadParent(String parent) {
		for (String key: QuickSell.cfg.getKeys("shops." + parent + ".price")) {
			if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + parent + ".price." + key) > 0.0) prices.put(key, QuickSell.cfg.getDouble("shops." + parent + ".price." + key) / amount);
		}
		for (String p: QuickSell.cfg.getStringList("shops." + parent + ".inheritance")) {
			loadParent(p);
		}
	}

	public PriceInfo(String shop) {
		this.prices = new HashMap<String, Double>();
		
		for (String key: QuickSell.cfg.getConfiguration().getConfigurationSection("shops." + shop + ".price").getKeys(false)) {
			if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + shop + ".price." + key) > 0.0) prices.put(key, QuickSell.cfg.getDouble("shops." + shop + ".price." + key) / amount);
		}
		
		for (String parent: QuickSell.cfg.getStringList("shops." + shop + ".inheritance")) {
			for (String key: getInfo(parent).getPrices().keySet()) {
				if (!prices.containsKey(key) && QuickSell.cfg.getDouble("shops." + parent + ".price." + key) > 0.0) prices.put(key, QuickSell.cfg.getDouble("shops." + parent + ".price." + key) / amount);
			}
		}
	}
	
	public Map<String, Double> getPrices() {
		return prices;
	}
	
	public double getPrice(ItemStack item) {
		if (item == null) return 0.0;
		String string = toString(item);
		
		if (prices.containsKey(string)) return DoubleHandler.fixDouble(prices.get(string) * item.getAmount());
		else return 0.0D;
	}

	public double getPrice(String string) {
		return prices.get(string);
	}
	
	public String toString(ItemStack item) {
		if (item == null) return "null";
		String name = item.hasItemMeta() ? item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName().replace("&", "&"): "": "";
		if (!name.equalsIgnoreCase("") && prices.containsKey(item.getType().toString() + "-" + name))  {
			return item.getType().toString() + "-" + name;
		}
		else if (item.isSimilar(new ItemStack(item.getType(), item.getAmount())) && prices.containsKey(item.getType().toString() + "-nodata"))  {
			return item.getType().toString() + "-nodata";
		}
		else if (prices.containsKey(item.getType().toString()))  {
			return item.getType().toString();
		}
		return "null";
	}
	
	public static PriceInfo getInfo(String shop) {
		return map.containsKey(shop) ? map.get(shop): new PriceInfo(shop);
	}
	
	public int getAmount() {
		return amount;
	}
	
	public Collection<ItemStack> getInfo() {
		return info.values();
	}
	
	public List<String> getItems() {
		return this.order;
	}

	public ItemStack getItem(String string) {
		return this.info.get(string);
	}
}
