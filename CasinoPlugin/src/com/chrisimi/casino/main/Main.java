package com.chrisimi.casino.main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import scripts.CasinoAnimation;
import scripts.CasinoManager;
import scripts.LeaderboardsignsManager;
import scripts.PlayerSignsManager;
import scripts.RollCommand;
import scripts.SignsManager;
import scripts.UpdateManager;
import slotChests.Animations.RollAnimationManager;

public class Main extends JavaPlugin {

	public static Economy econ = null;
	public static Permission perm = null;

	/**
	 * config version in jar
	 */
	public static String configVersion = "3.6"; //version in jar
	public static Boolean isConfigUpdated = true;
	
	public static String pluginVersion = "3.6";
	public static Boolean isPluginUpdated = true;
	
	
	public static File configYml;
	
	public static File signsYml;
	public static File playerSignsYml;
	public static File dataYml;
	public static File leaderboardSignsYml;
	
	public static File slotChestsYml;
	
	private static String pathToFolderOfPlugin; //.../plugins/CasinoPlugin/
	
	public static String fileSeparator;
	
	private static Main instance;
	public static MessageManager msgManager;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		//configYml = new File(getDataFolder(), "config.yml");
		fileSeparator = System.getProperty("file.separator");
		
		CasinoManager casinoManager = new CasinoManager(this);
		
		
		
		
		getPathToFolderOfPlugin();
		configYmlActivate();
		signsYmlActivate();
		playerSignsYmlActivate();
		slotChestsYmlActivate();
		
		UpdateManager.reloadConfig();
		
		
		 //equals .getConfigData
		msgManager = new MessageManager(this);
		
		//new ConfigurationManager(this);
		casinoManager.prefixYml();
		casinoManager.initialize();
		versionManager();
		VersionManager.CheckForNewVersion(pluginVersion, this);
		
		//getLogger().info("Minecraft Server version: " + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
		
		activateEconomySystem();
		activatePermissionSystem();
		
		Metrics metric = new Metrics(this); //Stats plugin
		configurateMetrics(metric);
		
