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

package sledgehammer.util;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ThrowableEvent;

/**
 * TODO: Document
 *
 * @author Jab
 */
public abstract class Printable {

  static String newLine = System.getProperty("line.separator");

  /**
   * Prints lines with "getName(): [message...]".
   *
   * @param messages The Object Array of messages to print.
   */
  public synchronized void println(Object... messages) {
    if (messages.length == 0) {
      System.out.println();
    } else {
      // Grab the name of the instance.
      String name = getName();
      // Create the header, based on if the name String is null of empty.
      String header = name == null || name.isEmpty() ? "" : name + ": ";
      // Go through each Object, and print them as a separate line.
      StringBuilder compiledString = new StringBuilder();
      for (Object message : messages) {
        compiledString.append(header).append(message).append(newLine);
      }
      // Print the result.
      System.out.print(compiledString.toString());
    }
  }

  /**
   * Prints lines with "getName(): [message...]".
   *
   * @param messages The Object Array of messages to print.
   */
  public synchronized void errln(Object... messages) {
    if (messages.length == 0) {
      System.err.println();
    } else {
      // Grab the name of the instance.
      String name = getName();
      // Create the header, based on if the name String is null of empty.
      String header = name == null || name.isEmpty() ? "" : name + ": ";
      // Go through each Object, and print them as a separate line.
      StringBuilder compiledString = new StringBuilder();
      for (Object message : messages) {
        compiledString.append(header).append(message).append(newLine);
      }
      // Print the result.
      System.err.print(compiledString.toString());
    }
  }

  /**
   * Prints a message with a header, without a new-line.
   *
   * @param message The Object to print.
   */
  public synchronized void printH(Object message) {
    // Grab the name of the instance.
    String name = getName();
    // Create the header, based on if the name String is null of empty.
    String header = name == null || name.isEmpty() ? "" : name + ": ";
    // Print the result.
    System.out.print(header + message);
  }

  /**
   * Prints a message, without a new-line.
   *
   * @param message The Object to print.
   */
  public synchronized void print(Object message) {
    // Print the result.
    System.out.print(message);
  }

  public synchronized void stackTrace(Throwable throwable) {
    errln(ThrowableEvent.getStackTrace(throwable));
    // Send to the EventManagerOld for ExceptionListeners to handle.
    if (SledgeHammer.instance != null) {
      SledgeHammer.instance.handle(throwable);
    }
  }

  public synchronized void stackTrace() {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      errln(element);
    }
  }

  /**
   * Grabs the name of the instance. This is used for the header of prints and printlns.
   *
   * @return Returns the name of the Printable.
   */
  public abstract String getName();
}
