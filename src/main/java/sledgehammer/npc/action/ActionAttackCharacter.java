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

package sledgehammer.npc.action;

import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.HandWeapon;
import zombie.sledgehammer.PacketHelper;
import zombie.sledgehammer.npc.NPC;

/**
 * Action that executes NPCs Attacking another IsoGameCharacter.
 *
 * @author Jab
 */
public class ActionAttackCharacter extends Action {

  /** The String name of the Action. */
  public static final String NAME = "Action->AttackCharacter";

  @Override
  public boolean act(NPC npc) {
    IsoGameCharacter target = npc.getAttackTarget();
    HandWeapon weapon = npc.getPrimaryWeapon();
    npc.faceDirection(target);
    if (weapon != null && npc.CanAttack()) {
      PacketHelper.hitCharacter(npc, target, weapon);
    }
    // Make sure target is valid.
    if (target != null) {}
    return false;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
