package com.chrisimi.casinoplugin.serializables;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.google.gson.annotations.Expose;

public class PlayerSignsConfiguration {
	
	/**
	 * all different gamemodes a player sign can be
	 * @author chris
	 *
	 */
	public enum GameMode {
		BLACKJACK,
		DICE,
		SLOTS,
		Blackjack,
		Dice,
		Slots
	}
	
	@Expose
	public GameMode gamemode;
	
	@Expose
	public String ownerUUID;
	
	@Expose
	public Double bet;
	
	@Expose
	public String plusinformations;
	
	@Expose
	public Boolean disabled;
	
	@Expose
	public String worldname;
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	
	public int currentSignAnimation = 0;
	public boolean isRunning = false;
	
	
	public PlayerSignsConfiguration() {}
	
	public PlayerSignsConfiguration(Location lrc, GameMode gamemode, Player player, Double bet, String plusInformations) {
		this.gamemode = gamemode;
		this.ownerUUID = player.getUniqueId().toString();
		this.bet = bet;
		this.plusinformations = plusInformations;
		this.worldname = lrc.getWorld().getName();
		this.x = lrc.getX();
		this.y = lrc.getY();
		this.z = lrc.getZ();
		this.disabled = false;
		
		changeEnum();
	}
	public PlayerSignsConfiguration(Location lrc, GameMode gamemode, Double bet, String plusInformations) {
		this.gamemode = gamemode;
		this.ownerUUID = "server";
		this.bet = bet;
		this.plusinformations = plusInformations;
		this.worldname = lrc.getWorld().getName();
		this.x = lrc.getX();
		this.y = lrc.getY();
		this.z = lrc.getZ();
		this.disabled = false;
		
		changeEnum();
	}
	public void changeEnum()
	{
		switch (this.gamemode)
		{
		case Blackjack:
			gamemode = GameMode.BLACKJACK;
			break;
		case Dice:
			gamemode = GameMode.DICE;
			break;
		case Slots:
			gamemode = GameMode.SLOTS;
			break;
		default:
			break;
		}
	}
	
