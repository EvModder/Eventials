package Extras;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import Eventials.Eventials;

public class Text {
/*	private static final RefClass classIChatBaseComponent = ReflectionUtils.getRefClass("{nms}.IChatBaseComponent");
	private static final RefClass classChatSerializer = ReflectionUtils.getRefClass("{nms}.IChatBaseComponent$ChatSerializer");
	private static final RefClass classPacketPlayOutChat = ReflectionUtils.getRefClass("{nms}.PacketPlayOutChat");
	private static final RefClass classCraftPlayer = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
	private static final RefClass classEntityPlayer = ReflectionUtils.getRefClass("{nms}.EntityPlayer");
	private static final RefClass classPlayerConnection = ReflectionUtils.getRefClass("{nms}.PlayerConnection");
	private static final RefClass classPacket = ReflectionUtils.getRefClass("{nms}.Packet");

	private static final RefMethod methodA = classChatSerializer.getMethod("a", String.class);
	private static final RefMethod methodAddSibling = classIChatBaseComponent.getMethod("addSibling", classIChatBaseComponent);
	private static final RefMethod methodGetHandle = classCraftPlayer.getMethod("getHandle");
	private static final RefMethod methodSendPacket = classPlayerConnection.getMethod("sendPacket", classPacket);

	private static final RefField fieldPlayerConnection = classEntityPlayer.getField("playerConnection");
	private static final RefConstructor makePacketPlayOutChat = classPacketPlayOutChat.getConstructor(classIChatBaseComponent);
*/
	enum Event{CLICK,HOVER};
	public enum TextAction{
		//ClickEvent
		LINK("§b", "open_url", Event.CLICK),
		FILE("&[something]", "open_file", Event.CLICK),
		RUN_CMD("§2", "run_command", Event.CLICK),
		SUGGEST_CMD("§9", "suggest_command", Event.CLICK),
		PAGE("&[something]", "change_page", Event.CLICK),
		//HoverEvent
		SHOW_TEXT("§a", "show_text", Event.HOVER),
		ACHIEVEMENT("&[something]", "show_achievement", Event.HOVER),
		ITEM("&[something]", "show_item", Event.HOVER),
		ENTITY("&[something]", "show_entity", Event.HOVER),

		//custom
		WARP(ChatColor.LIGHT_PURPLE+"@", "run_command", Event.CLICK);
		//MONEY(ChatColor.GREEN+"$", "show_text"),
		//PLUGIN(ChatColor.RED+"", "show_item"),

		Event type;
		String marker, action;
		TextAction(String s, String a, Event t){marker = s; action = a; type = t;}

		@Override public String toString(){return marker;}

		public static int countNodes(String str){
			int count = 0;
			for(TextAction n : TextAction.values()){
				count += StringUtils.countMatches(str.replace("\\"+n.marker, ""), n.marker);
			}
			return count;
		}

		public static String parseToRaw(String string, String endIndicator){
			//{"text":"xxx","extra":[{"text":"xxx","clickEvent":{"action":"xxx","value":"xxx"}}]}
			StringBuilder raw = new StringBuilder("[{\"text\":\"\"}");

			int nodes = TextAction.countNodes(string);//component count
			for(short i = 0; i < nodes; ++i){
				//get next node
				TextAction node = null;
				int nodeIndex = string.length();
				for(TextAction n : TextAction.values()){
					int x = -1;
					do{x = string.indexOf(n.marker, x+1);}
					while(x != -1 && isEscaped(string, x));
					if(x != -1 && x < nodeIndex){nodeIndex = x; node=n;}
				}

				//cut off preText
				String preText = string.substring(0, nodeIndex); string = string.substring(nodeIndex);
				preText = unescapeString(preText);

				//cut off hyperText
				int endSpecial = string.indexOf(endIndicator); if(endSpecial == -1) endSpecial = string.length();
				String hyperText = string.substring(0, endSpecial); string = string.substring(endSpecial);
				hyperText = unescapeString(hyperText);

				String actionText="";
				//detect underlying command/link/values
				if(hyperText.contains("=>")){
					String[] data = hyperText.split("=>");
					hyperText = data[0];
					actionText = data[1].trim();
				}
				else{
					if(node == TextAction.RUN_CMD){
						actionText = hyperText.substring(node.marker.length()).trim();
					}
					else if(node == TextAction.WARP){
						actionText = "/warp "+hyperText.substring(node.marker.length()).trim();
					}
					else actionText = hyperText.trim();
				}

//				Eventials.getPlugin().getLogger().info("PreText: "+preText);
//				Eventials.getPlugin().getLogger().info("HyperText: "+hyperText);
//				Eventials.getPlugin().getLogger().info("ActionText: "+actionText);

				if(!preText.isEmpty()) raw.append(",{\"text\":\"").append(preText).append("\"}");
				if(!hyperText.isEmpty()){
					raw.append(",{\"text\":\"").append(hyperText).append('"');
					if(node != null) raw
						.append(",\"").append(node.type == Event.CLICK ? "clickEvent" : "hoverEvent")
						.append("\":{\"action\":\"").append(node.action).append("\",\"value\":\"")
						.append(actionText).append("\"}");
					raw.append('}');
				}
				// Old (but perfectly valid) method:
/*				raw.append(",{\"text\":\"").append(preText).append('"');
				if(!hyperText.isEmpty()){
					raw.append(",\"extra\":[{\"text\":\"").append(hyperText).append("\"");
					if(node != null) raw.append(",\"").append(node.type == Event.CLICK ? "clickEvent" : "hoverEvent")
						.append("\":{\"action\":\"").append(node.action).append("\",\"value\":\"")
						.append(actionText).append("\"}}");
					raw.append(']');
				}
				raw.append('}');*/

				// /tellraw @a ["First","Second","Third"]
				// {"text":"Click","clickEvent":{"action":"open_url","value":"http://google.com"}}
				// {"text":"xxx","extra":[{"text":"xxx","clickEvent":{"action":"xxx","value":"xxx"}}]}
			}
			string = unescapeString(string);
			if(!string.isEmpty()) raw.append(",{\"text\":\"").append(string).append("\"}");
			return raw.append(']').toString();
		}
	};

