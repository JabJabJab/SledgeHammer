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

package sledgehammer.event.player;

import sledgehammer.enums.AccessResult;

/**
 * CoOpEvent to handle Access requests to the PZ server.
 *
 * @author Jab
 */
public class CoOpAccessEvent extends CoOpEvent {

  /** The AccessResult of the CopAccessEvent. */
  private AccessResult accessResult = null;
  /** The String reason provided for the AccessResult. */
  private String reason = null;

  /**
   * Main constructor.
   *
   * @param result The AccessResult of the Co-Op Access Request.
   */
  public CoOpAccessEvent(AccessResult result) {
    this.accessResult = result;
  }

  /**
   * Alternative constructor for providing a String reason for the AccessResult given.
   *
   * @param accessResult The AccessResult of the Co-Op Access Request.
   * @param reason The String reason for the AccessResult given.
   */
  public CoOpAccessEvent(AccessResult accessResult, String reason) {
    setAccessResult(accessResult);
    setReason(reason);
  }

  /** @return Returns the String reason for the AccessResult given. */
  public String getReason() {
    return this.reason;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the String reason for the AccessResult given.
   *
   * @param reason The String reason to set.
   */
  private void setReason(String reason) {
    this.reason = reason;
  }

  /** @return Returns the AccessResult of the Co-Op Access Request. */
  public AccessResult getAccessResult() {
    return this.accessResult;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the AccessResult of the Co-Op Access Request.
   *
   * @param accessResult The AccessResult to set.
   */
  private void setAccessResult(AccessResult accessResult) {
    this.accessResult = accessResult;
  }
}
