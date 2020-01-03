package slotChests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;

import com.chrisimi.casino.main.Main;
import com.mojang.authlib.minecraft.InsecureTextureException.WrongTextureOwnerException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.datafixers.types.templates.List;
import com.mysql.fabric.xmlrpc.base.Array;

import net.minecraft.server.v1_14_R1.EntityFox.i;
import net.minecraft.server.v1_14_R1.LootItemFunctionApplyBonus.e;
import scripts.CasinoManager;

public class WarehouseMenu implements Listener{
	
	/**
	 * alles abgedeckt mit Scheiben au�er ein Platz! da wird Items reingeshiftet
	 * 
	 * beim schlie�en inventar durchschauen! ob spieler etwas rausgenommen hat - fertig
	 * 
	 * TODO: bei inventar close task l�schen!
	 */
	public static HashMap<Inventory, Integer> tasks = new HashMap<>();
	
	
	private Main main;
	private SlotChest slotChest;
	private Player owner;
	private Inventory warehouseMenu;
	
	private ItemStack changeSortModeSign;
	private Boolean sortByID = true;
	private ItemStack sortButton;
	
	public WarehouseMenu(Main main, SlotChest slotChest, Player owner) {
		this.main = main;
		this.slotChest = slotChest;
		this.owner = owner;
		main.getServer().getPluginManager().registerEvents(this, main);
		initialize();
		owner.openInventory(warehouseMenu);
		initializeInventoryUpdateRunnable();
	}
	
	private void initialize() {
		changeSortModeSign = new ItemStack(Material.OAK_SIGN);
		ItemMeta meta = changeSortModeSign.getItemMeta();
		ArrayList<String> loreList = new ArrayList<>();
		loreList.add("�6click to change!");
		meta.setLore(loreList);
		meta.setDisplayName("�asort by ID");
		changeSortModeSign.setItemMeta(meta);
		
		sortButton = new ItemStack(Material.STONE_BUTTON);
		meta = sortButton.getItemMeta();
		meta.setDisplayName("�6SORT");
		sortButton.setItemMeta(meta);
		
		warehouseMenu = Bukkit.createInventory(owner, 9*6, "Warehouse");
		
		for(int i = 45; i < 9*6; i++) {
			if(i == 49) continue;
			warehouseMenu.setItem(i, new ItemStack(Material.PINK_STAINED_GLASS_PANE));
		}
		for(int i = 0; i < 9*5 ; i++) {
			if(i >= slotChest.lager.size()) {
				warehouseMenu.setItem(i, new ItemStack(Material.PINK_STAINED_GLASS_PANE));
			} else {
				warehouseMenu.setItem(i, slotChest.lager.get(i));
			}
			
			
		}
		warehouseMenu.setItem(52, changeSortModeSign);
		warehouseMenu.setItem(53, sortButton);
	}
	
	private void updateInventory() {
		warehouseMenu.setItem(52, changeSortModeSign);
		warehouseMenu.setItem(53, sortButton);
	}
	
