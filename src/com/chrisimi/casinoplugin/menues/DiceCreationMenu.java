package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import com.chrisimi.inventoryapi.Inventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DiceCreationMenu extends Inventory implements IInventoryAPI
{
    private boolean isServerSign = false;
    private double bet = 0.0;
    private int rangeMin = -1;
    private int rangeMax = -1;
    private double winMultiplicand = 2.0;
    private boolean isDisabled = false;

    private boolean allValuesValid = false;

    private ItemStack setBet = ItemAPI.createItem("§6Set the bet", Material.GOLD_INGOT);
    private ItemStack setWinRange = ItemAPI.createItem("§6Set the win range", Material.COMPASS);
    private ItemStack setWinMultiplicant = ItemAPI.createItem("§6Set the win multiplicand", Material.JUKEBOX);
    private ItemStack serverSign = ItemAPI.createItem("§6change it to a server sign", Material.GOLD_BLOCK);
    private ItemStack disableSign = ItemAPI.createItem("§4Disable this sign", Material.RED_WOOL);
    private ItemStack enableSign = ItemAPI.createItem("§aEnable this sign", Material.GREEN_WOOL);
    private ItemStack finishButton = ItemAPI.createItem("§a finish", Material.STONE_BUTTON);

    private final Location lrc;
    public DiceCreationMenu(Location lrc, Player player)
    {
        super(player, 9*1, Main.getInstance(), "Dice creation menu");
        this.lrc = lrc;

        initialize();
        addEvents(this);

        openInventory();
    }

    private void initialize()
    {
        bukkitInventory.setItem(0, setBet);
        bukkitInventory.setItem(1, setWinRange);
        bukkitInventory.setItem(2, setWinMultiplicant);
        bukkitInventory.setItem(5, serverSign);
        bukkitInventory.setItem(7, disableSign);

        bukkitInventory.setItem(8, finishButton);
    }

    private void updateInventory()
    {
        bukkitInventory.setItem(7, (isDisabled) ? enableSign : disableSign);

        updateLoreButton();
    }

    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().equals(disableSign)) isDisabled = true;
        else if(event.getClicked().equals(enableSign)) isDisabled = false;
        else if(event.getClicked().equals(finishButton) && allValuesValid) finishButton();
        updateInventory();
    }

    private void finishButton()
    {
        //TODO finish creation
    }
    private void updateLoreButton()
    {
        List<String> lore = new ArrayList<>();
        allValuesValid = true;

        allValuesValid = bet != 0.0;
        lore.add((bet != 0.0) ? String.format("-§a bet is %s", Main.econ.format(bet)) : "-§4 bet is not set");

        if(rangeMin != -1 && rangeMax != -1 && rangeMin < rangeMax && rangeMax < 100 && rangeMin > 0)
            lore.add("-§a range is set");
        else
        {
            allValuesValid = false;
            lore.add("-§4 the range is invalid!");
        }

        if(winMultiplicand != 0.0)
            lore.add("-§a win multiplicand is set to " + winMultiplicand);
        else
        {
            lore.add("-§4 win multiplicand is not set");
            allValuesValid = false;
        }

        ItemMeta meta = finishButton.getItemMeta();

        if(allValuesValid)
            meta.setDisplayName("§afinish the creation or update of the sign");
        else
            meta.setDisplayName("§4you can't finish the creation or update of the sign");

        meta.setLore(lore);
        finishButton.setItemMeta(meta);
    }
}