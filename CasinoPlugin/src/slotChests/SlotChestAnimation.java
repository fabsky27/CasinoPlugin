package slotChests;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.chrisimi.casino.main.Main;

@Deprecated
public class SlotChestAnimation implements Listener{

	private final Main main;
	private final SlotChest slotChest;
	private final OfflinePlayer owner;
	private final Player player; 
	private final Boolean isOwnerOffline;
	
	/*
	 * before starting this animation every option should be fixed!
	 */
	public SlotChestAnimation(Main main, SlotChest slotChest, Player whoClicked) {
		this.slotChest = slotChest;
		this.main = main;
		
		owner = slotChest.getOwner();
		isOwnerOffline = (slotChest.getOwner() instanceof Player) ? true : false; 
		this.player = whoClicked;
		initialize();
	}
	private void initialize() {
		
	
		
		
	}
}