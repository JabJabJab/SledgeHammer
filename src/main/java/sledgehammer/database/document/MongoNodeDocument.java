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

package sledgehammer.database.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;

public abstract class MongoNodeDocument extends MongoDocument {

  /** The Map storing MongoNodes by their node value as the key. */
  private Map<String, MongoNode> mapNodes;

  /**
   * Main constructor.
   *
   * @param collection The MongoCollection storing the document.
   * @param fieldId The String identifier for the document.
   */
  public MongoNodeDocument(MongoCollection collection, String fieldId) {
    super(collection, fieldId);
    mapNodes = new HashMap<>();
  }

  /**
   * Handles loading nodes.
   *
   * @param object The DBObject storing the node data.
   */
  public void loadNodes(DBObject object) {
    mapNodes.clear();
    @SuppressWarnings({"rawtypes"})
    List objectList = (List) object.get("nodes");
    for (Object nextObject : objectList) {
      DBObject nextDBObject = (DBObject) nextObject;
      MongoNode mongoNode = new MongoNode(this);
      mongoNode.onLoad(nextDBObject);
      addNode(mongoNode, false);
    }
  }

  /**
   * Handles saving nodes.
   *
   * @param object The DBObject that stores the nodes.
   */
  public void saveNodes(DBObject object) {
    // Create a list of objects for the export.
    List<DBObject> listNodes = new ArrayList<>();
    // Go through each node in the document.
    for (MongoNode mongoNode : getNodes()) {
      // Create the object to contain the saved data.
      DBObject objectMongoNode = new BasicDBObject();
      // Save the MongoNode to the object.
      mongoNode.onSave(objectMongoNode);
      // Add the result object to the list.
      listNodes.add(objectMongoNode);
    }
    // Place the nodes into the main document object provided.
    object.put("nodes", listNodes);
  }

  /**
   * Adds a MongoNode to the document, if the entry does not exist.
   *
   * <p>(All nodes are stored and compared in lower-case automatically)
   *
   * @param mongoNode The MongoNode being added to the document.
   * @param save Flag to save the document after adding the MongoNode.
   * @return Returns true if the MongoNode is added to the document.
   */
  public boolean addNode(MongoNode mongoNode, boolean save) {
    boolean returned = false;
    if (!hasNode(mongoNode)) {
      mapNodes.put(mongoNode.getNode(), mongoNode);
      returned = true;
      if (save) save();
    }
    return returned;
  }

  /**
   * Removes a node from the document, if the entry exists.
   *
   * <p>(All nodes are stored and compared in lower-case automatically)
   *
   * @param mongoNode The MongoNode being removed from the document.
   * @param save Flag to save the document after removing the node.
   * @return Returns true if the MongoNode is removed from the document.
   */
  public boolean removeNode(MongoNode mongoNode, boolean save) {
    boolean returned = false;
    if (hasNode(mongoNode)) {
      mapNodes.remove(mongoNode.getNode());
      returned = true;
      if (save) save();
    }
    return returned;
  }

  /**
   * Checks if a MongoNode is assigned to the document.
   *
   * @param mongoNode The MongoNode being tested.
   * @return Returns true if the document contains the node.
   */
  public boolean hasNode(MongoNode mongoNode) {
    return mapNodes.containsKey(mongoNode.getNode());
  }

  /** @return Returns a List of the String nodes assigned to the document. */
  public Collection<MongoNode> getNodes() {
    return mapNodes.values();
  }
}
