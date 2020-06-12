package utils.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import serializeableClass.PlayData;

public class SumlossAnalyse extends DataAnalyse
{
	public SumlossAnalyse(List<PlayData> data)
	{
		super(data);
	}

	@Override
	protected HashMap<OfflinePlayer, Double> prepareData()
	{
		HashMap<OfflinePlayer, Double> resultHashMap = new HashMap<>();
		
		Iterator<PlayData> iterator = data.iterator();
		
		while(iterator.hasNext())
		{
			PlayData data = iterator.next();
			
			if(data.WonAmount != 0.0) continue;
			
			if(resultHashMap.containsKey(data.Player))
			{
				resultHashMap.compute(data.Player, (a, b) -> b + data.PlayAmount);
			}
			else
			{
				resultHashMap.put(data.Player, data.PlayAmount);
			}
			
		}
		return resultHashMap;
	}

	@Override
	public LinkedHashMap<Integer, Query> getData(int from, int to)
	{
		return this.getData(from, to, comparator);
	}

}
