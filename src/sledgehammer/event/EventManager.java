package sledgehammer.event;

/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.ExceptionListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.manager.Manager;
import sledgehammer.module.core.CoreCommandListener;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.Command;
import sledgehammer.util.ChatTags;
import sledgehammer.util.Result;
import zombie.core.logger.LoggerManager;
import zombie.core.raknet.UdpConnection;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

/**
 * Manager class designed to organize EventListeners and execution of Events. 
 * 
 * @author Jab
 */
public class EventManager extends Manager {

	public static final String NAME = "EventManager";

	/**
	 * Map for registered EventListener interfaces.
	 */
	private Map<String, List<EventListener>> mapEventListeners;

	/**
	 * Map for registered CommandListener interfaces.
	 */
	private Map<String, List<CommandListener>> mapCommandListeners;

	/**
	 * List for registered LogListener interfaces.
	 */
	private List<LogListener> listLogListeners;
	
	/**
	 * List for registered ExceptionListneer interfaces.
	 */
	private List<ExceptionListener> listExceptionListeners;

	/**
	 * Main constructor.
	 */
	public EventManager() {
		
		// Initialize Maps.
		mapEventListeners = new HashMap<>();
		mapCommandListeners = new HashMap<>();

		// Initialize Lists.
		listLogListeners = new ArrayList<>();
		listExceptionListeners = new ArrayList<>();

		// Put a wild-card List for the CommandListener interface Map.
		mapCommandListeners.put("*", new ArrayList<CommandListener>());
	}
	
	/**
	 * Method executing the '/help' command.
	 * 
	 * @param command
	 */
	private void help(CommandEvent c) {
		Command command = c.getCommand();
		Player player = command.getPlayer();

		String response = "Commands: " + NEW_LINE + " " + COLOR_WHITE + " ";

		for (List<CommandListener> listListeners : mapCommandListeners.values()) {
			if (listListeners != null) {
				for (CommandListener listener : listListeners) {
					if (listener != null) {
						String[] commands = listener.getCommands();
						if (commands != null) {
							for (String com : listener.getCommands()) {
								if (com != null) {
									String tip = listener.onTooltip(player, new Command(com, null));
									if (tip != null) {
										response += COLOR_LIGHT_GREEN + " " + com + ": " + COLOR_WHITE + " "
												+ listener.onTooltip(player, new Command(com, null)) + COLOR_WHITE + " "
												+ NEW_LINE + " " + NEW_LINE + " ";
									}
								}
							}
						}
					}
				}
			}
		}

		CoreCommandListener coreCommandListener = getSledgeHammer().getModuleManager().getCoreModule().getCommandListener();

		if (coreCommandListener != null) {
			String[] commands = coreCommandListener.getCommands();
			if (commands != null) {
				for (String com : coreCommandListener.getCommands()) {
					if (com != null) {
						String tip = coreCommandListener.onTooltip(player, new Command(com.toLowerCase()));
						if (tip != null) {
							response += COLOR_LIGHT_GREEN + " " + com + ": " + COLOR_WHITE + " "
									+ coreCommandListener.onTooltip(player, new Command(com.toLowerCase())) + COLOR_WHITE + " "
									+ NEW_LINE + " " + NEW_LINE + " ";
						}
					}
				}
			}
		}

		CommandListener vanillaListener = getSledgeHammer().getModuleManager().getVanillaModule().getCommandListener();
		if (vanillaListener != null) {
			String[] commands = vanillaListener.getCommands();
			if (commands != null) {
				for (String com : vanillaListener.getCommands()) {
					if (com != null) {
						String tip = vanillaListener.onTooltip(player, command);
						if (tip != null) {
							response += COLOR_LIGHT_GREEN + " " + com + ": " + COLOR_WHITE + " "
									+ vanillaListener.onTooltip(player, command) + COLOR_WHITE + " "
									+ NEW_LINE + " " + NEW_LINE + " ";
						}
					}
				}
			}
		}

		c.getResponse().set(Result.SUCCESS, response);
	}

