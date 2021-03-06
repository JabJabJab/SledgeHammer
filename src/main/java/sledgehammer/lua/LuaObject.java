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

package sledgehammer.lua;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.util.Printable;
import zombie.Lua.LuaManager;

/**
 * An abstract utility Class that allows proper organization and export of KahluaTables for complex
 * data structures.
 *
 * @author Jab
 */
public abstract class LuaObject extends Printable {

  /** Global flag for debugging LuaObjects. */
  public static boolean DEBUG = false;
  /** Global flag for verbose debugging LuaObjects. */
  public static boolean VERBOSE = false;

  /** The String name of the LuaObject. This is shown as '__name' in the exported KahluaTable. */
  private String name;

  /**
   * Main constructor.
   *
   * @param name The String name of the LuaObject. This is shown as '__name' in the exported
   *     KahluaTable.
   */
  public LuaObject(String name) {
    this.name = name;
  }

  /**
   * Returns the String name of the LuaObject. This is shown as '__name' in the exported
   * KahluaTable.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the String name of the LuaObject. This is shown as '__name' in the exported KahluaTable.
   *
   * @param name The String name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /** @return Returns an exported version of a LuaObject formatted as a KahluaTable. */
  public abstract KahluaTable export();

  /**
   * Validates an Object value for KahLua.
   *
   * @param value The Object value to validate.
   * @return Returns the validated Object.
   */
  public static Object processValue(Object value) {
    Object result = value;
    if (value instanceof Number) {
      result = toLuaNumber((Number) value);
    } else if (value instanceof LuaObject) {
      result = ((LuaObject) value).export();
    }
    return result;
  }

  /**
   * Formats a given Number to a double, as Lua requires all values to be doubles.
   *
   * @param number The Number being formatted for Lua.
   * @return Returns a Double value as a Number.
   */
  public static Number toLuaNumber(Number number) {
    if (number instanceof Long) {
      return Double.longBitsToDouble((Long) number);
    } else {
      return number.doubleValue();
    }
  }

  /**
   * @return Returns a new KahluaTable instance, generated by the J2SEPlatform instance for KahLua
   *     in the ProjectZomboid LuaManager.
   */
  public static KahluaTable newTable() {
    return LuaManager.platform.newTable();
  }
}
