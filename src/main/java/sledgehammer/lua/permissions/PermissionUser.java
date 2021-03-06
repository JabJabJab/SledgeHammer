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

package sledgehammer.lua.permissions;

import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.module.permissions.MongoPermissionUser;
import sledgehammer.lua.core.Player;

/**
 * PermissionObject to handle permission-user data and operations for the Permissions Module.
 *
 * @author Jab
 */
public class PermissionUser extends PermissionObject<MongoPermissionUser> {

  /**
   * The PermissionGroup that the user is assigned to, if any. If not, the reference will be null.
   */
  private PermissionGroup permissionGroup;

  private PermissionGroup permissionGroupTemporary;

  /**
   * Main constructor.
   *
   * @param mongoDocument The MongoDocument storing the data.
   */
  public PermissionUser(MongoPermissionUser mongoDocument) {
    super(mongoDocument, "PermissionUser");
    loadNodes(mongoDocument);
  }

  @Override
  public boolean hasPermission(String node) {
    // Our returning flag result.
    boolean returned = false;
    // This will be the group's returned node for the one requested, if one is
    // defined.
    Node nodeGroup = null;
    // This will be the user's returned node for the one requested, if one is
    // defined.
    Node nodeUser;
    // Get the PermissionGroup associated with the user.
    PermissionGroup group = getPermissionGroup();
    // Check and see if the group exists.
    if (group != null) {
      // If so, then Grab the flag from the group.
      nodeGroup = group.getClosestPermissionNode(node);
    }
    // After the group comes any user-specific settings. This means that if a group
    // has a true flag for the node in question, and the user has a false flag, then
    // this means that the user overrides the group flag.
    //
    // Grab the closest permission for the user, if one exists.
    nodeUser = getClosestPermissionNode(node);
    // If the user has a Node definition affecting the one given
    if (nodeUser != null) {
      // If the group also has a definition for this node.
      if (nodeGroup != null) {
        // If they are equal, the user has authority over the group definition.
        // If the user is a sub-node of the most specific node defined for the group,
        // The user definition also has authority.
        if (nodeGroup.equals(nodeUser) || nodeUser.isSubNode(nodeGroup)) {
          // Set the user node's flag as the returned value.
          returned = nodeUser.getFlag();
        }
        // In this situation, the group definition is the most specific, being the
        // sub-node to the user, so the group node definition has the authority.
        else {
          // Set the group node's flag as the returned value.
          returned = nodeGroup.getFlag();
        }
      } else {
        // Set the user node's flag as the returned value.
        returned = nodeUser.getFlag();
      }
    }
    // In this situation, the user has no node defined for the one requested.
    else {
      // If the group has a definition for the node, while the user does not.
      // This should be the most common result, as user-specific permissions
      // are not the best practice for permissions, although it is reserved for
      // special instances or rare occasions.
      if (nodeGroup != null) {
        // Set the group node's flag as the returned value.
        returned = nodeGroup.getFlag();
      }
    }
    // Return the result flag.
    return returned;
  }

  @Override
  public void onLoad(KahluaTable table) {
    // TODO: Implement.
  }

  @Override
  public void onExport() {
    // TODO: Implement.
  }

  /**
   * @return Returns true if the Player being represented by the PermissionUser is an administrator.
   */
  public boolean isAdministrator() {
    // Default to false.
    boolean returned = false;
    // Attempt to grab the Player if online.
    Player player = SledgeHammer.instance.getPlayer(getUniqueId());
    // If the Player is not online.
    if (player == null) {
      // Grab the offline version.
      player = SledgeHammer.instance.getOfflinePlayer(getUniqueId());
    }
    // If the Player is null at this point, the Player does not exist.
    //
    // Check if the Player is an administrator.
    if (player != null && player.isAdministrator()) {
      // If so, set returned to true.
      returned = true;
    }
    // Return the result.
    return returned;
  }

  /**
   * @return Returns the PermissionGroup the user is assigned to, if any. If the user is not
   *     assigned to a group, null is returned.
   */
  public PermissionGroup getPermissionGroup() {
    return this.permissionGroup != null ? this.permissionGroup : this.permissionGroupTemporary;
  }

  /**
   * Sets the PermissionGroup that the PermissionUser is assigned to. To set the PermissionUser to
   * not have an assigned PermissionGroup, use null.
   *
   * <p>(Note: If the PermissionUser does not have a PermissionGroup assigned, the PermissionUser
   * will refer to the default PermissionGroup in the Permissions Module.
   *
   * @param permissionGroup The PermissionGroup to set.
   * @param save The flag to save the document.
   */
  public void setPermissionGroup(PermissionGroup permissionGroup, boolean save) {
    this.permissionGroup = permissionGroup;
    UUID groupId = null;
    if (permissionGroup != null) {
      groupId = permissionGroup.getUniqueId();
    }
    getMongoDocument().setGroupId(groupId, save);
  }

  public void setTemporaryPermissionGroup(PermissionGroup permissionGroup) {
    this.permissionGroupTemporary = permissionGroup;
  }

  /** @return Returns the Unique ID identifier associated with the PermissionUsers Player. */
  public UUID getUniqueId() {
    return getMongoDocument().getUniqueId();
  }

  /**
   * @return Returns the Unique ID identifier associated with the PermissionUsers PermissionGroup.
   */
  public UUID getGroupId() {
    return getMongoDocument().getGroupId();
  }

  /** @return Returns true if the PermissionUser is assigned to a PermissionGroup. */
  public boolean hasPermissionGroup() {
    return this.permissionGroup != null;
  }
}
