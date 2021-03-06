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

package sledgehammer.module.core;

import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.annotations.CommandHandler;
import sledgehammer.enums.LogType;
import sledgehammer.enums.Result;
import sledgehammer.command.CommandListener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ChatTags;
import sledgehammer.command.Command;
import sledgehammer.util.Response;
import zombie.characters.IsoPlayer;
import zombie.ui.UIFont;

// Imports chat colors for short-hand.


/**
 * TODO: Document.
 *
 * @author Jab
 */
public class CoreCommandListener extends CommandListener {

  private static final boolean DEBUG = true;
  public static final Command commandProperties = new Command("properties");

  private ModuleCore module;

  public CoreCommandListener(ModuleCore module) {
    super(module.getLanguagePackage());
    setModule(module);
    module.addDefaultPermission("core.command.commitsuicide");
    module.addDefaultPermission("core.command.colors");
    module.addDefaultPermission("core.command.fonts");
  }

  @CommandHandler(command = "colors", permission = "core.command.colors")
  private void onCommandColors(Command c, Response r) {
    r.set(Result.SUCCESS, ChatTags.listColors());
  }

  @CommandHandler(command = "fonts", permission = "core.command.fonts")
  private void onCommandFonts(Command c, Response r) {
    StringBuilder stringBuilder = new StringBuilder();
    for(UIFont font: UIFont.values()) {
      if(stringBuilder.length() == 0) {
        stringBuilder.append("Fonts: ").append(font.name());
      } else {
        stringBuilder.append(", ").append(font.name());
      }
    }
    r.set(Result.SUCCESS, stringBuilder.toString());
  }

  @CommandHandler(
    command = "commitsuicide",
    permission = "core.command.commitsuicide"
  )
  private void onCommandCommitSuicide(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    IsoPlayer iso = commander.getIso();
    if (iso != null) {
      iso.setHealth(-1.0F);
      iso.DoDeath(iso.bareHands, iso, true);
    }
    r.set(Result.SUCCESS, "Done.");
    r.log(LogType.INFO, commander.getUsername() + " committed suicide.");
  }

  @CommandHandler(command = "properties", permission = "core.command.properties")
  private void onCommandProperties(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    String username = null;
    Player playerProperties;
    switch (args.length) {
      case 0:
        playerProperties = commander;
        break;
      case 1:
        username = args[0];
        playerProperties = SledgeHammer.instance.getPlayer(username);
        break;
      default:
        r.set(Result.FAILURE, lang.getString("tooltip_command_properties", language));
        return;
    }
    if (playerProperties == null) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_properties", language));
      return;
    }
    Map<String, String> properties = playerProperties.getProperties();
    StringBuilder builder = new StringBuilder();
    builder
        .append("Properties for player \"")
        .append(playerProperties)
        .append("\":")
        .append(ChatTags.NEW_LINE)
        .append(" ");
    for (String key : properties.keySet()) {
      String value = properties.get(key);
      builder.append(key).append(": ").append(value).append(ChatTags.NEW_LINE).append(" ");
    }
    r.set(Result.SUCCESS, builder.toString());
    r.log(
        LogType.INFO,
        username + " looked up properties for player \"" + playerProperties.getUsername() + "\".");
  }

  public ModuleCore getModule() {
    return this.module;
  }

  public void setModule(ModuleCore module) {
    this.module = module;
  }
}