	/**
	 * Executes EventListeners from a given Event instance. 
	 * 
	 * This method is a simplified version of:
	 * <code> handleEvent(event, true); </code>, 
	 * 
	 * The Event is logged.
	 * 
	 * @param event
	 * 
	 * @return
	 */
	public Event handleEvent(Event event) {
		return handleEvent(event, true);
	}

	/**
	 * Executes EventListeners from a given Event instance. Logging is optional.
	 * 
	 * @param event
	 * 
	 * @param logEvent
	 * 
	 * @return
	 */
	public Event handleEvent(Event event, boolean logEvent) {
		try {
			if (event == null)
				throw new IllegalArgumentException("Event is null!");

			if (event.getID() == CommandEvent.ID) {
				return (handleCommand((CommandEvent) event, logEvent));
			}

			EventListener coreEventListener = getSledgeHammer().getModuleManager().getCoreModule().getEventListener();
			
			List<EventListener> listEventListeners = mapEventListeners.get(event.getID());
			if (listEventListeners != null) {
				for (EventListener listener : listEventListeners) {
					if(!listener.runSecondary()) {						
						if(listener != coreEventListener) {						
							listener.handleEvent(event);
						}
						if (event.canceled())
							return event;
						if (event.handled())
							break;
					}
				}
				for(EventListener listener : listEventListeners) {
					if(listener.runSecondary()) {						
						if(listener != coreEventListener) {						
							listener.handleEvent(event);
						}
						if (event.canceled())
							return event;
						if (event.handled())
							break;
					}
				}
			}

			// If the Event is set to canceled, return.
			if (event.canceled())
				return event;

			// Force Core Event-handling to be last, for modification potential.
			if (!event.handled() && !event.ignoreCore()) {
				coreEventListener.handleEvent(event);
			}

			// If the Event is set to canceled, return before logging it.
			if (event.canceled())
				return event;

			// Log the Event.
			if (logEvent)
				logEvent(event);

		} catch (Exception e) {
			stackTrace("Error handling event " + event + ": " + e.getMessage(), e);
		}
		return event;
	}
	
	/**
	 * Handles an Exception, passing it to all registered ExceptionListener's, with a reason String given.
	 * 
	 * @param reason
	 * 
	 * @param throwable
	 */
	public void handleException(String reason, Throwable throwable) {
		for (ExceptionListener listener : listExceptionListeners) {
			if (listener != null) {
				listener.onError(reason, throwable);
			}
		}
	}
	