	private void initializeInventoryUpdateRunnable() {
		int taskId = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
			
			@Override
			public void run() {
				ItemStack itemStack = warehouseMenu.getItem(49);
				if(itemStack == null) return;
				
				if(itemStack.getType() != Material.AIR && itemStack.getType() != Material.PINK_STAINED_GLASS_PANE) {
					if(slotChest.itemstoWinContains(itemStack.getType())) {
						
						if(!(slotChest.lager.size() >= 9*5)) {
							
							slotChest.lager.add(itemStack);
							warehouseMenu.setItem(49, new ItemStack(Material.AIR));
							for(int i = 0; i < 9*5; i++) {
								if(warehouseMenu.getItem(i) == null || warehouseMenu.getItem(i).getType() == Material.PINK_STAINED_GLASS_PANE) {
									warehouseMenu.setItem(i, itemStack);
									break;
								}
								
								
							}
							
							updateLager();
							owner.sendMessage(CasinoManager.getPrefix() + "Successfully added " + itemStack.getType().toString() + " to the warehouse");
							
						} else {
							owner.sendMessage(CasinoManager.getPrefix() + "�4The warehouse is full!");
							warehouseMenu.setItem(49, new ItemStack(Material.AIR));
							putItemInInventory(itemStack);
						}
						
						
					} else {
						owner.sendMessage(CasinoManager.getPrefix() + "�4This item is not in the list to win!");
						warehouseMenu.setItem(49, new ItemStack(Material.AIR));
						putItemInInventory(itemStack);
					}
					
				}
			}
		}, 10L, 10L);
		tasks.put(warehouseMenu, taskId);
	}
	private void putItemInInventory(ItemStack item) {
		int slot = owner.getInventory().first(Material.AIR);
		if(slot == -1) 
			owner.getWorld().dropItem(owner.getLocation().add(0, 3, 0), item);
		else
			owner.getInventory().setItem(slot, item);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(!(event.getInventory().equals(warehouseMenu))) return;
		
		if(tasks.containsKey(event.getInventory()))  {
			Bukkit.getServer().getScheduler().cancelTask(tasks.get(event.getInventory()));
			tasks.remove(event.getInventory());
		}
		updateLager();
		CasinoManager.slotChestManager.save();
	}
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getCurrentItem() == null) return;
		if(!(event.getInventory().equals(warehouseMenu))) return;
		
		if(event.getCurrentItem().equals(new ItemStack(Material.PINK_STAINED_GLASS_PANE))) {
			event.setCancelled(true);
			return;
		}
		
		if(event.getCurrentItem().equals(changeSortModeSign)) {
			
			ItemMeta meta = changeSortModeSign.getItemMeta();
			meta.setDisplayName((sortByID) ? "�asort by ID" : "�asort by name");
			ArrayList<String> loreList = new ArrayList<>();
			loreList.add("�6click to change!");
			meta.setLore(loreList);
			changeSortModeSign.setItemMeta(meta);
			updateInventory();
			
			sortByID = !sortByID;
			event.setCancelled(true);
		} else if(event.getCurrentItem().equals(sortButton)) {
			updateLager();
			sortLager();
			event.setCancelled(true);
		}
			
		
	}
	
	
	
	private void sortLager() {
		updateLager();
		
		if(sortByID)
			sortLagerID();
		else
			sortLagerName();
		
		
		updateLager();
	}
	private void sortLagerID() {
		HashMap<Material, Integer> items = slotChest.getLagerWithNumbers();
		for(Entry<Material, Integer> entry : items.entrySet()) System.out.println(entry.getKey().toString() + " " + entry.getValue());
		
		
		TreeMap<Material, Integer> sortedList = new TreeMap<>(items);
		
		for(Entry<Material, Integer> entry : sortedList.entrySet()) System.out.println(entry.getKey().toString() + " " + entry.getValue());
		
		Comparator<Entry<Material, Integer>> valueComperator = new Comparator<Map.Entry<Material,Integer>>() {
			@Override
			public int compare(Entry<Material, Integer> e1, Entry<Material, Integer> e2) {
				return e2.getKey().compareTo(e1.getKey());
			}
		};
		
		ArrayList<Entry<Material, Integer>> listOfEntries = new ArrayList<Entry<Material, Integer>>(sortedList.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		for(Entry<Material, Integer> entry : listOfEntries) System.out.println(entry.getKey().toString() + " " + entry.getValue());
		
		
		LinkedHashMap<Material, Integer> sortedByList = new LinkedHashMap<Material, Integer>(listOfEntries.size());
		for(Entry<Material, Integer> entry : listOfEntries) sortedByList.put(entry.getKey(), entry.getValue());
		
		int slot = 0;
		
		for(Entry<Material, Integer> entry : sortedByList.entrySet()) {
			System.out.println(entry.getKey().toString() + " " + entry.getValue());
			if(entry.getValue() <= 0) continue;
			
			int amountOfItems = entry.getValue();
			
			while(amountOfItems >= 1) {
				
				if(amountOfItems > 64) {
					warehouseMenu.setItem(slot, new ItemStack(entry.getKey(), 64));
					slot++;
					amountOfItems -= 64;
				} else {
					warehouseMenu.setItem(slot, new ItemStack(entry.getKey(), amountOfItems));
					slot++;
					break;
				}
				
				
				
			}
			
		}
		
	}
	private void sortLagerName() {
		
	}
	
	
	private void updateLager() {
		ArrayList<ItemStack> tempLager = new ArrayList<ItemStack>();
		for(int i = 0; i < 9*5; i++) {
			if(warehouseMenu.getItem(i) == null || warehouseMenu.getItem(i).getType() == Material.AIR || warehouseMenu.getItem(i).getType() == Material.PINK_STAINED_GLASS_PANE) continue;
			tempLager.add(warehouseMenu.getItem(i));
		}

		
		
		
		slotChest.lager = tempLager;
		slotChest.save();
	}
}