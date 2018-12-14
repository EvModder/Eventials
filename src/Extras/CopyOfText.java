package Extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import Eventials.Eventials;

public class CopyOfText {
	enum Event{CLICK,HOVER};
	public enum TextAction{
		//ClickEvent
		LINK(ChatColor.AQUA.toString(), "open_url", Event.CLICK),
		FILE("�[something]", "open_file", Event.CLICK),
		CMD(ChatColor.DARK_GREEN.toString(), "run_command", Event.CLICK),
		SUGGEST_CMD(ChatColor.BLUE.toString(), "suggest_command", Event.CLICK),
		PAGE("�[something]", "change_page", Event.CLICK),
		//HoverEvent
		TEXT("�[something]", "show_text", Event.HOVER),
		ACHIEVEMENT("�[something]", "show_achievement", Event.HOVER),
		ITEM("�[something]", "show_item", Event.HOVER),
		ENTITY("�[something]", "show_entity", Event.HOVER),
		
		//custom
		WARP(ChatColor.LIGHT_PURPLE+"@", "run_command", Event.CLICK);
		//MONEY("�a$", "show_text"),
		//PLUGIN("�c", "show_item"),
		
		Event type;
		String marker, action;
		TextAction(String s, String a, Event t){marker = s; action = a; type = t;}
		
		public static int countNodes(String str){
			int count = 0;
			for(TextAction n : TextAction.values()) count += StringUtils.countMatches(str, n.marker);
			return count;
		}
		
		public static String parseToRaw(String string){
			//{"text":"xxx","extra":[{"text":"xxx","clickEvent":{"action":"xxx","value":"xxx"}}]}
			StringBuilder raw = new StringBuilder("[{");
			
			int nodes = TextAction.countNodes(string);//component count
			for(short i = 0; i < nodes; ++i){
				
				//get next node
				TextAction node = null;
				int nodeIndex = string.length();
				for(TextAction n : TextAction.values()){
					int x = string.indexOf(n.marker);
					if(x != -1 && x < nodeIndex){nodeIndex = x; node=n;}
				}
				
				//cut off preText
				String preText = string.substring(0, nodeIndex); string = string.substring(nodeIndex);
				
				//cut off hyperText
				int endSpecial = string.indexOf("�e"); if(endSpecial == -1) endSpecial = string.length();
				String hyperText = string.substring(0, endSpecial); string = string.substring(endSpecial);
				
				String actionText="";
				//detect underlying command/link/values
				if(hyperText.contains("=>")){
					String[] data = hyperText.split("=>");
					hyperText = data[0];
					actionText = data[1].trim();
				}
				else{
					if(node == TextAction.CMD){
						actionText = hyperText.substring(node.marker.length()).trim();
					}
					else if(node == TextAction.WARP){
						actionText = "/warp "+hyperText.substring(node.marker.length()).trim();
					}
					else actionText = hyperText.trim();
				}
				
				if(!preText.isEmpty()) raw.append("\"text\":\"").append(preText).append("\"");
				if(!hyperText.isEmpty()){
					raw.append(",\"extra\":[{\"text\":\"").append(hyperText).append("\"");
					if(node != null) raw.append(",\"").append(node.type == Event.CLICK ? "clickEvent" : "hoverEvent")
						.append("\":{\"action\":\"").append(node.action).append("\",\"value\":\"").append(actionText).append("\"}");
					raw.append("}]");
				}
				raw.append(i == nodes-1 ? "}" : "},{");
				
				// /tellraw @a ["First","Second","Third"]
				// {"text":"Click","clickEvent":{"action":"open_url","value":"http://google.com"}}
				// {"text":"xxx","extra":[{"text":"xxx","clickEvent":{"action":"xxx","value":"xxx"}}]}
			}
			return raw.append(']').toString();
		}
	};

	public static void sendHyperTextCommand(String preMsg, String hyperMsg, String command, String postMsg, Player... recipients){
		IChatBaseComponent comp;
		if(preMsg != null && !preMsg.isEmpty()){
			comp = ChatSerializer.a(new StringBuilder("{\"text\":\"").append(preMsg)
					.append("\",\"extra\":[{\"text\":\"").append(hyperMsg)
					.append("\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"").append(command).append("\"}}]}").toString());
		}
		else{
			comp = ChatSerializer.a(new StringBuilder("{\"extra\":[{\"text\":\"�2").append(hyperMsg)
					.append("\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"").append(command).append("\"}}]}").toString());
		}
		if(postMsg != null && !postMsg.isEmpty()){
			comp.addSibling(ChatSerializer.a("{\"text\":\""+postMsg+"\"}"));
		}
		PacketPlayOutChat packet = new PacketPlayOutChat(comp);
		for(Player p : recipients) ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
	}

	//TODO: Unused
	public static void sendModifiedText(String[] preMsgs, String[] hyperMsgs,
			TextAction[] actions, String[] values, String postMsg, Player... recipients){

		if(preMsgs.length != hyperMsgs.length || hyperMsgs.length != actions.length || actions.length != values.length ||
				preMsgs.length == 0) return;

		//{"text":"xxx","extra":[{"text":"xxx","clickEvent":{"action":"xxx","value":"xxx"}}]}
		IChatBaseComponent comp = ChatSerializer.a(new StringBuilder("{\"text\":\"").append(preMsgs[0])
				.append("\",\"extra\":[{\"text\":\"").append(hyperMsgs[0]).append("\",\"clickEvent\":{\"action\":\"")
				.append(actions[0].action).append("\",\"value\":\"").append(values[0]).append("\"}}]}").toString());

		for(int i=1; i<hyperMsgs.length; ++i){
			comp.addSibling(ChatSerializer.a(new StringBuilder("{\"text\":\"").append(preMsgs[i]).append("\",\"extra\":[{\"text\":\"")
					.append(hyperMsgs[i]).append("\",\"clickEvent\":{\"action\":\"")
					.append(actions[i].action).append("\",\"value\":\"").append(values[i]).append("\"}}]}").toString()));
		}
		if(postMsg != null && !postMsg.isEmpty()){
			comp.addSibling(ChatSerializer.a("{\"text\":\""+postMsg+"\"}"));
		}
		PacketPlayOutChat packet = new PacketPlayOutChat(comp);

		if(recipients != null && recipients.length != 0) for(Player p : recipients){
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}
		else for(Player p : Eventials.getPlugin().getServer().getOnlinePlayers()){
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}
	}

	static char colorSymbol = ChatColor.WHITE.toString().charAt(0);
	static Character[] SET_VALUES = new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
										  'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o', 'r'};
	public static final Set<Character> colorChars = new HashSet<Character>(Arrays.asList(SET_VALUES));
	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate){
		char[] msg = textToTranslate.toCharArray();
		for(int i = 1; i < msg.length; ++i){
			if(msg[i-1] == altColorChar && colorChars.contains(msg[i]) && (i == 1 || msg[i-2] != '\\')){
				msg[i-1] = colorSymbol;
			}
		}
		return new String(msg);
	}

	public static ArrayList<String> toListFromString(String string){
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(Arrays.asList(string.substring(1, string.lastIndexOf(']')).split(", ")));
		return list;
	}
}
