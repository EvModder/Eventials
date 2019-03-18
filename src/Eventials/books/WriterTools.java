package Eventials.books;

import Eventials.Eventials;

public class WriterTools{
	public WriterTools(Eventials pl){
		//useCurItem = pl.getConfig().getBoolean("use-item-as-currency", true);

		//if(pl.getConfig().getDouble("chunk-generate-cost", 0.0) != 0){
		//	pl.getServer().getPluginManager().registerEvents(new ChunkGenerateListener(), plugin);

		new CommandSignBook(pl);
		new CommandUnsignBook(pl);
		new CommandFixBook(pl);
	}
}