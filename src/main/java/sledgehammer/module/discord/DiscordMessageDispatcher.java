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

package sledgehammer.module.discord;

import java.util.List;
import java.util.Map;

import de.btobastian.javacord.entities.Channel;

/**
 * Class designed to manage and dispatch the message queue for the IRC-Bot plug-in.
 *
 * @author Jab
 */
class DiscordMessageDispatcher extends Thread {

  /** The Module using this dispatcher. */
  private DiscordBot bot;

  private long minimumMessageDelay = 350L;
  private long maximumMessageDelay = 2000L;

  /** Flag for the Dispatcher's while loop. */
  private volatile boolean active = false;

  /**
   * Main constructor.
   *
   * @param bot The DiscordBot instance.
   */
  DiscordMessageDispatcher(DiscordBot bot) {
    // Assign the Module instance to the dispatcher field object.
    this.bot = bot;
  }

  public void run() {
    // Set the active flag to true. The while loop uses this to know when to
    // stop.
    active = true;
    // Run indefinitely until active is set to false.
    while (active) {
      try {
        if (bot.isConnected()) {
          Map<Channel, List<String>> mapQueue = bot.getQueue();
          int totalCount = 0;
          for (Channel channel : mapQueue.keySet()) {
            try {
              List<String> channelQueue = mapQueue.get(channel);
              if (channelQueue.size() > 0) {
                totalCount += channelQueue.size();
                // Grab the next message in the queue.
                String message = channelQueue.remove(0);
                // Make sure the message is not null.
                if (message != null) {
                  channel.sendMessage(message).get();
                  Thread.sleep(1L);
                }
              }
            } catch (Exception e) {
              if (ModuleDiscord.DEBUG) {
                bot.getModule().stackTrace(e);
              }
            }
          }
          try {
            // If the message list is big, the delay between
            // messages grows, to help prevent excess flooding.
            long delay = 250L * totalCount;
            // The maximum message delay cap check is applied.
            delay = delay > maximumMessageDelay ? maximumMessageDelay : delay;
            // The minimum message delay cap check is applied.
            delay = delay <= minimumMessageDelay ? minimumMessageDelay : delay;
            Thread.sleep(delay);
          } catch (InterruptedException e) {
            if (ModuleDiscord.DEBUG) {
              bot.getModule().stackTrace(e);
            }
          }
        } else {
          try {
            // Try to sleep for minimum sleep time.
            Thread.sleep(minimumMessageDelay);
          } catch (InterruptedException e) {
            //
          }
        }
      } catch (IndexOutOfBoundsException e) {
        try {
          // Try to sleep for minimum sleep time.
          Thread.sleep(minimumMessageDelay);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  /** Sets the active flag to false on the dispatcher. This stops the Thread. */
  public void setInactive() {
    active = false;
  }
}
