package REALDrummer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Timer;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class myZeus extends JavaPlugin implements Listener, ActionListener {
	private static Server server;
	private static ConsoleCommandSender console;
	private List<World> worlds;
	private World world_with_weather = null;
	private HashMap<String, ArrayList<String>> weather_change_messages = new HashMap<String, ArrayList<String>>();
	private String[] parameters = null;
	private Timer timer = new Timer(1000, this);

	// plugin enable/disable and the command operator
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

	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] my_parameters) {
		parameters = my_parameters;
		if (command.equalsIgnoreCase("sun") || command.equalsIgnoreCase("happy") || command.equalsIgnoreCase("rain") || command.equalsIgnoreCase("sad")
				|| command.equalsIgnoreCase("snow") || command.equalsIgnoreCase("thunderstorm") || command.equalsIgnoreCase("storm") || command.equalsIgnoreCase("thunder")
				|| command.equalsIgnoreCase("lightning") || command.equalsIgnoreCase("angry") || command.equalsIgnoreCase("anger") || command.toLowerCase().startsWith("mad")
				|| command.equalsIgnoreCase("rage") || command.equalsIgnoreCase("raging") || command.equalsIgnoreCase("fury") || command.equalsIgnoreCase("furious")) {
			if (!(sender instanceof Player) || sender.hasPermission("myzeus.weather"))
				changeTheWeather(sender, command.toLowerCase(), false);
			else
				sender.sendMessage(ChatColor.RED + "You're not Zeus! You can't change the weather!");
			return true;
		} else if ((command.equalsIgnoreCase("myZeus") || command.equalsIgnoreCase("mZ")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("load")) {
			if (!(sender instanceof Player) || sender.hasPermission("myzeus.admin"))
				loadTheWeatherChangeMessages(sender);
			else
				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.AQUA + "/myZeus load" + ChatColor.RED + ".");
			return true;
		} else if ((command.equalsIgnoreCase("myZeus") || command.equalsIgnoreCase("mZ")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("save")) {
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
		if (!to_return.toLowerCase().contains(to_change.toLowerCase()))
			return to_return;
		for (int i = 0; to_return.length() >= i + to_change.length(); i++) {
			if (to_return.substring(i, i + to_change.length()).equalsIgnoreCase(to_change)) {
				to_return = to_return.substring(0, i) + to_change_to + to_return.substring(i + to_change.length());
				i = i + to_change_to.length() - 1;
			}
			if (!to_return.toLowerCase().contains(to_change.toLowerCase()))
				break;
		}
		return to_return;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		ArrayList<String> weather_messages;
		if (world_with_weather.isThundering())
			weather_messages = weather_change_messages.get("thunderstorm");
		else if (world_with_weather.hasStorm())
			weather_messages = weather_change_messages.get("rain");
		else
			weather_messages = weather_change_messages.get("sun");
		if (weather_messages != null && weather_messages.size() > 0) {
			String message = ChatColor.AQUA + weather_messages.get((int) Math.random() * weather_messages.size());
			if (!message.toLowerCase().contains("[player]"))
				server.broadcastMessage(message);
			else {
				console.sendMessage(message);
				for (Player player : server.getOnlinePlayers())
					player.sendMessage(replaceAll(message, "[player]", player.getName()));
			}
		}
		timer.stop();
	}

	// listeners
	@EventHandler
	public void displayWeatherChangeMessage(WeatherChangeEvent event) {
		world_with_weather = event.getWorld();
		timer.start();
	}

	@EventHandler
	public void changeWeatherWithChat(AsyncPlayerChatEvent event) {
		if (event.getPlayer().hasPermission("myzeus.emoticoncontrol")) {
			String formatted_message = replaceAll(replaceAll(event.getMessage(), "-", ""), " ", "");
			if (formatted_message.endsWith(":)") || formatted_message.endsWith(":D") || formatted_message.endsWith("=)") || formatted_message.endsWith("=D")
					|| (formatted_message.endsWith(")") && (replaceAll(formatted_message, ")", "").endsWith(":") || replaceAll(formatted_message, ")", "").endsWith("=")))
					|| (formatted_message.endsWith("D") && (replaceAll(formatted_message, "D", "").endsWith(":") || replaceAll(formatted_message, "D", "").endsWith("=")))
					|| event.getMessage().toLowerCase().startsWith("let there be light"))
				changeTheWeather(event.getPlayer(), "sun", true);
			else if (formatted_message.endsWith(":(")
					|| formatted_message.endsWith(";(")
					|| formatted_message.endsWith("=(")
					|| formatted_message.endsWith("D:")
					|| formatted_message.endsWith("D;")
					|| formatted_message.endsWith("D=")
					|| (formatted_message.endsWith("(") && (replaceAll(formatted_message, "(", "").endsWith(":") || replaceAll(formatted_message, "(", "").endsWith(";") || replaceAll(
							formatted_message, "(", "").endsWith("="))))
				changeTheWeather(event.getPlayer(), "rain", true);
			else if (formatted_message.endsWith(">:(")
					|| formatted_message.endsWith(">=(")
					|| formatted_message.endsWith("D:<")
					|| formatted_message.endsWith("D=<")
					|| (formatted_message.endsWith("(") && (replaceAll(formatted_message, "(", "").endsWith(">:") || replaceAll(formatted_message, "(", "").endsWith(";") || replaceAll(
							formatted_message, "(", "").endsWith(">=")))
					|| (formatted_message.endsWith("<") && (replaceAll(formatted_message, "<", "").endsWith("D:") || replaceAll(formatted_message, "<", "").endsWith("D="))))
				changeTheWeather(event.getPlayer(), "thunderstorm", true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void cancelLightningFires(BlockIgniteEvent event) {
		if (event.getCause() == IgniteCause.LIGHTNING)
			event.setCancelled(true);
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
		else if (weather.equalsIgnoreCase("storm") || weather.equalsIgnoreCase("thunder") || weather.equalsIgnoreCase("lightning") || weather.equalsIgnoreCase("angry")
				|| weather.equalsIgnoreCase("anger") || weather.toLowerCase().startsWith("mad") || weather.equalsIgnoreCase("rage") || weather.equalsIgnoreCase("raging")
				|| weather.equalsIgnoreCase("fury") || weather.equalsIgnoreCase("furious"))
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
		if (target_worlds.size() > 0)
			for (World world : target_worlds) {
				if (weather.equals("sun")) {
					if (world.hasStorm() || world.isThundering()) {
						world.setStorm(false);
						world.setThundering(false);
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
					} else if (!through_chat)
						sender.sendMessage(ChatColor.RED + "It's already raining!");
					else if (!through_chat) {
						boolean is_already_raining = true;
						for (World my_world : target_worlds)
							if (!my_world.getWorldFolder().getName().endsWith("_nether") && !my_world.getWorldFolder().getName().endsWith("_the_end") && !my_world.hasStorm())
								is_already_raining = false;
						if (is_already_raining)
							sender.sendMessage(ChatColor.RED + "It's already raining!");
					}
				} else {
					if (!world.hasStorm() && !world.isThundering()) {
						world.setStorm(true);
						world.setThundering(true);
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
		else {
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