	public static void sendModifiedText(String preMsg, String hyperMsg, TextAction action, String value,
			String postMsg, Player... recipients){
		preMsg = preMsg.replace("\n", "\\n");
		hyperMsg = hyperMsg.replace("\n", "\\n");
		value = value.replace("\n", "\\n");
		postMsg = postMsg.replace("\n", "\\n");
		StringBuilder raw = new StringBuilder("[");
		if(preMsg != null && !preMsg.isEmpty()) raw.append("{\"text\":\"").append(preMsg).append("\"},");
		raw.append("{\"text\":\"").append(hyperMsg).append("\",\"clickEvent\":{\"action\":\"")
				.append(action.action).append("\",\"value\":\"").append(value).append("\"}}");
		if(postMsg != null && !postMsg.isEmpty()) raw.append(",{\"text\": \"").append(postMsg).append("\"}");
		raw.append(']');
		for(Player p : recipients){
			Eventials.getPlugin().runCommand("minecraft:tellraw "+p.getName()+' '+raw);
			//p.sendRawMessage(raw);//TODO: Doesn't work! (last checked: 1.12.1)
		}
	}

	public static void sendModifiedText(String[] preMsgs, String[] hyperMsgs,
			TextAction[] actions, String[] values, String postMsg, Player... recipients){
		if(preMsgs.length != hyperMsgs.length || hyperMsgs.length != actions.length || actions.length != values.length ||
				preMsgs.length == 0) return;

		StringBuilder raw = new StringBuilder("[");
		for(int i=0; i<hyperMsgs.length; ++i){
			if(i != 0) raw.append(',');
			raw.append(" {\"text\":\"").append(preMsgs[i]).append("\"}, {\"text\":\"")
				.append(hyperMsgs[i]).append("\", \"clickEvent\": {\"action\": \"")
				.append(actions[i].action).append("\", \"value\": \"").append(values[i]).append("\"}}");
		}
		if(postMsg != null && !postMsg.isEmpty()) raw.append(", {\"text\": \"").append(postMsg).append("\"} ");
		raw.append(']');
		for(Player p : recipients){
			Eventials.getPlugin().runCommand("minecraft:tellraw "+p.getName()+' '+raw);
			//p.sendRawMessage(raw);//TODO: Doesn't work! (last checked: 1.12.1)
		}
		Eventials.getPlugin().getLogger().info("Sending tellraw: "+raw);
	}

	public static final char colorSymbol = ChatColor.WHITE.toString().charAt(0);
	static Character[] SET_VALUES = new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
										  'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o', 'r'};
	public static final Set<Character> colorChars = new HashSet<Character>(Arrays.asList(SET_VALUES));
	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate){
		char[] msg = textToTranslate.toCharArray();
		for(int i=1; i<msg.length; ++i){
			if(msg[i-1] == altColorChar && colorChars.contains(msg[i]) && !isEscaped(msg, i-1)){
				msg[i-1] = colorSymbol;
			}
		}
		return new String(msg);
	}

	public static boolean isEscaped(char[] str, int x){
		boolean escaped = false;
		while(x != 0 && str[--x] == '\\') escaped = !escaped;
		return escaped;
	}
	public static boolean isEscaped(String str, int x){
		boolean escaped = false;
		while(x != 0 && str.charAt(--x) == '\\') escaped = !escaped;
		return escaped;
	}

	public static String unescapeString(String str){
		StringBuilder builder = new StringBuilder("");
		boolean unescaped = true;
		for(char c : str.toCharArray()){
			if(c == '\\' && unescaped) unescaped = false;
			else{
				builder.append(c);
				unescaped = true;
			}
		}
		return builder.toString();
	}

	public static String escapeTextActionCodes(String str){
		str = str.replace("\\", "\\\\");//Escape escapes first!
		for(TextAction n : TextAction.values()) str.replace(n.marker, "\\"+n.marker);
		return str;
	}

	public static LinkedList<String> toListFromString(String string){
		LinkedList<String> list = new LinkedList<String>();
		list.addAll(Arrays.asList(string.substring(1, string.lastIndexOf(']')).split(", ")));
		if(list.size() == 1 && list.get(0).isEmpty()) list.clear();
		return list;
	}

	public static String locationToString(Location loc){
		return locationToString(loc, ChatColor.GRAY, ChatColor.DARK_GRAY);}
	public static String locationToString(Location loc, ChatColor coordColor, ChatColor commaColor){
		return new StringBuilder("")
				.append(coordColor).append(String.format("%.2f", loc.getX())).append(commaColor).append(',')
				.append(coordColor).append(String.format("%.2f", loc.getY())).append(commaColor).append(',')
				.append(coordColor).append(String.format("%.2f", loc.getZ()))
			.toString();
	}
}