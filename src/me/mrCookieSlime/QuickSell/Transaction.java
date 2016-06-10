package me.mrCookieSlime.QuickSell;

import me.mrCookieSlime.QuickSell.SellEvent.Type;

public class Transaction {
	
	long timestamp;
	int items;
	double money;
	Type type;
	
	public Transaction(String value) {
		timestamp = Long.valueOf(value.split(" __ ")[0]);
		type = Type.valueOf(value.split(" __ ")[1]);
		items = Integer.parseInt(value.split(" __ ")[2]);
		money = Double.valueOf(value.split(" __ ")[3]);
	}

	public Transaction(long timestamp, Type type, int soldItems, double money) {
		this.timestamp = timestamp;
		this.type = type;
		this.items = soldItems;
		this.money = money;
	}

	public int getItemsSold() {
		return items;
	}

	public double getMoney() {
		return money;
	}
	
	public static int getItemsSold(String string) {
		return Integer.parseInt(string.split(" __ ")[2]);
	}
	
	public static double getMoney(String string) {
		return Double.valueOf(string.split(" __ ")[3]);
	}

}
