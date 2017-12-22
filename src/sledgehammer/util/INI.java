/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * TODO: Document
 * @author Jab
 */
@Deprecated
public class INI {

	private HashMapINI<String, HashMapINI<String, Object>> mapSections;
	private Map<String, HashMapINI<String, String>> mapSectionsAsStrings;
	private Map<String, List<String>> mapSectionComments;
	private Map<String, HashMapINI<String, List<String>>> mapSectionVariableComments;
	private List<String> listSections;
	private File file;

	public INI(File file) {
		setFile(file);
		reset();
	}

	public void reset() {
		if (mapSections == null) {
			mapSections = new HashMapINI<String, HashMapINI<String, Object>>("");
		} else {
			mapSections.clear();
		}

		if (listSections == null) {
			listSections = new ArrayList<String>();
		} else {
			listSections.clear();
		}

		if (mapSectionsAsStrings == null) {
			mapSectionsAsStrings = new HashMap<String, HashMapINI<String, String>>();
		} else {
			mapSectionsAsStrings.clear();
		}

		if (mapSectionVariableComments == null) {
			mapSectionVariableComments = new HashMap<String, HashMapINI<String, List<String>>>();
		} else {
			mapSectionVariableComments.clear();
		}

		if (mapSectionComments == null) {
			mapSectionComments = new HashMap<String, List<String>>();
		} else {
			mapSectionComments.clear();
		}

	}

	public void read() throws IOException {
		Scanner scanner = new Scanner(file);

		HashMapINI<String, Object> mapSection = null;
		HashMapINI<String, String> mapSectionAsString = null;

		List<String> comment = null;

		HashMapINI<String, List<String>> mapVariableComments = null;

		String mapSectionName = null;
		String[] variableSplit = null;

		boolean numericalSuccess = false;
		boolean lastLineComment = false;

		mapSectionAsString = new HashMapINI<String, String>(mapSectionName);
		mapSection = new HashMapINI<String, Object>(mapSectionName);
		mapSections.put("", mapSection);
		mapVariableComments = new HashMapINI<String, List<String>>(mapSectionName);
		mapSectionVariableComments.put("", mapVariableComments);

		String newLine = null;
		while (scanner.hasNextLine()) {

			newLine = scanner.nextLine().trim();

			if (newLine.isEmpty())
				continue;

			// If the line is a comment, continue to the next line.
			if (newLine.startsWith(";")) {
				if (!lastLineComment) {
					comment = new ArrayList<String>();
				}
				String nextComment = null;
				String[] split = newLine.split(";");
				if (split.length == 2) {
					nextComment = split[1].trim();
				} else if (split.length == 1) {
					nextComment = split[0];
				}

				comment.add(nextComment);
				if (mapSectionName != null) {
					mapSectionVariableComments.put(mapSectionName, mapVariableComments);
				}
				lastLineComment = true;
				continue;
			}

			// If the line is the beginning of a new section.
			if (newLine.startsWith("[") && newLine.endsWith("]")) {
				if (mapVariableComments != null)
					mapSectionVariableComments.put(mapSectionName, mapVariableComments);
				mapVariableComments = new HashMapINI<String, List<String>>(mapSectionName);

				// Place the current section into the main Map.
				if (mapSection != null) {
					mapSections.put(mapSectionName, mapSection);
					mapSectionsAsStrings.put(mapSectionName, mapSectionAsString);
				}
				mapSectionName = newLine.substring(1, newLine.length() - 1);
				mapSection = mapSections.get(mapSectionName);

				if (mapSection != null) {
					mapSectionAsString = mapSectionsAsStrings.get(mapSectionName);
					if (mapSectionAsString == null)
						mapSectionAsString = new HashMapINI<String, String>(mapSectionName);
				} else {
					mapSectionAsString = new HashMapINI<String, String>(mapSectionName);
					mapSection = new HashMapINI<String, Object>(mapSectionName);
					if (lastLineComment)
						mapSectionComments.put(mapSectionName, comment);
					mapSections.put(mapSectionName, mapSection);
				}
				lastLineComment = false;
				continue;
			}

			if (newLine.contains("=")) {
				variableSplit = newLine.split("=");

				// If the syntax is correct.
				if (variableSplit.length == 2) {
					// If the sides of the '=' character are valid.
					if (variableSplit[0] != null && variableSplit[0].length() > 0) {
						// Store object as String for convenience.
						mapSectionAsString.put(variableSplit[0], variableSplit[1]);
						// At this point, we can only handle the variable as a String.
						if (!numericalSuccess)
							mapSection.put(variableSplit[0], variableSplit[1]);
						// Stores comment for writing and reference purposes.
						if (lastLineComment)
							mapVariableComments.put(variableSplit[0], comment);
						else
							mapVariableComments.remove(variableSplit[0]);
						lastLineComment = false;
					}
				} else if (!variableSplit[0].isEmpty()) {
					mapSection.put(variableSplit[0], null);
					// Stores comment for writing and reference purposes.
					if (lastLineComment) {
						mapVariableComments.put(variableSplit[0], comment);
					} else {
						mapVariableComments.remove(variableSplit[0]);
					}
					lastLineComment = false;
				} else {
					lastLineComment = false;
				}
			}
		}

		if (mapVariableComments != null) {
			mapSectionVariableComments.put(mapSectionName, mapVariableComments);
		}
		scanner.close();
	}

