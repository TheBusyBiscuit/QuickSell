package me.mrCookieSlime.QuickSell.boosters;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class PrivateBooster extends Booster {

	public PrivateBooster(String owner, double multiplier, int minutes) {
		super(owner, multiplier, minutes);
	}
	
	public PrivateBooster(BoosterType type, String owner, double multiplier, int minutes) {
		super(type, owner, multiplier, minutes);
	}
	
	public PrivateBooster(int id) throws ParseException {
		super(id);
	}
	
	@Override
	public List<String> getAppliedPlayers() {
		return Arrays.asList(owner);
	}
	
	@Override
	public String getMessage() {
		return "messages.pbooster-use." + type.toString();
	}

}
