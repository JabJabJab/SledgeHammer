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

package sledgehammer.event.chat;

import java.util.UUID;

import sledgehammer.SledgeHammer;
import sledgehammer.event.Event;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.module.chat.ModuleChat;

/**
 * Event passed that contains a ChatMessage with the ChatChannel.
 *
 * @author Jab
 */
public class ChatMessageEvent extends Event {

  /** The ChatChannel the ChatMessage is in. */
  private ChatChannel chatChannel;
  /** The invoked ChatMessage. */
  private ChatMessage chatMessage;

  /**
   * Main constructor.
   *
   * @param chatMessage The ChatMessage invoked.
   */
  public ChatMessageEvent(ChatMessage chatMessage) {
    setMessage(chatMessage);
    UUID channelId = getMessage().getChannelId();
    ModuleChat moduleChat = getChatModule();
    setChatChannel(moduleChat.getChatChannel(channelId));
  }

  /** @return Returns the invoked ChatMessage. */
  public ChatMessage getMessage() {
    return this.chatMessage;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the invoked ChatMessage.
   *
   * @param chatMessage The ChatMessage to set.
   */
  private void setMessage(ChatMessage chatMessage) {
    this.chatMessage = chatMessage;
  }

  /** @return Returns the ChatChannel that the invoked ChatMessage is in. */
  public ChatChannel getChatChannel() {
    return this.chatChannel;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the ChatChannel that the invoked ChatMessage is in.
   *
   * @param chatChannel The ChatChannel to set.
   */
  private void setChatChannel(ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  /** @return Returns the ModuleChat instance of the Sledgehammer engine. */
  public ModuleChat getChatModule() {
    return SledgeHammer.instance.getPluginManager().getChatModule();
  }
}