	public void save() throws IOException {
		saveFileAs(file);
	}

	public void saveFileAs(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));

		HashMapINI<String, List<String>> mapVComments = null;
		List<String> comments = null;
		String newLine = System.getProperty("line.separator");
		HashMapINI<String, Object> mapSection = null;
		boolean firstSection = true;

		for (String sectionName : listSections) {

			mapSection = mapSections.get(sectionName);

			if (firstSection) {
				firstSection = false;
			} else {
				writer.write(newLine);
			}

			comments = mapSectionComments.get(sectionName);

			if (comments != null) {
				for (String comment : comments) {
					if (comment != null) {
						if (!comment.startsWith("#") || comment.startsWith("//")) {
							comment = "# " + comment + newLine;
						}
						writer.write(comment);
					}
				}
			}

			writer.write("[" + sectionName + "]" + newLine);

			mapVComments = mapSectionVariableComments.get(sectionName);

			for (String variable : mapSection.getKeysOrdered()) {

				if (mapVComments != null)
					comments = mapVComments.get(variable);

				if (comments != null) {
					String newComment;
					for (String comment : comments) {
						if (comment != null) {
							if (comment.startsWith(";"))
								newComment = comment;
							else
								newComment = "; " + comment;
							writer.write(newComment + newLine);
						}
					}
				}

				Object o = mapSection.get(variable);
				if (o == null) {
					writer.write(variable + "=" + newLine);
				} else if (o instanceof String) {
					writer.write(variable + "=" + (String) o + newLine);
				} else if (o instanceof Boolean) {
					writer.write(variable + "=" + ((Boolean) o).toString() + newLine);
				} else if (o instanceof Integer) {
					writer.write(variable + "=" + ((Integer) o).intValue() + newLine);
				} else {
					writer.write(variable + "=" + o.toString() + newLine);
				}
				writer.write(newLine);
			}
		}
		writer.close();
	}

	public void setComments(String sectionName, String variable, List<String> comments) {
		HashMapINI<String, List<String>> mapComments = getSectionComments(sectionName);

		if (mapComments == null) {
			mapComments = new HashMapINI<String, List<String>>(sectionName);
			mapSectionVariableComments.put(sectionName, mapComments);
		}

		mapComments.put(variable, comments);
	}

	public void setComments(String sectionName, String variable, String... comments) {
		List<String> listComments = new ArrayList<String>();
		for (String comment : comments)
			listComments.add(comment);
		setComments(sectionName, variable, listComments);
	}

	public void setVariableComment(String sectionName, String variable, String comment) {
		List<String> listComments = new ArrayList<String>();
		listComments.add(comment);
		setComments(sectionName, variable, listComments);
	}

	public void appendVariableComment(String sectionName, String variable, String comment) {
		HashMapINI<String, List<String>> mapComments = getSectionComments(sectionName);

		if (mapComments == null) {
			mapComments = new HashMapINI<String, List<String>>(sectionName);
			mapSectionVariableComments.put(sectionName, mapComments);
		}

		List<String> listComments = mapComments.get(variable);
		if (listComments == null) {
			listComments = new ArrayList<String>();
			mapComments.put(variable, listComments);
		}

		listComments.add(comment);
	}

	public void clearVariableComments(String sectionName, String variable) {
		Map<String, List<String>> mapComments = getSectionComments(sectionName);

		if (mapComments != null) {
			mapComments.remove(variable);
		}
	}

	private HashMapINI<String, List<String>> getSectionComments(String sectionName) {
		return mapSectionVariableComments.get(sectionName);
	}

	public boolean hasComment(String sectionName, String variable) {
		HashMapINI<String, List<String>> mapComments = getSectionComments(sectionName);

		if (mapComments != null) {
			List<String> comments = mapComments.get(variable);

			return comments != null && !comments.isEmpty();
		}
		return false;
	}

	private HashMapINI<String, List<String>> createVariableCommentsMap(String sectionName) {
		HashMapINI<String, List<String>> mapVComments = null;

		mapVComments = mapSectionVariableComments.get(sectionName);
		if (mapVComments != null) {
			return mapVComments;
		} else {
			mapVComments = new HashMapINI<String, List<String>>(sectionName);
			mapSectionVariableComments.put(sectionName, mapVComments);
			return mapVComments;
		}
	}

	public String getVariableAsString(String section, String variable) {
		Object o = mapSections.get(section).get(variable);
		if (o == null)
			return "";
		else
			return o.toString();
	}

	public Object getVariable(String section, String variable) {
		return mapSections.get(section).get(variable);
	}

	public void setVariable(String sectionName, String variableName, Object variable, String... comments) {

		List<String> listComments = null;

		if (comments.length > 0) {
			listComments = new ArrayList<String>();
			for (String comment : comments) {
				listComments.add(comment);
			}
		}

		setVariable(sectionName, variableName, variable, listComments);
	}

	public void setVariable(String sectionName, String variableName, Object variable, List<String> comments) {
		Map<String, Object> map = getSection(sectionName);
		if (map == null) {
			map = createSection(sectionName);
		}

		map.put(variableName, variable);
		if (comments != null) {
			HashMapINI<String, List<String>> mapVComments = mapSectionVariableComments.get(sectionName);
			mapVComments.put(variableName, comments);
		}
	}

	public HashMapINI<String, Object> createSection(String sectionName, String... comments) {
		List<String> listComments = null;

		listComments = new ArrayList<String>();

		if (comments.length > 0) {
			for (String comment : comments)
				listComments.add(comment);
		}
		return createSection(sectionName, listComments);
	}

	public HashMapINI<String, Object> createSection(String sectionName, List<String> comments) {
		HashMapINI<String, Object> map = null;

		map = mapSections.get(sectionName);

		if (map == null) {
			map = new HashMapINI<String, Object>(sectionName);
			mapSections.put(sectionName, map);
			listSections.add(sectionName);
		}

		createVariableCommentsMap(sectionName);
		mapSectionComments.put(sectionName, comments);
		if (mapSectionVariableComments.get(sectionName) == null) {
			mapSectionVariableComments.put(sectionName, new HashMapINI<String, List<String>>(sectionName));
		}
		return map;
	}

	public Map<String, Object> getSection(String section) {
		return mapSections.get(section);
	}

	public void setFile(File file) {
		this.file = file;
	}

	public class HashMapINI<K, V> extends HashMap<K, V> {
		private static final long serialVersionUID = 3570436213264485075L;
		private String name;
		private List<K> listOrderedKeys;

		public HashMapINI(String name) {
			super();
			setName(name);
			listOrderedKeys = new ArrayList<K>();
		}

		public String getName() {
			return this.name;
		}

		public V put(K key, V value) {
			if (!listOrderedKeys.contains(key))
				listOrderedKeys.add(key);
			return super.put(key, value);
		}

		public void clear() {
			super.clear();
			listOrderedKeys.clear();
		}

		public V remove(Object key) {
			if (listOrderedKeys.contains(key))
				listOrderedKeys.remove(key);
			return super.remove(key);
		}

		public List<K> getKeysOrdered() {
			return listOrderedKeys;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public File getFile() {
		return this.file;
	}
}