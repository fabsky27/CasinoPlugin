package com.chrisimi.casinoplugin.scripts;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.ChatColor;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class JackpotManager
{
    private static final HashMap<String, Jackpot> jackpotHashMap = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();

    //TODO import/export
    //TODO creation handeling
    //TODO player handeling
    public JackpotManager()
    {
        init();
    }

    private void init()
    {
        importJackpots();
    }

    private synchronized void importJackpots()
    {
        //clear old jackpots to overwrite them
        jackpotHashMap.clear();

        BufferedReader rb;
        try
        {
            //read in data
            rb = new BufferedReader(new FileReader(Main.jackpotJson));

            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = rb.readLine()) != null)
                sb.append(line);

            //extract data
            if(line.length() <= 24) return;

            Jackpot.JackpotContainer container = gson.fromJson(sb.toString(), Jackpot.JackpotContainer.class);
            if(container == null)
            {
                CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while importing jackpot data");
                return;
            }

            //TODO add valid check

            for(Jackpot jackpot : container.jackpots)
            {
                //when there is the same jackpot... just rename it
                if(jackpotHashMap.containsKey(jackpot.name))
                {
                    CasinoManager.LogWithColor(ChatColor.YELLOW + "Error while importing jackpot data... two jackpots do have the same name... add a 1 at the end of the name");
                    jackpot.name += "1";
                }

                jackpotHashMap.put(jackpot.name, jackpot);
            }

            if(CasinoManager.configEnableConsoleMessages)
                CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully imported " + container.jackpots.size() + " jackpots");

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to import jackpot data: " + e.getMessage());
            e.printStackTrace(CasinoManager.getPrintWriterForDebug());
        }
    }

    private synchronized void exportJackpots()
    {
        BufferedWriter bw;

        try
        {
            bw = new BufferedWriter(new FileWriter(Main.jackpotJson));

            Jackpot.JackpotContainer container = new Jackpot.JackpotContainer();
            container.jackpots = (List<Jackpot>) jackpotHashMap.values();

            String json = gson.toJson(container);

            //overwrite old
            bw.write("");

            //write new
            bw.write(json);

            if(CasinoManager.configEnableConsoleMessages)
                CasinoManager.LogWithColor(ChatColor.GREEN + "Successully exported " + container.jackpots.size() + " jackpots");

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to export jackpot data: " + e.getMessage());
        }
    }
}