	private void log(LogEvent logEvent) {
		try {
			Event event = logEvent.getEvent();
			// Go through each LogListener interface and fire it.
			for (LogListener listener : listLogListeners) {
				if (listener != null) {					
					listener.onLogEntry(logEvent);
				}
			}

			String log = "SledgeHammer";

			// Grab the ID of the event.
			String eName = event.getID();

			// For organization purposes.
			if (eName.equalsIgnoreCase(ChatEvent.ID)) {
				log += "-CHAT";
			} else if (eName.equalsIgnoreCase(CommandEvent.ID)) {
				log += "-COMMAND";
			}

			// If important, log as such. Else log normally.
			if (logEvent.isImportant()) {
				LoggerManager.getLogger(log).write(logEvent.getLogMessage(), "IMPORTANT");
			} else {
				LoggerManager.getLogger(log).write(logEvent.getLogMessage());
			}
		} catch (Exception e) {
			println("Error logging event " + logEvent.getEvent() + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
	}

	/**
	 * Logs a Event, running through each LogListener interface.
	 * 
	 * @param event
	 * 
	 * @param important
	 */
	public void logEvent(Event event, boolean important) {
		LogEvent logEvent = new LogEvent(event);
		logEvent.setImportant(important);
		log(logEvent);
	}
	
	/**
	 * Logs a Event, running through each LogListener interface.
	 * 
	 * @param event
	 */
	public void logEvent(Event event) {
		LogEvent logEvent = new LogEvent(event);
		log(logEvent);
	}
	
	/**
	 * Handles a command.
	 * 
	 * @param connection
	 * @param input
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input) {
		return handleCommand(connection, input, true);
	}

	/**
	 * Handles a command.
	 * 
	 * @param connection
	 * @param input
	 * @param logEvent
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
		Player player = null;

		// Create a Player instance.
		if (connection == null)
			player = SledgeHammer.getAdmin();
		else
			player = getSledgeHammer().getPlayer(connection.username);

		
		// Create a CommandEvent.
		Command command = new Command(input);
		command.setPlayer(player);
		CommandEvent c = new CommandEvent(command);

		// Fire the CommandEvent handle method, and return its result.
		return handleCommand(c, logEvent);
	}

	/**
	 * Handles a CommandEvent.
	 * 
	 * @param c
	 * @param logEvent
	 * @return
	 */
	public CommandEvent handleCommand(CommandEvent c, boolean logEvent) {
		synchronized (this) {
			try {
				Command com = c.getCommand();
				String command = com.getCommand();

				// If '/help' is fired.
				if (command.equalsIgnoreCase("help")) {
					help(c);
					return c;
				}
//				if (player.getUsername().equalsIgnoreCase("admin")) {
//					if (command.equalsIgnoreCase("emulate")) {
//						String[] args = com.getArguments();
//						if (args.length > 1) {
//							String name = args[0];
//
//							String commandString = com.getRaw();
//							commandString = commandString.split(name)[1].trim();
//
//							Player playerEmulated = SledgeHammer.instance.getPlayer(name);
//							
//							Command com2 = new Command(commandString);
//							com2.setPlayer(playerEmulated);
//							CommandEvent event = new CommandEvent(com2);
//							handleCommand(event, logEvent);
//							
//							event.getResponse().set(event.getResponse().getResult(), ChatTags.stripTags(event.getResponse().getResponse(), true));
//							return event;
//						} else {
//							return c;
//						}
//					}
//				}

				// Run through selected command listeners first (Optimization).
				List<CommandListener> listListeners = mapCommandListeners.get(c.getCommand().getCommand().toLowerCase());

				if (listListeners != null) {
					for (CommandListener listener : listListeners) {
						// If the listener is not null, fire the
						// CommandListener.
						if (listener != null) {							
							listener.onCommand(c.getCommand(), c.getResponse());
						}

						// If the listener set the command as handled, break the
						// loop.
						if (c.handled()) {							
							break;
						}
					}
				}
				
				// Check native command handlers if the command is not handled.
				if(!c.isHandled()) {
					SledgeHammer sledgeHammer = getSledgeHammer();
					
					CommandListener vanillaListener = sledgeHammer.getModuleManager().getVanillaModule().getCommandListener();
					vanillaListener.onCommand(c.getCommand(), c.getResponse());
					
					if(!c.isHandled()) {						
						CoreCommandListener coreCommandListener = sledgeHammer.getModuleManager().getCoreModule().getCommandListener();
						coreCommandListener.onCommand(c.getCommand(), c.getResponse());
					}
				}

				if (logEvent) {
					// Iterate the log listeners after the command.
					if (c.getResponse().getLogMessage() != null) {
						LogEvent entry = new LogEvent(c);
						for (LogListener listener : listLogListeners) {
							if (listener != null) {
								listener.onLogEntry(entry);
							}
						}
					}
				}

				// For console commands, or other methods outside of the game,
				// this strips the color codes, and replaces '<LINE>' with \n.
				if (c.getCommand().getPlayer().getConnection() == null) {
					c.getResponse().set(c.getResponse().getResult(), ChatTags.stripTags(c.getResponse().getResponse(), true));
				}
				
			} catch (Exception e) {
				println("Error handling command " + c + ": " + e.getMessage());
				for (StackTraceElement o : e.getStackTrace()) {
					println(o);
				}
			}
		}
		return c;
	}

	/**
	 * Registers an EventListener interface, with a Event ID, given as a String.
	 * 
	 * @param type
	 * 
	 * @param listener
	 */
	public void registerEventListener(String type, EventListener listener) {

		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		List<EventListener> listListeners = mapEventListeners.get(type);
		if (listListeners == null) {
			listListeners = new ArrayList<>();
			mapEventListeners.put(type, listListeners);
			listListeners.add(listener);
		} else {
			listListeners.add(listener);
		}
	}

	/**
	 * Registers an EventListener interface, with all Event IDs listed in the
	 * interface as String[] getTypes().
	 * 
	 * @param listener
	 */
	public void registerEventListener(EventListener listener) {

		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		String[] types = listener.getTypes();
		if (types == null)
			throw new IllegalArgumentException("listener.getTypes() array is null!");
		for (String type : types) {
			registerEventListener(type, listener);
		}

	}

	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * 
	 * @param command
	 * 
	 * @param listener
	 */
	public void registerCommandListener(String command, CommandListener listener) {

		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");

		command = command.toLowerCase();

		List<CommandListener> listListeners = mapCommandListeners.get(command);
		if (listListeners == null) {
			listListeners = new ArrayList<>();
			mapCommandListeners.put(command, listListeners);
			listListeners.add(listener);
		} else {
			listListeners.add(listener);
		}
	}
	
	/**
	 * Unregisters a given CommandListener.
	 * 
	 * @param command
	 * 
	 * @param listener
	 */
	public void unregisterCommandListener(String command, CommandListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		// Grab the list associated with the command.
		List<CommandListener> list = mapCommandListeners.get(command);
		
		// If the list exists, tell the list to remove the CommandListener.
		if(list != null) list.remove(listener);

	}

	/**
	 * Registers a LogListener interface.
	 * 
	 * @param listener
	 */
	public void registerLogListener(LogListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		// Make sure that the listener isn't registered twice.
		if(!listLogListeners.contains(listener)) {			
			listLogListeners.add(listener);
		}
	}
	
	/**
	 * Registers a ExceptionListener interface.
	 * 
	 * @param listener
	 */
	public void registerExceptionListener(ExceptionListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		// Make sure that the listener isn't registered twice.
		if(!listExceptionListeners.contains(listener)) {			
			listExceptionListeners.add(listener);
		}
	}
	
	/**
	 * Unregisters a given EventListener.
	 * 
	 * @param listener
	 */
	public void unregister(EventListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		String[] types = listener.getTypes();
		
		// Make sure the Listener 
		if(types == null || types.length == 0) throw new IllegalArgumentException("Listener does not have defined types!");
		
		// Go through each type registered with the listener.
		for (String type : listener.getTypes()) {			
			
			// Grab the list holding the listener.
			List<EventListener> list = mapEventListeners.get(type);
			
			// If the list is valid, remove the listener from the List.
			if(list != null) {				
				list.remove(listener);
			}
			
		}
	}

	/**
	 * Unregisters a given CommandListener.
	 * 
	 * @param listener
	 */
	public void unregister(CommandListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		for(String command : listener.getCommands()) {
			mapCommandListeners.get(command).remove(listener);
		}
	}

	/**
	 * 
	 * @param listener
	 */
	public void unregister(LogListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		listLogListeners.remove(listener);
	}

	/**
	 * 
	 * @param listener
	 */
	public void unregister(ExceptionListener listener) {
		
		// Make sure the listener instance given is valid.
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		
		listExceptionListeners.remove(listener);
	}

	/**
	 * Returns the map of EventListener interfaces registered.
	 * 
	 * @return
	 */
	public Map<String, List<EventListener>> getEventListeners() {
		return this.mapEventListeners;
	}

	/**
	 * Returns the List of registered LogListeners.
	 * 
	 * @return
	 */
	public List<LogListener> getLogListeners() {
		return this.listLogListeners;
	}
	
	/**
	 * Returns the List of registered ExceptionListeners.
	 * 
	 * @return
	 */
	public List<ExceptionListener> getExceptionListeners() {
		return this.listExceptionListeners;
	}

	/**
	 * Returns the Map of registered CommandListeners, organized as Lists with the given command as a key, in lowercase.
	 * 
	 * @return
	 */
	public Map<String, List<CommandListener>> getCommandListeners() {
		return this.mapCommandListeners;
	}

	@Override
	public String getName() { return NAME; }

	@Override
	public void onLoad(boolean debug) {}

	@Override
	public void onStart() {}

	@Override
	public void onUpdate() {}

	@Override
	public void onShutDown() {}
	
}