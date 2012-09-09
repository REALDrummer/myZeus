package REALDrummer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class myZeus extends JavaPlugin implements Listener {
	private static Server server;
	private static ConsoleCommandSender console;
	private List<World> worlds;
	private HashMap<String, ArrayList<String>> weather_change_messages = new HashMap<String, ArrayList<String>>();
	private String[] parameters = null;

	public void onEnable() {
		server = getServer();
		console = server.getConsoleSender();
		worlds = server.getWorlds();
		server.getPluginManager().registerEvents(this, this);
		loadTheWeatherChangeMessages(console);
	}

	public void onDisable() {
		saveTheWeatherChangeMessages(console, true);
	}

	public boolean onCommand(CommandSender sender, Command command, String command_label, String[] my_parameters) {
		parameters = my_parameters;
		if (command_label.equalsIgnoreCase("sun") || command_label.equalsIgnoreCase("happy") || command_label.equalsIgnoreCase("rain")
				|| command_label.equalsIgnoreCase("sad") || command_label.equalsIgnoreCase("snow") || command_label.equalsIgnoreCase("thunderstorm")
				|| command_label.equalsIgnoreCase("storm") || command_label.equalsIgnoreCase("thunder") || command_label.equalsIgnoreCase("lightning")
				|| command_label.equalsIgnoreCase("angry") || command_label.equalsIgnoreCase("anger") || command_label.toLowerCase().startsWith("mad")
				|| command_label.equalsIgnoreCase("rage") || command_label.equalsIgnoreCase("raging") || command_label.equalsIgnoreCase("fury")
				|| command_label.equalsIgnoreCase("furious")) {
			if (!(sender instanceof Player) || sender.hasPermission("myzeus.weather"))
				changeTheWeather(sender, command_label.toLowerCase(), false);
			else
				sender.sendMessage(ChatColor.RED + "You're not Zeus! You can't change the weather!");
			return true;
		} else if ((command_label.equalsIgnoreCase("myZeus") || command_label.equalsIgnoreCase("mZ")) && parameters.length > 0
				&& parameters[0].equalsIgnoreCase("load")) {
			if (!(sender instanceof Player) || sender.hasPermission("myzeus.admin"))
				loadTheWeatherChangeMessages(sender);
			else
				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.AQUA + "/myZeus load" + ChatColor.RED + ".");
			return true;
		} else if ((command_label.equalsIgnoreCase("myZeus") || command_label.equalsIgnoreCase("mZ")) && parameters.length > 0
				&& parameters[0].equalsIgnoreCase("save")) {
			if (!(sender instanceof Player) || sender.hasPermission("myzeus.admin"))
				saveTheWeatherChangeMessages(sender, true);
			else
				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.AQUA + "/myZeus save" + ChatColor.RED + ".");
			return true;
		}
		return false;
	}

	// intra-command methods
	private static String replaceAll(String to_return, String to_change, String to_change_to) {
		int index = 0;
		while (to_return.contains(to_change) && to_return.length() >= index + to_change.length()) {
			if (to_return.substring(index, index + to_change.length()).equals(to_change))
				to_return = to_return.substring(0, index) + to_change_to + to_return.substring(index + to_change.length());
			index++;
		}
		return to_return;
	}

	// listeners
	@EventHandler
	public void displayWeatherChangeMessage(WeatherChangeEvent event) {
		ArrayList<String> weather_messages;
		if (event.getWorld().isThundering())
			weather_messages = weather_change_messages.get("thunderstorm");
		else if (event.getWorld().hasStorm())
			weather_messages = weather_change_messages.get("rain");
		else
			weather_messages = weather_change_messages.get("sun");
		if (weather_messages != null && weather_messages.size() > 0) {
			String message = ChatColor.AQUA + weather_messages.get((int) Math.random() * weather_messages.size());
			if (message.contains("[player]"))
				server.broadcastMessage(message);
			else {
				console.sendMessage(message);
				for (Player player : server.getOnlinePlayers())
					player.sendMessage(replaceAll(message, "[player]", player.getName()));
			}
		}
	}

	@EventHandler
	public void changeWeatherWithChat(PlayerChatEvent event) {
		if (event.getPlayer().hasPermission("myzeus.emoticoncontrol")) {
			String formatted_message = replaceAll(replaceAll(event.getMessage(), "-", ""), " ", "");
			if (formatted_message.endsWith(":)")
					|| formatted_message.endsWith(":D")
					|| formatted_message.endsWith("=)")
					|| formatted_message.endsWith("=D")
					|| (formatted_message.endsWith(")") && (replaceAll(formatted_message, ")", "").endsWith(":") || replaceAll(formatted_message, ")", "")
							.endsWith("=")))
					|| (formatted_message.endsWith("D") && (replaceAll(formatted_message, "D", "").endsWith(":") || replaceAll(formatted_message, "D", "")
							.endsWith("="))) || event.getMessage().toLowerCase().startsWith("let there be light"))
				changeTheWeather(event.getPlayer(), "sun", true);
			else if (formatted_message.endsWith(":(")
					|| formatted_message.endsWith(";(")
					|| formatted_message.endsWith("=(")
					|| formatted_message.endsWith("D:")
					|| formatted_message.endsWith("D;")
					|| formatted_message.endsWith("D=")
					|| (formatted_message.endsWith("(") && (replaceAll(formatted_message, "(", "").endsWith(":")
							|| replaceAll(formatted_message, "(", "").endsWith(";") || replaceAll(formatted_message, "(", "").endsWith("="))))
				changeTheWeather(event.getPlayer(), "rain", true);
			else if (formatted_message.endsWith(">:(")
					|| formatted_message.endsWith(">=(")
					|| formatted_message.endsWith("D:<")
					|| formatted_message.endsWith("D=<")
					|| (formatted_message.endsWith("(") && (replaceAll(formatted_message, "(", "").endsWith(">:")
							|| replaceAll(formatted_message, "(", "").endsWith(";") || replaceAll(formatted_message, "(", "").endsWith(">=")))
					|| (formatted_message.endsWith("<") && (replaceAll(formatted_message, "<", "").endsWith("D:") || replaceAll(formatted_message, "<", "")
							.endsWith("D="))))
				changeTheWeather(event.getPlayer(), "thunderstorm", true);
		}
	}

	// loading
	private void loadTheWeatherChangeMessages(CommandSender sender) {
		ArrayList<String> messages = new ArrayList<String>();
		messages.add("Here comes the sun! Do-do-do-do!");
		weather_change_messages.put("sun", messages);
		messages = new ArrayList<String>();
		messages.add("Rain, rain, go away! Don't come again another day!");
		weather_change_messages.put("rain", messages);
		messages = new ArrayList<String>();
		messages.add("Let's bring the THUNDER!");
		weather_change_messages.put("thunderstorm", messages);
		// TODO Auto-generated method stub
		saveTheWeatherChangeMessages(sender, false);
	}

	// saving
	private void saveTheWeatherChangeMessages(CommandSender sender, boolean display_message) {
		// TODO Auto-generated method stub

	}

	// plugin commands
	private void changeTheWeather(CommandSender sender, String weather, boolean through_chat) {
		if (weather.equalsIgnoreCase("happy"))
			weather = "sun";
		else if (weather.equalsIgnoreCase("sad") || weather.equalsIgnoreCase("snow"))
			weather = "rain";
		else if (weather.equalsIgnoreCase("storm") || weather.equalsIgnoreCase("thunder") || weather.equalsIgnoreCase("lightning")
				|| weather.equalsIgnoreCase("angry") || weather.equalsIgnoreCase("anger") || weather.toLowerCase().startsWith("mad")
				|| weather.equalsIgnoreCase("rage") || weather.equalsIgnoreCase("raging") || weather.equalsIgnoreCase("fury")
				|| weather.equalsIgnoreCase("furious"))
			weather = "thunderstorm";
		// find the target worlds
		ArrayList<World> target_worlds = new ArrayList<World>();
		if (parameters != null && parameters.length > 0) {
			for (World my_world : worlds)
				if (my_world.getWorldFolder().getName().toLowerCase().startsWith(parameters[0].toLowerCase())) {
					target_worlds.add(my_world);
					break;
				}
		} else
			for (World my_world : worlds)
				if (!my_world.getWorldFolder().getName().endsWith("_nether") && !my_world.getWorldFolder().getName().endsWith("_the_end")) {
					target_worlds.add(my_world);
				}
		if (target_worlds.size() > 0) {
			boolean has_broadcasted_message = false;
			for (World world : target_worlds) {
				sender.sendMessage(world.getWorldFolder().getName());
				if (weather.equals("sun")) {
					if (world.hasStorm() || world.isThundering()) {
						world.setStorm(false);
						world.setThundering(false);
						ArrayList<String> messages = weather_change_messages.get(weather);
						if (messages != null && !has_broadcasted_message) {
							server.broadcastMessage(ChatColor.AQUA + messages.get((int) (Math.random() * messages.size())));
							has_broadcasted_message = true;
						}
					} else if (!through_chat && target_worlds.size() == 1)
						sender.sendMessage(ChatColor.RED + "It's already sunny out!");
					else if (!through_chat) {
						boolean is_already_sunny = true;
						for (World my_world : target_worlds)
							if (!my_world.getWorldFolder().getName().endsWith("_nether") && !my_world.getWorldFolder().getName().endsWith("_the_end")
									&& (my_world.isThundering() || my_world.hasStorm()))
								is_already_sunny = false;
						if (is_already_sunny)
							sender.sendMessage(ChatColor.RED + "It's already sunny out!");
					}
				} else if (weather.equals("rain")) {
					if (!world.hasStorm() || world.isThundering()) {
						world.setStorm(true);
						world.setThundering(false);
						ArrayList<String> messages = weather_change_messages.get(weather);
						if (messages != null)
							server.broadcastMessage(ChatColor.AQUA + messages.get((int) (Math.random() * messages.size())));
					} else if (!through_chat)
						sender.sendMessage(ChatColor.RED + "It's already raining!");
					else if (!through_chat) {
						boolean is_already_raining = true;
						for (World my_world : target_worlds)
							if (!my_world.getWorldFolder().getName().endsWith("_nether") && !my_world.getWorldFolder().getName().endsWith("_the_end")
									&& !my_world.hasStorm())
								is_already_raining = false;
						if (is_already_raining)
							sender.sendMessage(ChatColor.RED + "It's already raining!");
					}
				} else {
					if (!world.hasStorm() && !world.isThundering()) {
						world.setStorm(true);
						world.setThundering(true);
						ArrayList<String> messages = weather_change_messages.get(weather);
						if (messages != null)
							server.broadcastMessage(ChatColor.AQUA + messages.get((int) (Math.random() * messages.size())));
					} else if (!through_chat)
						sender.sendMessage(ChatColor.RED + "It's already storming!");
					else if (!through_chat) {
						boolean is_already_storming = true;
						for (World my_world : target_worlds)
							if (!my_world.getWorldFolder().getName().endsWith("_nether") && !my_world.getWorldFolder().getName().endsWith("_the_end")
									&& !my_world.isThundering())
								is_already_storming = false;
						if (is_already_storming)
							sender.sendMessage(ChatColor.RED + "It's already storming!");
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "I can't find \"" + parameters[0] + ".\"");
			if (worlds.size() == 1)
				sender.sendMessage(ChatColor.RED + "Your server has only one world: \"" + worlds.get(0).getWorldFolder().getName() + ".\"");
			else if (worlds.size() == 2)
				sender.sendMessage(ChatColor.RED + "Your server only has two worlds: \"" + worlds.get(0).getWorldFolder().getName() + "\" and \""
						+ worlds.get(0).getWorldFolder().getName() + ".\"");
			else {
				String message = ChatColor.RED + "Your server has " + worlds.size() + " worlds: \"";
				for (int i = 0; i < worlds.size() - 1; i++)
					message = message + worlds.get(i).getWorldFolder().getName() + "\", \"";
				sender.sendMessage(message + " and \"" + worlds.get(worlds.size() - 1).getWorldFolder().getName() + ".\"");
			}
		}
	}
}
