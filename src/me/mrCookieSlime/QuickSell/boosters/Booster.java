package me.mrCookieSlime.QuickSell.boosters;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Clock;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.QuickSell.QuickSell;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Booster {
	
	public static List<Booster> active = new ArrayList<Booster>();
	
	BoosterType type;
	int id;
	int minutes;
	public String owner;
	double multiplier;
	Date timeout;
	Config cfg;
	boolean silent, infinite;
	Map<String, Integer> contributors = new HashMap<String, Integer>();
	
	public Booster(double multiplier, boolean silent, boolean infinite) {
		this(BoosterType.MONETARY, multiplier, silent, infinite);
	}
	
	public Booster(BoosterType type, double multiplier, boolean silent, boolean infinite) {
		this.type = type;
		this.multiplier = multiplier;
		this.silent = silent;
		this.infinite = infinite;
		if (infinite) {
			this.minutes = Integer.MAX_VALUE;
			this.timeout = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);
		}
		this.owner = "INTERNAL";
		
		active.add(this);
	}
	
	public Booster(String owner, double multiplier, int minutes) {
		this(BoosterType.MONETARY, owner, multiplier, minutes);
	}
	
	public Booster(BoosterType type, String owner, double multiplier, int minutes) {
		this.type = type;
		this.minutes = minutes;
		this.multiplier = multiplier;
		this.owner = owner;
		this.timeout = new Date(System.currentTimeMillis() + minutes * 60 * 1000);
		this.silent = false;
		this.infinite = false;
		
		contributors.put(owner, minutes);
	}
	
	public Booster(int id) throws ParseException {
		active.add(this);
		this.id = id;
		this.cfg = new Config(new File("data-storage/QuickSell/boosters/" + id + ".booster"));
		if (cfg.contains("type")) this.type = BoosterType.valueOf(cfg.getString("type"));
		else {
			cfg.setValue("type", BoosterType.MONETARY.toString());
			cfg.save();
			this.type = BoosterType.MONETARY;
		}
		
		this.minutes = cfg.getInt("minutes");
		this.multiplier = (Double) cfg.getValue("multiplier");
		this.owner = cfg.getString("owner");
		this.timeout = new SimpleDateFormat("yyyy-MM-dd-HH-mm").parse(cfg.getString("timeout"));
		this.silent= false;
		this.infinite = false;
		
		if (cfg.contains("contributors." + owner)) {
			for (String key: cfg.getKeys("contributors")) {
				contributors.put(key, cfg.getInt("contributors." + key));
			}
		}
		else {
			contributors.put(owner, minutes);
			writeContributors();
		}
	}
	
	private void writeContributors() {
		for (Map.Entry<String, Integer> entry: contributors.entrySet()) {
			cfg.setValue("contributors." + entry.getKey(), entry.getValue());
		}
		
		cfg.save();
	}

	@SuppressWarnings("deprecation")
	public void activate() {
		if (QuickSell.cfg.getBoolean("boosters.extension-mode")) {
			for (Booster booster: active) {
				if (booster.getType().equals(this.type) && Double.compare(booster.getMultiplier(), getMultiplier()) == 0) {
					if ((this instanceof PrivateBooster && booster instanceof PrivateBooster) || (!(this instanceof PrivateBooster) && !(booster instanceof PrivateBooster))) {
						booster.extend(this);
						if (!silent) {
							if (this instanceof PrivateBooster && Bukkit.getPlayer(getOwner()) != null) QuickSell.local.sendTranslation(Bukkit.getPlayer(getOwner()), "pbooster.extended." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
							else {
								for (String message: QuickSell.local.getTranslation("booster.extended." + type.toString())) {
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier()))));
								}
							}
						}
						return;
					}
				}
			}
		}
		
		if (!infinite) {
			for (int i = 0; i < 1000; i++) {
				if (!new File("data-storage/QuickSell/boosters/" + i + ".booster").exists()) {
					this.id = i;
					break;
				}
			}
			this.cfg = new Config(new File("data-storage/QuickSell/boosters/" + id + ".booster"));
			cfg.setValue("type", type.toString());
			cfg.setValue("owner", getOwner());
			cfg.setValue("multiplier", multiplier);
			cfg.setValue("minutes", minutes);
			cfg.setValue("timeout", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(timeout));
			cfg.setValue("private", this instanceof PrivateBooster ? true: false);
			
			writeContributors();
		}
		
		active.add(this);
		if (!silent) {
			if (this instanceof PrivateBooster && Bukkit.getPlayer(getOwner()) != null) QuickSell.local.sendTranslation(Bukkit.getPlayer(getOwner()), "pbooster.activate." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
			else {
				for (String message: QuickSell.local.getTranslation("booster.activate." + type.toString())) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier()))));
				}
			}
		}
	}
	
	public void extend(Booster booster) {
		addTime(booster.getDuration());
		
		int minutes = contributors.containsKey(booster.getOwner()) ? contributors.get(booster.getOwner()): 0;
		minutes = minutes + booster.getDuration();
		contributors.put(booster.getOwner(), minutes);
		
		writeContributors();
	}

	@SuppressWarnings("deprecation")
	public void deactivate() {
		if (!silent) {
			if (this instanceof PrivateBooster) {
				if (Bukkit.getPlayer(getOwner()) != null) QuickSell.local.sendTranslation(Bukkit.getPlayer(getOwner()), "pbooster.deactivate." + type.toString(), false, new Variable("%time%", String.valueOf(this.getDuration())), new Variable("%multiplier%", String.valueOf(this.getMultiplier())));
			}
			else {
				for (String message: QuickSell.local.getTranslation("booster.deactivate." + type.toString())) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", this.getOwner()).replace("%time%", String.valueOf(this.getDuration())).replace("%multiplier%", String.valueOf(this.getMultiplier()))));
				}
			}
		}
		if (!infinite) new File("data-storage/QuickSell/boosters/" + getID() + ".booster").delete();
		active.remove(this);
	}
	
	public static Iterator<Booster> iterate() {
		return active.iterator();
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	
	public Double getMultiplier()	{			return this.multiplier;			}
	public int getDuration()		{			return this.minutes;			}
	public Date getDeadLine() 		{			return this.timeout;			}
	public int getID()				{			return this.id;					}

	public long formatTime() {
		return ((getDeadLine().getTime() - Clock.getCurrentDate().getTime()) / (1000 * 60));
	}
	
	public void addTime(int minutes) {
		timeout = new Date(timeout.getTime() + minutes * 60 * 1000);
		cfg.setValue("timeout", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(timeout));
		cfg.save();
	}
	
	public static void update() {
		Iterator<Booster> boosters = Booster.iterate();
		while(boosters.hasNext()) {
			Booster booster = boosters.next();
			if (new Date().after(booster.getDeadLine())) {
				boosters.remove();
				booster.deactivate();
			}
		}
	}

	@Deprecated
	public static Double getMultiplier(String p) {
		return getMultiplier(p, BoosterType.MONETARY);
	}
	
	public static List<Booster> getBoosters(String player) {
		update();
		List<Booster> boosters = new ArrayList<Booster>();
		
		for (Booster booster: active) {
			if (booster.getAppliedPlayers().contains(player)) boosters.add(booster);
		}
		return boosters;
	}
	
	public static List<Booster> getBoosters(String player, BoosterType type) {
		update();
		List<Booster> boosters = new ArrayList<Booster>();
		
		for (Booster booster: active) {
			if (booster.getAppliedPlayers().contains(player) && booster.getType().equals(type)) boosters.add(booster);
		}
		return boosters;
	}
	
	public List<String> getAppliedPlayers() {
		List<String> players = new ArrayList<String>();
		for (Player p: Bukkit.getOnlinePlayers()) {
			players.add(p.getName());
		}
		return players;
	}
	
	public String getMessage() {
		return "messages.booster-use." + type.toString();
	}

	@Deprecated
	public static long getTimeLeft(String player) {
		long timeleft = 0;
		for (Booster booster: getBoosters(player)) {
			timeleft = timeleft + booster.formatTime();
		}
		return timeleft;
	}
	
	public BoosterType getType() {
		return this.type;
	}

	public boolean isSilent() {
		return silent;
	}
	
	public String getUniqueName() {
		switch(type) {
		case EXP:
			return "Booster (Experience)";
		case MONETARY:
			return "Booster (Money)";
		default:
			return "Booster";
		}
	}

	public static double getMultiplier(String name, BoosterType type) {
		double multiplier = 1.0;
		for (Booster booster: getBoosters(name, type)) {
			multiplier = multiplier * booster.getMultiplier();
		}
		return DoubleHandler.fixDouble(multiplier, 2);
	}
	
	public boolean isPrivate() {
		return this instanceof PrivateBooster;
	}
	
	public boolean isInfinite() {
		return this.infinite;
	}

	public Map<String, Integer> getContributors() {
		return this.contributors;
	}
	
	public void sendMessage(Player p, Variable... variables) {
		List<String> messages = QuickSell.local.getTranslation(getMessage());
		if (messages.isEmpty()) return;
		try {
			String message = ChatColor.translateAlternateColorCodes('&', messages.get(0).replace("%multiplier%", String.valueOf(this.multiplier)).replace("%minutes%", String.valueOf(this.formatTime())));
			for (Variable v: variables) {
				message = v.apply(message);
			}
			new TellRawMessage()
			.addText(message)
			.addClickEvent(ClickAction.RUN_COMMAND, "/boosters")
			.addHoverEvent(HoverAction.SHOW_TEXT, BoosterMenu.getTellRawMessage(this))
			.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
