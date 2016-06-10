package me.mrCookieSlime.QuickSell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.InvUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Player.PlayerInventory;
import me.mrCookieSlime.PrisonUtils.Backpacks;
import me.mrCookieSlime.QuickSell.SellEvent.Type;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class Shop {
	
	private static final List<Shop> shops = new ArrayList<Shop>();
	private static Map<String, Shop> map = new HashMap<String, Shop>();
	
	String shop, permission;
    PriceInfo prices;
    ItemStack unlocked, locked;
    String name;
	
	@SuppressWarnings("deprecation")
	public Shop(String id) {
		this.shop = id;
		this.prices = new PriceInfo(this);
		
		name = QuickSell.cfg.getString("shops." + shop + ".name");
		permission = QuickSell.cfg.getString("shops." + shop + ".permission");
		
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7<&a&l Click to open &7>"));
		for (String line: QuickSell.cfg.getStringList("shops." + shop + ".lore")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		
		MaterialData md = null;
		if (QuickSell.cfg.getString("shops." + shop + ".itemtype").contains("-")) md = new MaterialData(Material.getMaterial(QuickSell.cfg.getString("shops." + shop + ".itemtype").split("-")[0]), (byte) Integer.parseInt(QuickSell.cfg.getString("shops." + shop + ".itemtype").split("-")[1])); 
		else md = new MaterialData(Material.getMaterial(QuickSell.cfg.getString("shops." + shop + ".itemtype")));
		unlocked = new CustomItem(md, name, lore.toArray(new String[lore.size()]));
		
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', QuickSell.local.getTranslation("messages.no-access").get(0)));
		for (String line: QuickSell.cfg.getStringList("shops." + shop + ".lore")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		
		MaterialData md2 = null;
		if (QuickSell.cfg.getString("options.locked-item").contains("-")) md2 = new MaterialData(Material.getMaterial(QuickSell.cfg.getString("options.locked-item").split("-")[0]), (byte) Integer.parseInt(QuickSell.cfg.getString("options.locked-item").split("-")[1])); 
		else md2 = new MaterialData(Material.getMaterial(QuickSell.cfg.getString("options.locked-item")));
		locked = new CustomItem(md2, name, lore.toArray(new String[lore.size()]));
		
		shops.add(this);
		map.put(this.shop.toLowerCase(), this);
	}
	
	public Shop() {
		shops.add(null);
	}
	
	public boolean hasUnlocked(Player p) {
		return permission.equalsIgnoreCase("") ? true: p.hasPermission(permission);
	}
	
	public static void reset() {
		shops.clear();
		map.clear();
	}
	
	public static List<Shop> list() {
		return shops;
	}
	
	public static Shop getHighestShop(Player p) {
		for (int i = shops.size() - 1; i >= 0; i--) {
			if (shops.get(i) != null && shops.get(i).hasUnlocked(p)) return shops.get(i);
		}
		return null;
	}

	public String getID() {
		return shop;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public static Shop getShop(String id) {
		return map.get(id.toLowerCase());
	}
	
	public PriceInfo getPrices() {
		return prices;
	}
	
	public void sellall(Player p, String item) {
		sellall(p, item, Type.UNKNOWN);
	}
	
	public void sellall(Player p, String item, Type type) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (int slot = 0; slot < p.getInventory().getSize(); slot++) {
			ItemStack is = p.getInventory().getItem(slot);
			if (QuickSell.getInstance().isPrisonUtilsInstalled() && Backpacks.isBackPack(is)) {
				Inventory backpack = Backpacks.getInventory(is);
				for (ItemStack itemstack: backpack.getContents()) {
					if (getPrices().getPrice(itemstack) > 0.0) {
						items.add(itemstack);
						backpack.removeItem(itemstack);
					}
				}
				Backpacks.saveBackpack(backpack, is);
			}
			else if (getPrices().getPrice(is) > 0.0) {
				items.add(is);
				p.getInventory().setItem(slot, null);
			}
		}
		PlayerInventory.update(p);
		sell(p, false, type, items.toArray(new ItemStack[items.size()]));
	}

	public void sell(Player p, boolean silent, ItemStack... soldItems) {
		sell(p, silent, Type.UNKNOWN, soldItems);
	}
	
	public void sell(Player p, boolean silent, Type type, ItemStack... soldItems) {
		if (soldItems.length == 0) {
			if (!silent) QuickSell.local.sendTranslation(p, "messages.no-items", false);
		}
		else {
			double money = 0.0;
			int sold = 0;
			int total = 0;
			for (ItemStack item: soldItems) {
				if (item != null) {
					total = total + item.getAmount();
					if (getPrices().getPrice(item) > 0.0) {
						sold = sold + item.getAmount();
						money = money + getPrices().getPrice(item);
					}
					else if (InvUtils.fits(p.getInventory(), item)) p.getInventory().addItem(item);
					else p.getWorld().dropItemNaturally(p.getLocation(), item);
				}
			}
			
			money = DoubleHandler.fixDouble(money, 2);
			
			if (money > 0.0) {
				double totalmoney = handoutReward(p, money, sold, silent);
				if (!silent) {
					if (QuickSell.cfg.getBoolean("sound.enabled")) p.playSound(p.getLocation(), Sound.valueOf(QuickSell.cfg.getString("sound.sound")), 1F, 1F);
					for (String command: QuickSell.cfg.getStringList("commands-on-sell")) {
						String cmd = command;
						if (cmd.contains("{PLAYER}")) cmd = cmd.replace("{PLAYER}", p.getName());
						if (cmd.contains("{MONEY}")) cmd = cmd.replace("{MONEY}", String.valueOf(DoubleHandler.fixDouble(totalmoney, 2)));
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
					}
				}
				for (SellEvent event: QuickSell.getSellEvents()) {
					event.onSell(p, type, total, totalmoney);
				}
			}
			else if (!silent && total > 0) QuickSell.local.sendTranslation(p, "messages.get-nothing", false);
			if (!silent && sold < total && total > 0) QuickSell.local.sendTranslation(p, "messages.dropped", false);
		}
		PlayerInventory.update(p);
	}
	
	public double handoutReward(Player p, double totalmoney, int items, boolean silent) {
		double money = totalmoney;
		if (!silent) QuickSell.local.sendTranslation(p, "messages.sell", false, new Variable("{MONEY}", DoubleHandler.getFancyDouble(money)), new Variable("{ITEMS}", String.valueOf(items)));
		for (Booster booster: Booster.getBoosters(p.getName())) {
			if (booster.getType().equals(BoosterType.MONETARY)) {
				if (!silent) booster.sendMessage(p, new Variable("{MONEY}", DoubleHandler.getFancyDouble(money * (booster.getMultiplier() - 1))));
				money = money + money * (booster.getMultiplier() - 1);
			}
		}
		if (!silent && !Booster.getBoosters(p.getName()).isEmpty()) QuickSell.local.sendTranslation(p, "messages.total", false, new Variable("{MONEY}", DoubleHandler.getFancyDouble(money)));
		money = DoubleHandler.fixDouble(money, 2);
		QuickSell.economy.depositPlayer(p, money);
		return money;
	}
	
	public ItemStack getItem(ShopStatus status) {
		switch(status) {
		case LOCKED: return locked;
		case UNLOCKED: return unlocked;
		default: return null;
		}
	}

	public String getName() {
		return ChatColor.translateAlternateColorCodes('&', name);
	}

	public void showPrices(Player p) {
		ShopMenu.openPrices(p, this, 1);
	}
	
}