	public boolean unlimitedBet()
	{
		return this.bet == -1.0 || this.plusinformations.contains("-1");
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(worldname), x, y, z);
	}
	
	public double[] getWinChancesDiceOld() {
		double[] values = new double[2];
		String[] informations = plusinformations.split(";");
		String[] numbers = informations[0].split("-");
		values[0] = Double.parseDouble(numbers[0]);
		values[1] = Double.parseDouble(numbers[1]);
		return values;
	}

	public int[] getWinChancesDice()
	{
		//TODO add check for false values
		int[] values = new int[2];
		String[] informations = plusinformations.split(";");
		String[] numbers = informations[0].split("-");
		values[0] = Integer.parseInt(numbers[0]);
		values[1] = Integer.parseInt(numbers[1]);

		return values;
	}


	public double winMultiplicatorDice() {
		String[] informations = plusinformations.split(";");
		Double value = Double.parseDouble(informations[1].trim());
		return value;
	}
	/**
	 * Get the owner of the sign
	 * @return offlineplayer instance of player, null is owner is server
	 */
	public OfflinePlayer getOwner() {
		if(isServerOwner()) return null;
		return Bukkit.getOfflinePlayer(UUID.fromString(this.ownerUUID));
	}
	public String getOwnerName()
	{
		if(isServerOwner()) return "§6Server";
		else {
			return getOwner().getName();
		}
	}
	public Boolean hasOwnerEnoughMoney() {
		if(isServerOwner()) return true;
		return Main.econ.has(getOwner(), this.winMultiplicatorDice() * this.bet);
	}
	public Boolean hasOwnerEnoughMoney(double amount) {
		if(isServerOwner()) return true;
		return Main.econ.has(getOwner(), amount);
	}
	public Sign getSign() {
		try 
		{
			Sign sign =  (Sign) Bukkit.getWorld(worldname).getBlockAt(this.getLocation()).getState();
			return sign;
		} catch(ClassCastException e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "There is not a sign for a CasinoSign at: " + this.getLocation().toString());
		}
		return null;
	}
	
	public Boolean isSignDisabled() {
		return disabled;
	}
	public Boolean isSignEnabled() {
		return !(disabled);
	}
	public void enableSign() {
		this.disabled = false;
	}
	public void disableSign() {
		this.disabled = true;
	}
	
	/**
	 * gets the maximum bet which is valid
	 * if {@link PlayerSignsConfiguration#unlimitedBet} true then it will return the maximum account which the owner can payout
	 * @return {@link Double} value of maxValue
	 */
	public Double blackjackGetMaxBet() 
	{
		if(unlimitedBet())
		{
			double value = (isServerOwner()) ? Double.MAX_VALUE : Main.econ.getBalance(getOwner()) / blackjackMultiplicator();
			int rounded = (int) (value * 100.0);
			return (double)rounded / 100.0;
		}
		
		String[] values = this.plusinformations.split(";");
		return Double.valueOf(values[0]);
	}
	public Double blackjackGetMinBet() 
	{
		return (unlimitedBet()) ? 1.0 : this.bet;
	}
	
	/**
	 * get the multiplcator from the to writing
	 * @return double value which is the factor
	 */
	public Double blackjackMultiplicator() {
		if(this.plusinformations.contains("to"))
		{
			String[] values = this.plusinformations.split(";");
			String[] blackjackValues = values[1].split("to");
			Double left = Double.parseDouble(blackjackValues[0]);
			Double right = Double.parseDouble(blackjackValues[1]);
			return (double)left/(double)right;
		}
		String[] values = this.plusinformations.split(";");
		return Double.valueOf(values[1]);
	}
	public Double blackjackGetWin(Double bet) {
		return blackjackMultiplicator()*bet;
	}
	
	public Boolean isServerOwner()
	{
		return this.ownerUUID.equalsIgnoreCase("server");
	}
	/**
	 * Take money from owner
	 * @param amount amount
	 */
	public void withdrawOwner(double amount)
	{
		if(!isServerOwner())
		{
			Main.econ.withdrawPlayer(getOwner(), amount);
		}
	}
	/**
	 * Give owner money
	 * @param amount amount
	 */
	public void depositOwner(double amount) 
	{
		if(!isServerOwner())
		{
			Main.econ.depositPlayer(getOwner(), amount);
		}
	}
	//A-10-2.5;B-50-3.5;C-40-3.8
	public String[] getSlotsSymbols()
	{
		String[] symbols = this.plusinformations.split(";");
		String[] values = new String[3];
		for(int i = 0; i < 3; i++)
		{
			String[] splited = symbols[i].split("-");
			values[i] = splited[0];
		}
		return values;
	}
	public double[] getSlotsMultiplicators()
	{
		String[] symbols = this.plusinformations.split(";");
		double[] values = new double[3];
		for(int i = 0; i < 3; i++)
		{
			String[] splited = symbols[i].split("-");
			values[i] = Double.parseDouble(splited[1]);
		}
		return values;
	}
	public double[] getSlotsWeight()
	{
		String[] symbols = this.plusinformations.split(";");
		double[] values = new double[3];
		for(int i = 0; i < 3; i++)
		{
			String[] splited = symbols[i].split("-");
			values[i] = Double.parseDouble(splited[2]);
		}
		return values;
	}
	public double getSlotsHighestPayout()
	{
		double[] multiplicators = getSlotsMultiplicators();
		double highestMulti = 0.0;
		for(int i = 0; i < 3; i++)
		{
			if(highestMulti < multiplicators[0])
				highestMulti = multiplicators[0];
		}
		return this.bet * highestMulti + bet;
	}
	public String[] getColorMultiplicators()
	{
		return new String[] {"§b", "§a", "§c"};
	}
}
