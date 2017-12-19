package sledgehammer.lua.chat;

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

import java.util.UUID;

import com.mongodb.DBObject;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.lua.core.Player;

public class ChatMessagePlayer extends ChatMessage {

	private Player player;
	private String playerUsername;
	private UUID playerUniqueId;
	
	public ChatMessagePlayer(KahluaTable table) {
		super(table);
		setName("ChatMessagePlayer");
	}
	
	public ChatMessagePlayer(KahluaTable table, long id) {
		super(table);
		setID(id);
	}
	
	public ChatMessagePlayer(Player player, String message) {
		super(message);
		setName("ChatMessagePlayer");
		setPlayer(player);
		setID(System.nanoTime());
		setMessage(message);
		setOriginalMessage(message);
	}

	public ChatMessagePlayer(long messageID, String channel, String message, String messageOriginal,
			boolean edited, String editorID, boolean deleted, String deleterID, long modifiedTimestamp, String time, String playerID, String playerName) {
		super(messageID, channel, message, messageOriginal, edited, editorID, deleted, deleterID, modifiedTimestamp, time);
		setPlayer(SledgeHammer.instance.getPlayer(UUID.fromString(playerID)));
		
		// Player expired or unregistered.
		if(getPlayer() == null) {
			Player player = new Player(playerName);
			setPlayer(player);
		}
		setPlayerUniqueId(UUID.fromString(playerID));
		setPlayerUsername(playerName);
	}

	public ChatMessagePlayer(DBObject object) {
		super("ChatMessagePlayer");
		load(object);
	}
	
	@Override
	public void load(DBObject object) {
		super.load(object);
		String playerUniqueIdString = object.get("playerID").toString();
		UUID playerUniqueId = UUID.fromString(playerUniqueIdString);
		setPlayer(SledgeHammer.instance.getPlayer(playerUniqueId));
		setPlayerUniqueId(playerUniqueId);
	}
	
	@Override
	public void onSave(DBObject object) {
		super.onSave(object);
		object.put("playerID"      , getPlayerUniqueId().toString());
		object.put("playerUsername", getPlayerUsername());
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public void setPlayer(Player player) {
		if(this.player != player) {
			this.player = player;
		}
	}
	
	@Override
	public void onExport() {
		super.onExport();
		set("player", player);
	}
	
	@Override
	public void onLoad(KahluaTable table) {
		// Load superclass table-data first.
		super.onLoad(table);
		String playerID = table.rawget("playerID").toString();
		Player player = SledgeHammer.instance.getPlayer(UUID.fromString(playerID));
		setPlayer(player);
	}
	
	public UUID getPlayerUniqueId() {
		return this.playerUniqueId;
	}
	
	public void setPlayerUniqueId(UUID playerUniqueId) {
		this.playerUniqueId = playerUniqueId;
	}
	
	public String getPlayerUsername() {
		return this.playerUsername;
	}
	
	public void setPlayerUsername(String username) {
		this.playerUsername = username;
	}
}