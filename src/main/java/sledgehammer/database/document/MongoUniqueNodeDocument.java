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
import java.util.List;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;

/**
 * Class to handle String node operations for UniqueMongoDocuments.
 *
 * <p>(All nodes are stored and compared in lower-case automatically)
 *
 * @author Jab
 */
public abstract class MongoUniqueNodeDocument extends MongoUniqueDocument {

  /** The List of String nodes. */
  private List<MongoNode> listMongoNodes;

  /**
   * MongoDB constructor.
   *
   * @param collection The MongoCollection storing the document.
   * @param object The DBObject storing the data.
   */
  public MongoUniqueNodeDocument(MongoCollection collection, DBObject object) {
    super(collection, object);
    listMongoNodes = new ArrayList<>();
    loadNodes(object);
  }

  /**
   * New constructor.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   */
  public MongoUniqueNodeDocument(MongoCollection collection) {
    super(collection);
    listMongoNodes = new ArrayList<>();
  }

  /**
   * New constructor with provided ID.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   * @param uniqueId The Unique ID being assigned.
   */
  public MongoUniqueNodeDocument(MongoCollection collection, UUID uniqueId) {
    super(collection, uniqueId);
    listMongoNodes = new ArrayList<>();
  }

  /**
   * Loads the Nodes into the MongoNodeDocument
   *
   * @param object The DBObject storing the data for the MongoNodeDocument.
   */
  public void loadNodes(DBObject object) {
    // Grab the list of MongoNodes in DBObject format.
    @SuppressWarnings("rawtypes")
    List list = (List) object.get("nodes");
    // Go through each DBObject.
    for (Object oNodeNext : list) {
      // Cast to the DBObject class.
      DBObject objectNodeNext = (DBObject) oNodeNext;
      // Create a new MongoNode linking to the document.
      MongoNode mongoNodeNext = new MongoNode(this);
      // Load the MongoNode.
      mongoNodeNext.onLoad(objectNodeNext);
      // Add to the list of MongoNodes.
      this.listMongoNodes.add(mongoNodeNext);
    }
  }

  /**
   * Saves the MongoNode List to the DBObject given.
   *
   * @param object The DBObject to store the MongoNode sub-document List.
   */
  public void saveNodes(DBObject object) {
    // Create a new List to store the DBObjects representing the MongoNodes.
    List<DBObject> objectNodes = new ArrayList<>();
    // Go through each assigned MongoNode.
    for (MongoNode nodeNext : listMongoNodes) {
      // Create a new DBObject to store the MongoNode data to.
      DBObject objectNodeNext = new BasicDBObject();
      // Save the data to the DBObject.
      nodeNext.onSave(objectNodeNext);
      // Add the MongoNode's DBObject to the List.
      objectNodes.add(objectNodeNext);
    }
    // Save the MongoNodes into the 'nodes' field.
    object.put("nodes", objectNodes);
  }

  @Override
  public void save() {
    // Create a new DBObject with the document's identifier.
    DBObject object = new BasicDBObject(getFieldId(), getFieldValue());
    // Populate the main document.
    onSave(object);
    // Save the entries.
    saveEntries(object);
    // Save the nodes.
    saveNodes(object);
    // Upsert the document.
    getCollection().upsert(object, getFieldId(), this);
  }

  /**
   * Adds a MongoNode to the document. If the MongoNode already exists within the document, the flag
   * will be set for the first MongoNode, and then the MongoNode will be removed. The given
   * MongoNode will be put into the list thereafter.
   *
   * @param mongoNode The MongoNode to add (or override), to the document.
   * @param save Flag for saving the document after adding the node.
   */
  public void addNode(MongoNode mongoNode, boolean save) {
    // Validate the MongoNode argument.
    if (mongoNode == null) {
      throw new IllegalArgumentException("MongoNode given is null.");
    }
    // If the node already exists, the one being set needs to replace the instance
    // entirely.
    if (listMongoNodes.contains(mongoNode)) {
      // Grab the previous instance that identifies with the equals operation.
      MongoNode mongoNodeOther = listMongoNodes.remove(listMongoNodes.indexOf(mongoNode));
      // In case something is using this instance of the node, set the flag for
      // reference.
      mongoNodeOther.setFlag(mongoNode.getFlag(), false);
      mongoNodeOther.setMongoDocument(null);
    }
    mongoNode.setMongoDocument(this);
    // Add the current instance.
    listMongoNodes.add(mongoNode);
    // If the argument to save is true
    if (save) {
      // Save the document.
      save();
    }
  }

  /**
   * Removes a MongoNode from the document.
   *
   * @param mongoNode The MongoNode being removed.
   * @param save Flag for saving the document after removing the node.
   * @return Returns true if the MongoNode is removed from the document.
   */
  public boolean removeNode(MongoNode mongoNode, boolean save) {
    boolean returned = false;
    // Validate the MongoNode argument.
    if (mongoNode == null) {
      throw new IllegalArgumentException("MongoNode given is null.");
    }
    // Check if the MongoNode is assigned to the document.
    if (listMongoNodes.contains(mongoNode)) {
      // Remove from the list.
      listMongoNodes.remove(mongoNode);
      // Set the flag to true to return success.
      returned = true;
    }
    // If the flag to save is set to true, save the document.
    if (save) {
      save();
    }
    // Return the result.
    return returned;
  }

  public List<MongoNode> getMongoNodes() {
    return this.listMongoNodes;
  }
}
