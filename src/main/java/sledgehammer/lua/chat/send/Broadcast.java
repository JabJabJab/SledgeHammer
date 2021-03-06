/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.lua.chat.send;

import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.core.Color;
import zombie.Lua.LuaManager;
import zombie.ui.UIFont;

// @formatter:off
/**
 * LuaTable designed to handle Broadcast operations, and storage of Broadcast data.
 *
 * <p>LuaTable: { - "timestamp": (String) The String-formatted timestamp of the Broadcast. -
 * "player_id": (String) The Unique ID String of the player who sent the Broadcast. - "message":
 * (String) The message content of the Broadcast. }
 *
 * <p>(Note: If the player_id is not set, it should be handled as administrator)
 *
 * @author Jab
 */
// @formatter:on
public class Broadcast extends LuaTable {

  /** Author of the Broadcast. Set to administrator by default. */
  private UUID playerId;
  /** The time of the Broadcast. */
  private String timestamp;
  /** The message content of the Broadcast. */
  private String message;

  private Color color;
  private UIFont font;

  /**
   * Main constructor.
   *
   * @param message The String message content of the Broadcast.
   */
  public Broadcast(String message, UIFont font, Color color) {
    super("Broadcast");
    setTimestamp(LuaManager.getHourMinuteJava());
    setMessage(message);
    setColor(color);
    setFont(font);
  }

  @Override
  public void onLoad(KahluaTable table) {
    // Set the playerId if it is defined.
    Object oPlayerId = table.rawget("player_id");
    if (oPlayerId != null) {
      setPlayerId(UUID.fromString(oPlayerId.toString()));
    }
    // Set the time if it is defined.
    Object oTimestamp = table.rawget("timestamp");
    if (oTimestamp != null) {
      setTimestamp(oTimestamp.toString());
    }
    // Set the message if it is defined.
    Object oMessage = table.rawget("message");
    if (oMessage != null) {
      setMessage(oMessage.toString());
    }
  }

  @Override
  public void onExport() {
    // @formatter:off
    set("timestamp", getTimestamp());
    set("player_id", getPlayerId());
    set("message", getMessage());
    set("color", getColor());
    set("font", getFont().name());
    // @formatter:on
  }

  /** @return Returns the String message content of the Broadcast. */
  public String getMessage() {
    return this.message;
  }

  /**
   * Sets the String message content of the Broadcast.
   *
   * @param message The String message to set.
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return Returns the Unique ID of the Player who sent the Broadcast.
   *     <p>(Note: if the Unique ID is not set, then it should be handled as administrator)
   */
  public UUID getPlayerId() {
    return this.playerId;
  }

  /**
   * Sets the Unique ID of the Player who sent the Broadcast.
   *
   * <p>(Note: if the Unique ID is not set, then it should be handled as administrator)
   *
   * @param playerId The Unique ID of the Player to set.
   */
  private void setPlayerId(UUID playerId) {
    this.playerId = playerId;
  }

  /** @return Returns the String timestamp of the Broadcast. */
  public String getTimestamp() {
    return this.timestamp;
  }

  /**
   * Sets the String timestamp of the Broadcast.
   *
   * @param timestamp The String timestamp to set.
   */
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public Color getColor() {
    return this.color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public void setFont(UIFont font) {
    this.font = font;
  }

  public UIFont getFont() {
    return this.font;
  }
}
