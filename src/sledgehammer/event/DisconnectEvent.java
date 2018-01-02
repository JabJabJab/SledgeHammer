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
package sledgehammer.event;

import sledgehammer.lua.core.Player;
import sledgehammer.util.DisconnectType;
import zombie.core.raknet.UdpConnection;

/**
 * PlayerEvent to pass when a <Player> disconnects from the PZ Server.
 * 
 * @author Jab
 */
public class DisconnectEvent extends PlayerEvent {

	/** The String ID of the Event. */
	public static final String ID = "DisconnectEvent";

	/** The message Sent to the Player for being disconnected. */
	private String message;
	/** The <DisconnectType> type of disconnection the <Player> received. */
	private DisconnectType disconnectType = DisconnectType.DISCONNECT_MISC;

	/**
	 * Main constructor.
	 * 
	 * @param player
	 *            The <Player> that being disconnected.
	 */
	public DisconnectEvent(Player player) {
		super(player);
	}

	/**
	 * Alternative constructor for providing a <String> message to the <Player>
	 * being disconnected.
	 * 
	 * @param player
	 *            The <Player> being disconnected.
	 * @param message
	 *            The <String> message sent to the <Player>.
	 */
	public DisconnectEvent(Player player, String message) {
		super(player);
		setMessage(message);
	}

	/**
	 * Alternative constructor for providing the type of disconnection of the
	 * <Player>.
	 * 
	 * @param player
	 *            The <Player> being disconnected.
	 * @param disconnectType
	 *            The <DisconnectType> type of disconnection.
	 */
	public DisconnectEvent(Player player, DisconnectType disconnectType) {
		super(player);
		if (player == null) {
			throw new IllegalArgumentException("Player is null!");
		}
		setDisconnectType(disconnectType);
	}
	
	@Override
	public String getLogMessage() {
		Player player = getPlayer();
		UdpConnection connection = player.getConnection();
		if (disconnectType == DisconnectType.DISCONNECT_USERNAME_EMPTY) {
			return connection.ip + " disconnected. (Username is Empty)";
		} else if (disconnectType == DisconnectType.DISCONNECT_KICKED) {
			return player.getUsername() + " disconnected. (Kicked from Server)";
		} else if (disconnectType == DisconnectType.DISCONNECT_USERNAME_BANNED) {
			return player.getUsername() + " disconnected. (Username Banned)";
		} else if (disconnectType == DisconnectType.DISCONNECT_IP_BANNED) {
			return player.getUsername() + " disconnected. (IP Banned: " + connection.ip + ")";
		} else if (disconnectType == DisconnectType.DISCONNECT_USERNAME_ALREADY_LOGGED_IN) {
			return player.getUsername() + " disconnected. (User already logged in)";
		} else if (disconnectType == DisconnectType.DISCONNECT_STEAM_BANNED) {
			return player.getUsername() + " disconnected. (Steam ID Banned)";
		} else if (disconnectType == DisconnectType.DISCONNECT_SERVER_FULL) {
			return player.getUsername() + " disconnected. (Server Full)";
		} else if (disconnectType == DisconnectType.DISCONNECT_MISC) {
			return player.getUsername() + " disconnected. (Unknown)";
		}
		return player.getUsername() + " disconnected.";
	}

	@Override
	public String getID() {
		return ID;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the <DisconnectType> for the <DisconnectEvent>.
	 * 
	 * @param disconnectType
	 *            The <DisconnectType> to set.
	 */
	private void setDisconnectType(DisconnectType disconnectType) {
		this.disconnectType = disconnectType;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the <String> message sent to the <Player>.
	 * 
	 * @param message
	 *            The <String> message to set.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return Returns the <String> message sent to the <Player>.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @return Returns the <DisconnectType> type of the <DisconnectEvent>.
	 */
	public DisconnectType getDisconnectType() {
		return this.disconnectType;
	}
}