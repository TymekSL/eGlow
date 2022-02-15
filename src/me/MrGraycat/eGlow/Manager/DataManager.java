package me.MrGraycat.eGlow.Manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.API.Event.GlowColorChangeEvent;
import me.MrGraycat.eGlow.Config.EGlowCustomEffectsConfig.Effect;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class DataManager implements PluginMessageListener {	
	private static Map<String, IEGlowPlayer> dataPlayers = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, IEGlowEffect> dataEffects = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, IEGlowEffect> dataCustomEffects = new ConcurrentHashMap<>();
	
	//Server
	public static void initialize() {
		addEGlowEffects();
		EGlow.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(EGlow.getInstance(), "eglow:bungee");
		EGlow.getInstance().getServer().getMessenger().registerIncomingPluginChannel(EGlow.getInstance(), "eglow:bungee", new DataManager());
	}
	
	//Entities
	public static IEGlowPlayer addEGlowPlayer(Player player, String UUID) {
		if (!dataPlayers.containsKey(UUID))
			dataPlayers.put(UUID, new IEGlowPlayer(player));
		return dataPlayers.get(UUID);
	}
	
	public static IEGlowPlayer getEGlowPlayer(Player player) {
		return (dataPlayers.containsKey(player.getUniqueId().toString())) ? dataPlayers.get(player.getUniqueId().toString()) : null;
	}
	
	public static IEGlowPlayer getEGlowPlayer(String name) {
		Player player = Bukkit.getPlayer(name);
		
		//Prevent kick loop caused by for example npc's
		if (player == null)
			return null;
		return (dataPlayers.containsKey(player.getUniqueId().toString())) ? dataPlayers.get(player.getUniqueId().toString()) : null;
	}
	
	public static Collection<IEGlowPlayer> getEGlowPlayers() {
		return dataPlayers.values();
	}
	
	public static void removeEGlowPlayer(Player p) {
		dataPlayers.remove(p.getUniqueId().toString());
	}
	
	//Effects
	public static void addEGlowEffects() {
		IEGlowEffect effect;
		
		for (ChatColor color : ChatColor.values()) {
			if (!color.equals(ChatColor.ITALIC) && !color.equals(ChatColor.MAGIC) && !color.equals(ChatColor.STRIKETHROUGH) && !color.equals(ChatColor.UNDERLINE) && ! color.equals(ChatColor.BOLD)) {
				String configName = color.name().toLowerCase().replace("_", "-").replace("ı", "i").replace("dark-purple", "purple").replace("light-purple", "pink").replace("reset", "none").replace(" ", "");
				String name = configName.replace("-", "");
				
				if (!dataEffects.containsKey(name)) {
					addEGlowEffect(name, (EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName), "eglow.color." + name, color);
					
					addEGlowEffect("blink" + name + "slow", (EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName) + " §f(" + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get("slow") + "§f)", "eglow.blink." + name, EGlowMainConfig.getPlayerSlowDelay(), ChatColor.RESET, color);
					addEGlowEffect("blink" + name + "fast", (EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName) + " §f(" + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get("fast") + "§f)", "eglow.blink." + name, EGlowMainConfig.getPlayerFastDelay(), ChatColor.RESET, color);
				} else {
					effect = getEGlowEffect(name);
					
					if (effect != null)
						effect.setDisplayName((EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName));
					
					effect = getEGlowEffect("blink" + name + "slow");
					
					if (effect != null) {
						effect.setDisplayName((EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName) + " §f(" + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get("slow") + "§f)");
						effect.setDelay(EGlowMainConfig.getPlayerSlowDelay());
						effect.reloadEffect();
					}
					
					effect = getEGlowEffect("blink" + name + "fast");
					
					if (effect != null) {
						effect.setDisplayName((EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get(configName) : Message.COLOR.get(configName) + " §f(" + Message.COLOR.get("effect-blink") + " " + Message.COLOR.get("fast") + "§f)");
						effect.setDelay(EGlowMainConfig.getPlayerFastDelay());
						effect.reloadEffect();
					}
				}
			}
		}
		
		if (!dataEffects.containsKey("rainbowslow")) {
			addEGlowEffect("rainbowslow", (EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get("effect-rainbow") : Message.COLOR.get("effect-rainbow") + " §f(" + Message.COLOR.get("slow") + "§f)", "eglow.effect.rainbow", EGlowMainConfig.getPlayerSlowDelay(), ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE);
			addEGlowEffect("rainbowfast", (EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get("effect-rainbow") : Message.COLOR.get("effect-rainbow") + " §f(" + Message.COLOR.get("fast") + "§f)", "eglow.effect.rainbow", EGlowMainConfig.getPlayerFastDelay(), ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE);
		} else {
			effect = getEGlowEffect("rainbowslow");
			
			if (effect != null) {
				effect.setDisplayName((EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get("effect-rainbow") : Message.COLOR.get("effect-rainbow") + " §f(" + Message.COLOR.get("slow") + "§f)");
				effect.setDelay(EGlowMainConfig.getPlayerSlowDelay());
				effect.reloadEffect();
			}
			
			effect = getEGlowEffect("rainbowfast");
			
			if (effect != null) {
				effect.setDisplayName((EGlowMainConfig.OptionUseGUIColorAsChatColor()) ? Message.GUI_COLOR.get("effect-rainbow") : Message.COLOR.get("effect-rainbow") + " §f(" + Message.COLOR.get("fast") + "§f)");
				effect.setDelay(EGlowMainConfig.getPlayerFastDelay());
				effect.reloadEffect();
			}
		}
		
		addCustomEffects();
	}

	private static ArrayList<String> oldEffects = new ArrayList<>();

	public static void addCustomEffects() {
		ArrayList<String> newEffects = new ArrayList<>();
		
		for (String effectName : Effect.GET_ALL_EFFECTS.get()) {
			if (dataEffects.containsKey(effectName.toLowerCase())) {
				ChatUtil.sendToConsoleWithPrefix("&cWARNING! Not registering custom effect: &f" + effectName + " &cdue to it using a default effect name!");
				continue;
			}
			
			String displayName = ChatUtil.translateColors(Effect.GET_NAME.getString(effectName));
			int delay = (int) (Effect.GET_DELAY.getDouble(effectName) * 20);
			List<String> colors = Effect.GET_COLORS.getList(effectName);
			String permission = "eglow.effect." + effectName.toLowerCase();
			
			if (!oldEffects.isEmpty() && oldEffects.contains(effectName.toLowerCase())) {
				IEGlowEffect effect = getEGlowEffect(effectName.toLowerCase());
				
				if (effect != null) {
					effect.setDisplayName(displayName);
					effect.setDelay(delay);
					effect.setColors(colors);
				}
				oldEffects.remove(effectName.toLowerCase());
				EGlow.getInstance().getServer().getPluginManager().removePermission(permission);
			} else {
				addEGlowEffect(effectName.toLowerCase(), displayName, "eglow.effect." + effectName.toLowerCase(), delay, colors);
				try {EGlow.getInstance().getServer().getPluginManager().addPermission(new Permission(permission, "Activate " + effectName + " effect.", PermissionDefault.FALSE));} catch (IllegalArgumentException e) {}//Permission already registered ;)}
			}
			
			newEffects.add(effectName.toLowerCase());
		}
		
		//Removing removed effects
		if (!oldEffects.isEmpty()) {
			for (String effect : oldEffects) {
				IEGlowEffect Eeffect = getEGlowEffect(effect.toLowerCase());
				
				if (Eeffect != null) {
					removeCustomEGlowEffect(effect.toLowerCase());
					Eeffect.removeEffect();
				}
			}
		}
		
		oldEffects = newEffects;
	}
	
	private static void addEGlowEffect(String name, String displayName, String permissionNode, ChatColor color) {
		if (!dataEffects.containsKey(name.toLowerCase()))
			dataEffects.put(name.toLowerCase(), new IEGlowEffect(name, displayName, permissionNode, 0, color));
	}
	
	private static void addEGlowEffect(String name, String displayName, String permissionNode, int delay, ChatColor... colors) {
		if (!dataEffects.containsKey(name.toLowerCase()))
			dataEffects.put(name.toLowerCase(), new IEGlowEffect(name, displayName, permissionNode, delay, colors));
	}
	
	private static void addEGlowEffect(String name, String displayName, String permissionNode, int delay, List<String> colors) {
		for (String color : colors) {
			try {
				color = color.toLowerCase().replace("dark", "dark_").replace("light", "_light").replace("purple", "dark_purple").replace("pink", "light_purple").replace("none", "reset");
				ChatColor.valueOf(color.toUpperCase());
			} catch (IllegalArgumentException | NullPointerException e) {
				ChatUtil.sendToConsoleWithPrefix("&cInvalid color &f'&e" + color + "&f' &cfor effect &f'&e" + name + "&f'");
				return;
			}
		}
		
		if (!dataCustomEffects.containsKey(name.toLowerCase()))
			dataCustomEffects.put(name.toLowerCase(), new IEGlowEffect(name, displayName, permissionNode, delay, colors));
	}
	
	public static List<IEGlowEffect> getEGlowEffects() {
		List<IEGlowEffect> effects = new ArrayList<IEGlowEffect>();
		
		dataEffects.forEach((key, value) -> { effects.add(value); });
		return effects;
	}
	
	public static List<IEGlowEffect> getCustomEffects() {
		List<IEGlowEffect> effects = new ArrayList<IEGlowEffect>();
		
		dataCustomEffects.forEach((key, value) -> { effects.add(value); });
		return effects;
	}
	
	public static boolean isValidEffect(String name, boolean containsSpeed) {
		return (containsSpeed) ? (dataEffects.containsKey(name.toLowerCase()) || dataCustomEffects.containsKey(name.toLowerCase())) : (dataEffects.containsKey(name.toLowerCase() + "slow") && dataEffects.containsKey(name.toLowerCase() + "fast"));
	}
	
	public static boolean isValidSpeed(String speed) {
		speed = speed.toLowerCase();
		
		return (speed.equals("slow") || speed.equals("fast")) ? true : false;
	}
	
	public static boolean isCustomEffect(String name) {
		return (dataCustomEffects.containsKey(name)) ? true : false;
	}
	
	public static IEGlowEffect getEGlowEffect(String name) {
		if (dataEffects.containsKey(name.toLowerCase()))
			return dataEffects.get(name.toLowerCase());
		if (dataCustomEffects.containsKey(name.toLowerCase()))
			return dataCustomEffects.get(name.toLowerCase());
		return null;
	}
	
	private static void removeCustomEGlowEffect(String name) {
		if (dataCustomEffects.containsKey(name.toLowerCase()))
			dataCustomEffects.remove(name.toLowerCase());
	}
	
	//API
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] msg) {
		
	}
	
	public static void TABProxyUpdateRequest(Player player, String glowColor) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("TABProxyUpdateRequest");
		out.writeUTF(player.getUniqueId().toString());
		out.writeUTF(glowColor);
		Bukkit.getServer().sendPluginMessage(EGlow.getInstance(), "eglow:bungee", out.toByteArray());
	}
	
	public static void sendAPIEvent(IEGlowPlayer player, boolean fake) {
		if (fake) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.getPluginManager().callEvent(new GlowColorChangeEvent(player.getPlayer(), player.getUUID(), player.getActiveColor(), player.getGlowStatus()));	
			}
		}.runTask(EGlow.getInstance());
	}
}
