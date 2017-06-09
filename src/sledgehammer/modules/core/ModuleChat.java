package sledgehammer.modules.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.event.ChatMessageEvent;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.CommandEvent;
import sledgehammer.event.RequestChannelsEvent;
import sledgehammer.manager.ChatManager;
import sledgehammer.module.SQLModule;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessage;
import sledgehammer.objects.chat.ChatMessagePlayer;
import sledgehammer.objects.chat.Command;
import sledgehammer.requests.RequestChatChannels;
import zombie.Lua.LuaManager;

public class ModuleChat extends SQLModule {

	public static final String ID      = "ModuleChat";
	public static final String NAME    = "Chat";
	public static final String MODULE  = "core.chat";
	public static final String VERSION = "1.00";
	
	private static final String TABLE_CHANNELS = "sledgehammer_channels";
	private static final String TABLE_MESSAGES = "sledgehammer_messages";
	
	public ModuleChat() {}
	
	public void onLoad() {
		// Establish the SQLite database connection.
		establishConnection("sledgehammer_chat");
		validateTables();
	}

	public void validateTables() {
		Statement statement;
		try {
			statement = createStatement();
			statement.executeUpdate("create table if not exists " + TABLE_CHANNELS + " (name TEXT, description TEXT, context TEXT, public BOOL);");
			statement.executeUpdate("create table if not exists " + TABLE_MESSAGES 
					+ " (id BIGINT, origin TEXT, channel TEXT, message TEXT, message_original TEXT, edited BOOL, editor_id INTEGER, deleted BOOL, deleter_id INTEGER, modified_timestamp TEXT, player_id INTEGER, player_name TEXT, time TEXT, type INTEGER);");
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void onStart() {
		
		ChatChannel global = new ChatChannel("Global");
		ChatChannel local = new ChatChannel("Local");
		ChatChannel pms = new ChatChannel("PMs");
		
		global.setPublic(true);
		
		addChannel(global);
		addChannel(local);
		addChannel(pms);
	}
	
	public void addChannel(ChatChannel channel) {
		loadChannel(channel);
		getManager().addChatChannel(channel);
	}
	
	
	public void loadChannel(ChatChannel channel) {
		PreparedStatement pStatement;
		ResultSet set;
		Statement statement;
		try {
			statement = createStatement();
			statement.executeUpdate("create table if not exists " + "sledgehammer_channel_" + channel.getChannelName() + "_history (message_id INTEGER, time_added BIGINT);");
			
			pStatement = prepareStatement("SELECT * from " + TABLE_CHANNELS + " WHERE name = \"" + channel.getChannelName() + "\";");
			set = pStatement.executeQuery();
			if(set.next()) {
				// Grab the stored definitions of the ChatChannel.
				String _desc = set.getString("description");
				String _cont = set.getString("context");
				
				// Apply definitions.
				channel.setDescription(_desc);
				channel.setContext(_cont);
				
				// Grab channel history (If any).
				getChannelHistory(channel, 32);
				
			} else {
				// No definitions or history for channels being created.
				statement.executeUpdate("INSERT INTO " + TABLE_CHANNELS + " (name, description, context, public) VALUES (\"" + channel.getChannelName() + "\",\"\",\"" + channel.getContext() + "\",\"" + channel.isPublic() + "\");");
			}

			// Close streams.
			set.close();
			pStatement.close();
			statement.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ChatManager getManager() {
		return SledgeHammer.instance.getChatManager();
	}
	
	public void onUpdate(long delta) {}
	public void onStop() {}
	public void onUnload() {}


	public void onClientCommand(ClientEvent event) {
		String command = event.getCommand();

		if(command.equalsIgnoreCase("getChatChannels")) {
	
			
			Player player = event.getPlayer();
			
			// Send request for channels before dispatching.
			RequestChannelsEvent requestEvent = new RequestChannelsEvent(player);
			SledgeHammer.instance.handle(requestEvent);
			
			List<ChatChannel> channels = SledgeHammer.instance.getChatManager().getChannelsForPlayer(player);
			RequestChatChannels request = new RequestChatChannels();
			
			for(ChatChannel channel : channels) {
				request.addChannel(channel);
			}
			
			event.respond(request);
		} else if(command.equalsIgnoreCase("sendChatMessagePlayer")) {
			// Get the arguments.
			KahluaTable table = event.getTable();
			KahluaTable tableMessage = (KahluaTable) table.rawget("message");
			ChatMessagePlayer message = new ChatMessagePlayer(tableMessage, System.nanoTime());
			message.setTime(LuaManager.getHourMinuteJava());
			saveMessage(message);
			
			String channelName = (String) tableMessage.rawget("channel");
			
			println("ChannelName: " + channelName);
			
			ChatChannel channel = SledgeHammer.instance.getChatManager().getChannel(channelName);
			if(channel == null) {
				errorln("Channel does not exist: \"" + channelName + "\".");
				return;
			}
			
			message.setChannel(channelName);
			channel.addMessage(message);
			
			ChatMessageEvent e = new ChatMessageEvent(message);
			SledgeHammer.instance.handle(e);
		}
	}
	
	public void getChannelHistory(ChatChannel channel, int length) {
		//TODO: Implement.
		PreparedStatement statement;
		ResultSet set;
		try {
			String sql = "SELECT * FROM " + TABLE_MESSAGES + " WHERE channel = \"" + channel.getChannelName().toLowerCase() + "\" ORDER BY id DESC LIMIT " + length + ";";
			statement = prepareStatement(sql);
			set = statement.executeQuery();
			
			List<ChatMessage> listMessages = new LinkedList<>();
			
			while(set.next()) {
				long _messageID = set.getLong("id");
				int _playerID = set.getInt("player_id");
				ChatMessage message = getManager().getMessageFromCache(_messageID);
				if(message == null) {
					
					String _channel         = set.getString("Channel");
					String _message         = set.getString("message");
					String _messageOriginal = set.getString("message_original");
					String _playerName      = set.getString("player_name");
					String _time            = set.getString("time");
					String _origin          = set.getString("origin");
					int type                = set.getInt("type");
					long _modifiedTimestamp = set.getLong("modified_timestamp");
					boolean _edited         = set.getBoolean("edited");
					int _editorID           = set.getInt("editor_id");
					boolean _deleted        = set.getBoolean("deleted");
					int _deleterID          = set.getInt("deleter_id");
					
					if (type == 1) {
						message = new ChatMessagePlayer(_messageID, _channel, _message, _messageOriginal, _edited,
								_editorID, _deleted, _deleterID, _modifiedTimestamp, _time, _playerID, _playerName);
						message.setOrigin(_origin);
					} else {
						message = new ChatMessage(_messageID, _channel, _message, _messageOriginal, _edited, _editorID,
								_deleted, _deleterID, _modifiedTimestamp, _time);
						message.setOrigin(_origin);
					}
					
					getManager().addMessageToCache(message);
				}
				
				listMessages.add(message);
			}
			
			int size = listMessages.size();
			
			for(int index = 0; index < size; index++) {				
				channel.addMessage(listMessages.get(index));
			}
			
			set.close();
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveMessage(ChatMessage message) {
		Statement statement;
		try {
			long messageID = message.getMessageID();
			String origin = message.getOrigin();
			int type = 0;
			
			ChatChannel channel = getManager().getChannel(message.getChannel());
			if(channel == null) {
				println("Message has no channel: " + message.getChannel());
				return;
			}
			
			int playerID = -1;
			String playerName = "";
			
			if(message instanceof ChatMessagePlayer) {
				type = 1;
				ChatMessagePlayer mPlayer = (ChatMessagePlayer)message;
				playerID = mPlayer.getPlayer().getID();
				playerName = mPlayer.getPlayer().getNickname();
			}
			
			String sql = "";
			
			// Upsert the message into the database.
			if (has(TABLE_MESSAGES, "id", "" + messageID)) {
				sql = "UPDATE " + TABLE_MESSAGES 
					+ " SET "
						+ "origin = \"" + origin + "\" AND "
						+ "channel = \"" + message.getChannel() + "\" AND "
						+ "message = \"" + message.getMessage() + "\" AND "
						+ "message_original = \"" + message.getOriginalMessage() + "\" AND "
						+ "edited = \"" + message.isEdited() + "\" AND "
						+ "editor_id = \"" + message.getEditorID() + "\" AND "
						+ "deleted = \"" + message.isDeleted() + "\" AND "
						+ "deleter_id = \"" + message.getDeleterID() + "\" AND "
						+ "modified_timestamp = \"" + message.getModifiedTimestamp() + "\" AND "
						+ "player_id = \"" + playerID + "\" AND "
						+ "player_name = \"" + playerName + "\" AND"
						+ "time = \"" + message.getTime() + "\" AND"
						+ "type = \"" + type + "\" "
					+ "WHERE id = " + message.getMessageID() + "\";";
			
			} else {
				sql = "INSERT INTO " + TABLE_MESSAGES
						+ " (id, origin, channel, message, message_original, edited, editor_id, deleted, deleter_id, modified_timestamp, player_id, player_name, time, type) "
						+ "VALUES ("
							+ "\"" + messageID + "\","
							+ "\"" + origin + "\","
							+ "\"" + message.getChannel() + "\","
							+ "\"" + message.getMessage() + "\","
							+ "\"" + message.getOriginalMessage() + "\","
							+ "\"" + message.isEdited() + "\","
							+ "\"" + message.getEditorID() + "\","
							+ "\"" + message.isDeleted() + "\","
							+ "\"" + message.getDeleterID() + "\","
							+ "\"" + message.getModifiedTimestamp() + "\","
							+ "\"" + playerID + "\","
							+ "\"" + playerName + "\","
							+ "\"" + message.getTime() + "\","
							+ "\"" + type + "\""
						+ ");";
			}

			statement = createStatement();
			statement.executeUpdate(sql);
			
			// Update the channel history.
			String tableHistory = "sledgehammer_channel_" + channel.getChannelName() + "_history";
			if(!has(tableHistory, "message_id", "" + messageID)) {
				sql = "INSERT INTO " + tableHistory + " (message_id, time_added) VALUES (\"" + messageID + "\", \"" + System.currentTimeMillis() + "\");";
				statement.executeUpdate(sql);
			}
			statement.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void loadAllChannels() {
		PreparedStatement statement;
		ResultSet set;
		try {
			statement = prepareStatement("SELECT * from " + TABLE_CHANNELS + ";");
			set = statement.executeQuery();
			
			while(set.next()) {
				String _name = set.getString("name");
				String _desc = set.getString("description");
				String _cont = set.getString("context");
				boolean _pub = set.getBoolean("public");
				ChatChannel channel = new ChatChannel(_name, _desc, _cont);
				channel.setPublic(_pub);
				getManager().addChatChannel(channel);
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getID()         { return ID;      }
	public String getName()       { return NAME;    }
	public String getModuleName() { return MODULE;  }
	public String getVersion()    { return VERSION; }

	public void deleteChannel(ChatChannel channel) {
		getManager().removeChatChannel(channel);
		PreparedStatement statement;
		try {			
			statement = prepareStatement(
					"DELETE FROM " + TABLE_CHANNELS + " WHERE name = \"" + channel.getChannelName() + "\"");
			statement.executeUpdate();
			statement.close();
			
			statement = prepareStatement(
					"DELETE FROM " + TABLE_MESSAGES + " WHERE channel = \"" + channel.getChannelName().toLowerCase() + "\"");
			statement.executeUpdate();
			statement.close();
			
			statement = prepareStatement(
					"DROP TABLE IF EXISTS " + "sledgehammer_channel_" + channel.getChannelName() + "_history;");
			statement.executeUpdate();
			statement.close();
			
			// Removes all players from the channel.
			channel.removeAllPlayers();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

}