		//CasinoManager.LogWithColor("Test: " + MessageManager.get("test"));
		
	}
	
	@Override
	public void onDisable()
	{
		LeaderboardsignsManager.clearAllTasks();
		CasinoManager.playerSignsManager.serverClose();
	}

	private void configurateMetrics(Metrics metric) {
		
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_slots", new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				int amount = CasinoAnimation.rollCount;
				CasinoAnimation.rollCount = 0;
				//Bukkit.getLogger().info("Send use_of_slots to bstats: " + amount);
				return amount;
				
			}
			
		}));
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_casinosigns", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				int amount = SignsManager.playCount;
				SignsManager.playCount = 0;
				//Bukkit.getLogger().info("Send use_of_casinoslots to bstats: " + amount);
				return amount;
				
			}
			
		}));
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_playersigns", new Callable<Integer>() {
			
			@Override
			public Integer call() throws Exception {
				int amount = PlayerSignsManager.rollCount;
				PlayerSignsManager.rollCount = 0;
				//Bukkit.getLogger().info("Send use_of_playersigns to bstats: " + amount);
				return amount;
			}
			
		}));
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_roll_command", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				int amount = RollCommand.rollAmount;
				RollCommand.rollAmount = 0;
				return amount;
			}
		}));
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_slotchest", new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				int amount = RollAnimationManager.rollsGlobal;
				RollAnimationManager.rollsGlobal = 0;
				return amount;
			}
			
		}));
		metric.addCustomChart(new Metrics.SingleLineChart("currently_running_leaderboardsigns", new Callable<Integer>()
		{

			@Override
			public Integer call() throws Exception
			{
				return LeaderboardsignsManager.leaderboardsignRunnableTaskID.size();
			}
		}));
	}
	



	private void versionManager() {
		String versionLocal = UpdateManager.getValue("version").toString();
		
		CasinoManager.LogWithColor(ChatColor.YELLOW + String.format("config version installed: %s - config version in jar: %s", versionLocal, configVersion));
		if(!(configVersion.equals(versionLocal))) {
			CasinoManager.LogWithColor(ChatColor.YELLOW + "There is a new version for the CasinoPlugin config.yml. Please follow ingame instructions! /casino updateconfig");
			isConfigUpdated = false;
		}
	}

	
	
	private void getPathToFolderOfPlugin() {
		File toCasinoPluginFolder = getDataFolder();
		if(!toCasinoPluginFolder.exists()) {
			toCasinoPluginFolder.mkdir();
		} else {
		
		}
		pathToFolderOfPlugin = toCasinoPluginFolder.getAbsolutePath();
	}
	private void configYmlActivate() {
		
		/*
		configYml = new File(getDataFolder().getAbsolutePath() + "\\config.yml");
		//getLogger().info(getDataFolder().getAbsolutePath()+ "\\config.yml");
		if(!(configYml.exists())) {
			new File(getDataFolder().getAbsolutePath()).mkdir();
			UpdateManager.createConfigYml(this);
		}
		*/
		getLogger().info(pathToFolderOfPlugin);
		configYml = new File(pathToFolderOfPlugin, "config.yml");
		if(!configYml.exists()) {
			try {
				configYml.createNewFile();
				UpdateManager.createConfigYml(this);
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create config.yml:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "config.yml exists!");
		
	}
	private void signsYmlActivate() {
		/*
		signsYml = new File(getDataFolder().getAbsolutePath() + "\\signs.json");
		if(!(signsYml.exists())) {
			try {
				signsYml.createNewFile();
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while creating signs.json!");
				e.printStackTrace();
			}
		}
		*/
		signsYml = new File(pathToFolderOfPlugin + fileSeparator + "signs.json");
		if(!signsYml.exists()) {
			try {
				signsYml.createNewFile();
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create signs.json:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "signs.json exists!");
	}
	private void playerSignsYmlActivate() {
		/*
		playerSignsYml = new File(getDataFolder().getAbsolutePath() + "\\playersigns.json");
		if(!(playerSignsYml.exists())) {
			try {
				playerSignsYml.createNewFile();
			} catch(IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while creating playersigns.json!");
				e.printStackTrace();
			}
		}
		*/
		playerSignsYml = new File(pathToFolderOfPlugin + fileSeparator + "playersigns.json");
		if(!playerSignsYml.exists()) {
			try {
				playerSignsYml.createNewFile();
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create playersigns.json:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "playersigns.json exists!");
		
		dataYml = new File(pathToFolderOfPlugin, "data.yml");
		if(!dataYml.exists()) {
			try {
				dataYml.createNewFile();
			} catch(IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create data.yml:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "data.yml exists!");
		
		
		leaderboardSignsYml = new File(pathToFolderOfPlugin, "leaderboardsigns.yml");
		if(!leaderboardSignsYml.exists()) {
			try {
				leaderboardSignsYml.createNewFile();
			} catch(IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create leaderboardsigns.yml:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "leaderboardsigns.yml exists!");
	}
	private void slotChestsYmlActivate() {
		/*
		slotChestsYml = new File(getDataFolder().getAbsolutePath() + "\\slotchests.json");
		if(!(slotChestsYml.exists())) {
			try {
				slotChestsYml.createNewFile();
			} catch(IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while creating slotchests.json!");
				e.printStackTrace();
			}
		}
		*/
		slotChestsYml = new File(pathToFolderOfPlugin + fileSeparator + "slotchests.json");
		if(!slotChestsYml.exists()) {
			try {
				slotChestsYml.createNewFile();
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create slotchests.json:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "slotchests.json exists!");
	}
	

	private void activateEconomySystem() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			CasinoManager.LogWithColor(ChatColor.RED + "Can't find vault!");
			Bukkit.shutdown();
		} else {
			RegisteredServiceProvider<Economy> esp = getServer().getServicesManager().getRegistration(Economy.class);
			if (esp == null) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to connect to vault, try restarting server and check vault!");
			}
			else {
				econ = esp.getProvider();
				CasinoManager.LogWithColor(ChatColor.GREEN + "Connected successfully to vault!");
			}
		}
	}
	private void activatePermissionSystem() {
		RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if(permProvider != null) {
			perm = permProvider.getProvider();
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully hooked with " + permProvider.getPlugin().getName());
		} else {
			CasinoManager.LogWithColor(ChatColor.RED + "Unable to activate vault's permission system! Try restarting server and check vault!");
		}
	}

	public static Main getInstance()
	{
		return instance;
	}
}
