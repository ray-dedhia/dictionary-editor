package dictionary_editor_stuff;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DictionaryEditor extends JFrame {
	/* Main Variables */
	private static final long serialVersionUID = -5747202602043187715L;
	/**
	 * Maps words to a list of entries, in case the user wants to add more than one entry for a word (for example, if the word can be used as a noun and as an adjective. Each entry can have only one part of speech, but can have more than one definition.
	 */
	//private Map<String, ArrayList<Entry>> dictionaryWords = new HashMap<>();
	private Map<String, Map<String, Entry>> dictionaryWords = new HashMap<>();
	private List<String> words = new ArrayList<String>();
	private List<String> pos = new ArrayList<String>();
	private Map<String, List<String>> wordsToExamples = new HashMap<>();
	/** Maps words to a map that maps parts of speech (representing entries) to lists of examples using their alternate forms */
	private Map<String, Map<String, List<String>>> entriesToExamples = new HashMap<>();
	/**
	 * The integers <b>before</b> and <b>after</b> hold the number of characters before and after the search word that the user wants to be shown. Their default values, respectively, are <i>20</i> and <i>50</i>, and the variables can be customized under <i>Edit > Resource Settings</i> in the menu bar.
	 */
	private int before = 20, after = 50;
	private boolean frequency = false, alphabetical = false;
	/** If a user opens a file, this variable will hold the value of the full path of the file for saving purposes. */
	private File openedFile = null;
	private ArrayList<String[]> entryViewInfo;

	/* Content Panel Variables */
	private JScrollPane wordPanel;
	private DefaultListModel<String> wordListModel = new DefaultListModel<>();
	private JList<String> listOfWords = new JList<>(wordListModel);
	private List<String> resourceFNs = new ArrayList<>();
	private final DefaultListModel<String> exampleListModel = new DefaultListModel<>();
	private final DefaultListModel<String> formsExListModel = new DefaultListModel<>();
	private final JComboBox<String> dropDownList = new JComboBox<>();

	/** Adds default part of speech tags and generates the JFrame and its contents. */
	public DictionaryEditor() {
		addPOSTags("/home/rheadedhia/Documents/pos2.txt", true, true);
		setSize(700, 800);

		createMenuBar();
		createContent();

		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	/** Creates the menu bar. */
	@SuppressWarnings("serial")
	public void createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		ImageIcon openIcon = new ImageIcon("/home/rheadedhia/workspace/jp/src/images/open.gif");
		ImageIcon saveIcon = new ImageIcon("/home/rheadedhia/workspace/jp/src/images/save.gif");
		ImageIcon saveAsIcon = new ImageIcon("/home/rheadedhia/workspace/jp/src/images/saveas.gif");
		final DictionaryEditor DE = this;

		JMenu file = new JMenu("File");
		JMenuItem open = new JMenuItem(new AbstractAction("Open", openIcon) {
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked open");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("xml files (*.xml)", "xml"));
				fileChooser.setDialogTitle("Open .xml File");
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String filename = file.getAbsolutePath();
					System.out.println("Opening .xml file: " + filename);
					openXML(filename, file);
				}
			}
		});
		JMenuItem save = new JMenuItem(new AbstractAction("Save", saveIcon) {
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked save");
				if (openedFile != null) {
					saveDictEntries(openedFile);
				} else {
					JOptionPane.showMessageDialog(DE, "Please click \"Save As\" or \"Open\" to create or choose a file to save your words to.", "Error", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		JMenuItem saveAs = new JMenuItem(new AbstractAction("Save As", saveAsIcon) {
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked save as");
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("/home/me/Documents"));
				int retrival = chooser.showSaveDialog(null);
				if (retrival == JFileChooser.APPROVE_OPTION) {
					try {
						File file = chooser.getSelectedFile();
						if (!file.toString().substring(file.toString().length() - 3).equalsIgnoreCase("xml")) {
							file = new File(file.toString() + ".xml");
						}
						saveDictEntries(file);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		JMenuItem viewUnsaved = new JMenuItem(new AbstractAction("View Entires") {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked view entries");
				JFrame frame = new JFrame();
				DefaultListModel<String> model = new DefaultListModel<>();
				entryViewInfo = populateModel(model);
				final JList<String> theWordList = new JList<>(model);
				theWordList.setBorder(new EmptyBorder(10, 10, 10, 10));
				theWordList.setFont(new Font("serif", Font.PLAIN, 14));
				theWordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				MyCellRenderer cellRenderer = new MyCellRenderer(330);
				theWordList.setCellRenderer(cellRenderer);
				theWordList.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (!e.getValueIsAdjusting() && theWordList.getSelectedValue() != null) {// This line prevents double events
							int i = theWordList.getSelectedIndex();
							System.out.println(i + " " + entryViewInfo.get(i)[0] + (entryViewInfo.get(i).length==2?" "+entryViewInfo.get(i)[1]:""));
						}
					}
				});
				theWordList.addMouseListener(new MousePopupListener(theWordList, genViewPM(model, theWordList), DE));
				JScrollPane scroll = new JScrollPane(theWordList);
				frame.getContentPane().add(scroll);
				displFrame(frame, 480, 450, false);
			}
		});
		file.add(open);
		file.add(save);
		file.add(saveAs);
		file.addSeparator();
		file.add(viewUnsaved);
		menuBar.add(file);

		JMenu edit = new JMenu("Edit");
		JMenuItem openWords = new JMenuItem(new AbstractAction("Add Search Words", openIcon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked add search words");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("txt files (*.txt)", "txt"));
				fileChooser.setDialogTitle("Open .txt File With Search Words");
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String filename = fileChooser.getSelectedFile().getAbsolutePath();
					System.out.println("Opening .txt file: " + filename);
					addSearchWords(filename);
				}
			}
		});
		JMenuItem openText = new JMenuItem(new AbstractAction("Add Resource", openIcon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked open resource file");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("txt files (*.txt)", "txt"));
				fileChooser.setDialogTitle("Open .txt File");
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String filename = fileChooser.getSelectedFile().getAbsolutePath();
					System.out.println("Opening .txt file: " + filename);
					if (!resourceFNs.contains(filename)) {
						resourceFNs.add(filename);
						if (words.size()>0) {
							mapToExamples(words, wordsToExamples, exampleListModel, null);
							for (String word : words) {
								for (Entry entry : dictionaryWords.get(word).values()) {
									if (entry.getForms().size()>0) {
										mapToExamples(entry.getForms(), entriesToExamples.get(word), null, entry.getPOS());
									}
								}
							}
						}
					}
				}
			}
		});
		JMenuItem openPOS = new JMenuItem(new AbstractAction("Add POS Tags", openIcon) {
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked add pos tags");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("txt files (*.txt)", "txt"));
				fileChooser.setDialogTitle("Open .txt File");
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String filename = fileChooser.getSelectedFile().getAbsolutePath();
					System.out.println("Opening .txt file: " + filename);
					addPOSTags(filename, false, true);
				}
			}
		});
		JMenuItem setPOS = new JMenuItem(new AbstractAction("Set POS Tags", openIcon) {
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked set pos tags");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("txt files (*.txt)", "txt"));
				fileChooser.setDialogTitle("Open .txt File");
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String filename = fileChooser.getSelectedFile().getAbsolutePath();
					System.out.println("Opening .txt file: " + filename);
					addPOSTags(filename, false, false);
				}
			}
		});
		JMenuItem typeWords = new JMenuItem(new AbstractAction("Type Search Words") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame();
				JPanel panel = new JPanel();
				final JTextField textField = new JTextField(10);
				JButton button = new JButton("Add Search Word");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String text = textField.getText();
						if (text.length() > 0) {
							words.add(text);
							textField.setText("");
							List<String> wd = new ArrayList<String>();
							wd.add(text);
							updateWordPanel(wd, true);
						}
					}
				});
				panel.add(textField);
				panel.add(button);
				frame.add(panel);
				displFrame(frame, 200, 100, true);
			}
		});
		JMenuItem typePOS = new JMenuItem(new AbstractAction("Type POS Tags") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame();
				JPanel panel = new JPanel();
				final JTextField textField = new JTextField(10);
				JButton button = new JButton("Add POS Tag");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String text = textField.getText();
						if (text.length() > 0) {
							pos.add(text);
							textField.setText("");
							updatePOSTags(dictionaryWords.get(listOfWords.getSelectedValue()));
						}
					}
				});
				panel.add(textField);
				panel.add(button);
				frame.add(panel);
				displFrame(frame, 200, 100, true);
			}
		});
		JMenuItem resourceSettings = new JMenuItem(new AbstractAction("Settings") {
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked settings");
				JFrame frame = new JFrame();
				Box hold = Box.createVerticalBox();
				frame.add(hold);

				/**
				 * Resource settings
				 */

				JPanel rS = new JPanel();
				rS.add(new JLabel("<html><h3>Examples</h3></html>"));
				hold.add(rS);

				JPanel getCharNum = new JPanel(new GridLayout(0, 1));
				Box bef = Box.createHorizontalBox();
				bef.add(new JLabel("Number of characters to show before the search word:  "));
				final JTextField beforeTF = new JTextFieldLimit(2);
				beforeTF.setColumns(2);
				beforeTF.setToolTipText("<html>Currently set to <font color=red>" + before + "</font>. If you<br>don't want to change this<br>value, leave this field blank.<html>");
				bef.add(beforeTF);
				getCharNum.add(bef);
				Box aft = Box.createHorizontalBox();
				aft.add(new JLabel("Number of characters to show after the search word:     "));
				final JTextField afterTF = new JTextFieldLimit(2);
				afterTF.setColumns(2);
				afterTF.setToolTipText("<html>Currently set to <font color=red>" + after + "</font>. If you<br>don't want to change this<br>value, leave this field blank.<html>");
				aft.add(afterTF);
				getCharNum.add(aft);
				JPanel centerRS = new JPanel();
				centerRS.add(getCharNum);
				hold.add(centerRS);

				hold.add(new JSeparator(SwingConstants.HORIZONTAL));

				/**
				 * Word Panel Settings
				 */

				JPanel wPS = new JPanel();
				wPS.add(new JLabel("<html><h3>How do you want to order the search words?</h3></html>"));
				hold.add(wPS);

				JPanel orderOptions = new JPanel(new GridLayout(0, 1));
				JRadioButton freq = new JRadioButton("By Their Frequency in Resources");
				freq.setActionCommand("freq");
				JRadioButton alpha = new JRadioButton("Alphabetically");
				alpha.setActionCommand("alpha");
				JRadioButton orig = new JRadioButton("By the Inputted Order");
				orig.setActionCommand("orig");

				// Make current option selected
				final String[] choice = new String[1];
				final String[] old = new String[1];
				if (frequency) {
					freq.setSelected(true);
					choice[0] = "freq";
				} else if (alphabetical) {
					alpha.setSelected(true);
					choice[0] = "alpha";
				} else {
					orig.setSelected(true);
					choice[0] = "orig";
				}
				old[0] = choice[0];
				
				// Group the radio buttons.
				ButtonGroup group = new ButtonGroup();
				group.add(freq);
				group.add(alpha);
				group.add(orig);
				ActionListener listener = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						choice[0] = e.getActionCommand();
						System.out.println("You selected: " + choice);
					}
				};
				// Register a listener for the radio buttons.
				freq.addActionListener(listener);
				alpha.addActionListener(listener);
				orig.addActionListener(listener);
				// Put the radio buttons in a column in a panel.
				orderOptions.add(freq);
				orderOptions.add(alpha);
				orderOptions.add(orig);
				JPanel centerOO = new JPanel();
				centerOO.add(orderOptions);
				hold.add(centerOO);

				hold.add(new JSeparator(SwingConstants.HORIZONTAL));

				/**
				 * Update Settings
				 */

				JPanel panelB = new JPanel();
				JButton button = new JButton("Update Settings");
				panelB.add(button);
				hold.add(panelB);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String b = beforeTF.getText().replaceAll("\\s+", "");
						String a = afterTF.getText().replaceAll("\\s+", "");
						if (b.length() > 0 && a.length() > 0 && !b.matches(".*\\D.*") && !a.matches(".*\\D.*")) {
							if (before != Integer.parseInt(b) || after != Integer.parseInt(a)) {
								before = Integer.parseInt(b);
								after = Integer.parseInt(a);
								System.out.println("before: " + before + ", after: " + after);
								// Updates examples
								mapToExamples(words, wordsToExamples, exampleListModel, null);
								for (String ex : safe(wordsToExamples.get(listOfWords.getSelectedValue()))) {
									exampleListModel.clear();
									exampleListModel.addElement(ex);
								}
							}
							beforeTF.setText("");
							afterTF.setText("");
						}
						if (!choice[0].equals(old[0])) { // If the user changed the order from the old setting
							switch (choice[0]) {
							case "freq": {
								frequency = true;
								alphabetical = false;
								System.out.println("frequency");
								break;
							}
							case "alpha": {
								alphabetical = true;
								frequency = false;
								System.out.println("alphabetical");
								break;
							}
							case "orig": {
								frequency = false;
								alphabetical = false;
								System.out.println("original");
								break;
							}
							}
							choice[0] = "";
							updateWordPanel(words, false);
						}
					}
				});
				
				// Display Frame
				displFrame(frame, 500, 300, true);
			}
		});
		edit.add(openWords);
		edit.add(openText);
		edit.add(openPOS);
		edit.add(setPOS);
		edit.addSeparator();
		edit.add(typeWords);
		edit.add(typePOS);
		edit.add(resourceSettings);
		menuBar.add(edit);

		setJMenuBar(menuBar);
	}

	/** Populates the default list model that contains a formatted version of the dictionary words. Creates an ArrayList
	 * of arrays that maps the index of an item in the list model (stored as the index of the item in the array) to 
	 * the word the item corresponds to, and, if the item is an entry and not a word, the part of speech of that entry.*/
	public ArrayList<String[]> populateModel(DefaultListModel<String> model) {
		ArrayList<String[]> array = new ArrayList<>();
		for (String word : dictionaryWords.keySet()) {
			String dW = "<html><h2>" + word + "</h2></html>";
			System.out.println(dW);
			model.addElement(dW);
			array.add(new String[] {word});
			for (Entry entry : dictionaryWords.get(word).values()) {
				//System.out.println("pos: " + entry.getPOS() + ", " + "def: " + entry.getDef() + ", " + entry.getNotes() + ", " + entry.getExamples() + ", " + entry.getForms());
				dW = popModEntry(dW, entry, model, array, word);
			}
		}
		
		return array;
	}
	
	/** Helper method for populate model; called by methods that are used to edit Entry View and dictionaryWords. */
	private String popModEntry(String dW, Entry entry, DefaultListModel<String> model, ArrayList<String[]> array, String word) {
		dW = "<html><b>" + entry.getPOS() + "</b>";
		//System.out.println("POS TAG: " + entry.getPOS());
		if (entry.getDef().size() > 0) {
			dW += "<ol>";
			for (String d : entry.getDef()) {
				dW += "<li>" + d + "</li>";
			}
			dW += "</ol>";
		}
		if (entry.getExamples().size() > 0) {
			dW += "Examples";
			dW += "<ul>";
			for (String ex : entry.getExamples()) {
				dW += "<li>" + ex.replaceAll("<html>|</html>", "") + "</li>";
			}
			dW += "</ul>";
		}
		if (entry.getNotes().length() > 0) {
			dW += "<b>Notes</b>: " + entry.getNotes();
		}
		if (entry.getForms().size()>0) {
			dW += "<br>Other Forms of the Word: ";
			for (int i = 0; i < entry.getForms().size(); i++) {
				dW += entry.getForms().get(i);
				// Add a comma between forms unless the form being added is the last one
				if (i != entry.getForms().size()-1) {
					dW += ", ";
				}
			}
		}
		dW += "<br><br></html>";
		//System.out.println(dW);
		model.addElement(dW);
		array.add(new String[] {word, entry.getPOS()});
		
		return dW;
	}
	
	/** Saves the entries into an XML file. */
	public void saveDictEntries(File file) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// root element (can only be one)
			Element dictionary = doc.createElement("dictionary");
			doc.appendChild(dictionary);

			for (String w : dictionaryWords.keySet()) {
				Element word = doc.createElement("word");
				word.setAttribute("id", w);
				dictionary.appendChild(word);

				for (Entry e : dictionaryWords.get(w).values()) {
					Element entry = doc.createElement("entry");
					word.appendChild(entry);

					Element pos = doc.createElement("pos");
					pos.appendChild(doc.createTextNode(e.getPOS()));
					entry.appendChild(pos);

					Element definitions = doc.createElement("definitions");
					entry.appendChild(definitions);

					for (String d : e.getDef()) {
						Element def = doc.createElement("def");
						def.appendChild(doc.createTextNode(d));
						definitions.appendChild(def);
					}

					Element notes = doc.createElement("notes");
					notes.appendChild(doc.createTextNode(e.getNotes()));
					entry.appendChild(notes);

					Element examples = doc.createElement("examples");
					entry.appendChild(examples);
					for (String example : e.getExamples()) {
						Element ex = doc.createElement("ex");
						ex.appendChild(doc.createTextNode(example));
						examples.appendChild(ex);
					}
					
					Element forms = doc.createElement("forms");
					entry.appendChild(forms);
					for (String f : e.getForms()) {
						Element form = doc.createElement("form");
						form.appendChild(doc.createTextNode(f));
						forms.appendChild(form);
					}
				}
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	/** Opens XML files that are formatted the way the method saveDictEntries formats the XML files that it saves */
	public void openXML(String filename, File file) {
		if (validateXMLSchema(filename)) {
			try {
				List<String> fileWords = new ArrayList<>();
				String word = "";
				Entry entry = new Entry();
				XMLInputFactory inputFactory = XMLInputFactory.newInstance();
				InputStream in = new FileInputStream(filename);
				XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
				streamReader.nextTag(); // Advance to "dictionary" element
				streamReader.nextTag(); // Advance to "word" element
				while (streamReader.hasNext()) {
					if (streamReader.isStartElement()) {
						switch (streamReader.getLocalName()) {
						case "word": {
							word = "";
							word = streamReader.getAttributeValue(0);
							fileWords.add(word);
							break;
						}
						case "pos": {
							entry.setPOS(streamReader.getElementText());
							break;
						}
						case "def": {
							entry.addDef(streamReader.getElementText());
							break;
						}
						case "notes": {
							entry.setNotes(streamReader.getElementText());
							break;
						}
						case "ex": {
							entry.addExample(streamReader.getElementText());
							break;
						}
						case "form": {
							entry.addForm(streamReader.getElementText());
						}
						}
					} else if (streamReader.isEndElement() && streamReader.getLocalName().equals("entry")) {
						System.out.println("End of Entry");
						if (dictionaryWords.containsKey(word)) {
							dictionaryWords.get(word).put(entry.getPOS(), entry);
						} else {
							Map<String, Entry> list = new HashMap<>();
							list.put(entry.getPOS(), entry);
							dictionaryWords.put(word, list);
						}
						entry = new Entry();
					}

					streamReader.next();
				}

				for (String s : dictionaryWords.keySet()) {
					System.out.println("word: " + s);
					for (Entry e : dictionaryWords.get(s).values()) {
						System.out.println("pos: " + e.getPOS());
						System.out.println("def: " + e.getDef());
						System.out.println("notes: " + e.getNotes());
						System.out.println("examples: " + e.getExamples());
						System.out.println("forms: " + e.getForms());
					}
				}

				// If file is valid and is opened successfully:				
				// Set openedFile equal to the full path name of the file.
				openedFile = file;				
				// Add words from file to word panel.
				words.addAll(fileWords);
				updateWordPanel(fileWords, true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean validateXMLSchema(String xmlPath) {
		String xsdPath = "/home/rheadedhia/schema.xsd"; // xml validation schema file
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new File(xsdPath));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new File(xmlPath)));
		} catch (IOException | SAXException e) {
			System.out.println("Exception: " + e.getMessage());
			System.out.println("invalid xml file");
			return false;
		}
		System.out.println("valid xml file");
		return true;
	}

	/**
	 * Adds the words in the file to the list of words to search for in the given resources and calls updateWordPanel, 
	 * which adds them to the panel of words on the left.
	 */
	public void addSearchWords(String filename) {
		List<String> newWords = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String thisLine = "";
			while ((thisLine = in.readLine()) != null) {
				if (!thisLine.equals("")) {
					words.add(thisLine.trim());
					newWords.add(thisLine.trim());
				}
			}
			in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateWordPanel(newWords, true);
	}

	/**
	 * Adds the words in the file to the list of part of speech tags that the user can attach to word entries. Called 
	 * whenever part of speech tags are added through a file. The boolean start is true when the method is called in the 
	 * constructor; otherwise it is false. The boolean keepOld is true when the user wants to keep old part of speech 
	 * tags (when tags are added with "Add POS Tags"); otherwise (when tags are added with "Set POS Tags") old POS tags are removed.
	 */
	public void addPOSTags(String fn, boolean start, boolean keepOld) {
		if (!keepOld) {
			pos = new ArrayList<String>();
		}
		try {
			BufferedReader in = new BufferedReader(new FileReader(fn));
			String thisLine = "";
			while ((thisLine = in.readLine()) != null) {
				if (!thisLine.equals("")) {
					pos.add(thisLine.trim());
				}
			}
			in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!start)
			updatePOSTags(dictionaryWords.get(listOfWords.getSelectedValue()));
	}

	/** Updates the drop down list that contains the POS tags. Called whenever POS tags are added by the user. */
	public void updatePOSTags(Map<String, Entry> entries) {
		dropDownList.removeAllItems();
		//for (String p : pos) {
		//	dropDownList.addItem(p);
		//}
		genPOSDDL(dropDownList, entries);
		dropDownList.validate();
		dropDownList.updateUI();
	}

	/** Creates the main panel that allows users to add entries into their dictionary. */
	public void createContent() {

		/*
		 * Create lists, their models, and the scroll panes which will contain them.
		 */

		final JList<String> exampleList = new JList<>(exampleListModel);
		final JScrollPane examples = new JScrollPane(exampleList);
		
		final JList<String> formsExList = new JList<>(formsExListModel);
		final JScrollPane formsExs = new JScrollPane(formsExList);

		listOfWords.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && listOfWords.getSelectedValue() != null) {// This line prevents double events
					System.out.println("Word changed to: " + listOfWords.getSelectedValue());
					updatePOSTags(dictionaryWords.get(listOfWords.getSelectedValue()));
					exampleListModel.clear();
					for (String ex : safe(wordsToExamples.get(listOfWords.getSelectedValue())))
						exampleListModel.addElement(ex);
				}
			}
		});

		final DefaultListModel<String> defListModel = new DefaultListModel<>();
		final JList<String> listOfDefs = new JList<>(defListModel);
		final JScrollPane addedDefs = new JScrollPane(listOfDefs);
		listOfDefs.addMouseListener(new MousePopupListener(listOfDefs, new JPopupMenu[] {genMainPM(defListModel, listOfDefs, "Definition(s)")}, this));

		final DefaultListModel<String> aEListModel = new DefaultListModel<>();
		final JList<String> listOfAddedExamples = new JList<String>(aEListModel);
		final JScrollPane addedExamples = new JScrollPane(listOfAddedExamples);
		listOfAddedExamples.addMouseListener(new MousePopupListener(listOfAddedExamples, new JPopupMenu[] {genMainPM(aEListModel, listOfAddedExamples, "Example(s)")}, this));
		
		final DefaultListModel<String> formsListModel = new DefaultListModel<>();
		final JList<String> listOfForms = new JList<>(formsListModel);
		final JScrollPane addedForms = new JScrollPane(listOfForms);
		listOfForms.addMouseListener(new MousePopupListener(listOfForms, new JPopupMenu[] {genMainPM(formsListModel, listOfForms, "Form(s)")}, this));
		
		/*
		 * Creates word panel.
		 */

		listOfWords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOfWords.setSelectedIndex(0);
		wordPanel = new JScrollPane(listOfWords);
		wordPanel.setPreferredSize(new Dimension((int) (getWidth() * .2), getHeight()));
		JViewport header = new JViewport();
		header.setView(new JLabel("Les Mots"));
		wordPanel.setColumnHeader(header);

		/*
		 * Creates content panel.
		 */

		// Entry panel (contains everything related to creating entries)
		JPanel entryPanel = new JPanel();
		Box vertical = Box.createVerticalBox();
		vertical.setPreferredSize(new Dimension((int) (getWidth() * .65), getHeight() - 55));
		entryPanel.add(vertical);
		
		// Other forms of word panel
		JPanel otherFormsPanel = new JPanel();
		final JTextArea formsText = new JTextArea(3, 15);
		formsText.setLineWrap(true);
		formsText.setWrapStyleWord(true);
		JScrollPane scrollForms = new JScrollPane(formsText);
		otherFormsPanel.add(scrollForms);
		JButton addForm = new JButton("<html>Add<br>Form</html>");
		addForm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Add forms pressed");
				String form = formsText.getText().trim();
				System.out.println("Form: " + form);
				if (form.length()>0) {
					formsListModel.addElement(form);
					formsText.setText("");
				} 
			}
		});
		otherFormsPanel.add(addForm);
		vertical.add(otherFormsPanel);
		
		// Added Forms Panel
		JPanel addedFormsPanel = new JPanel();
		addedFormsPanel.add(new JLabel("<html>Other Forms<br>of the Word</html>"));
		addedForms.setPreferredSize(new Dimension((int) (getWidth() * .45), 70));
		addedFormsPanel.add(addedForms);
		vertical.add(addedFormsPanel);
		
		addSeparator(vertical);
		
		// Part Of Speech panel
		JPanel posPanel = new JPanel();
		posPanel.add(new JLabel("Set POS: "));
		for (int i = 0; i < pos.size(); i++) {
			dropDownList.addItem(pos.get(i));
		}
		posPanel.add(dropDownList);
		vertical.add(posPanel);

		addSeparator(vertical);

		// Definition panel
		JPanel defPanel = new JPanel();
		final JTextArea defText = new JTextArea(3, 15);
		defText.setLineWrap(true);
		defText.setWrapStyleWord(true);
		JScrollPane scrollDef = new JScrollPane(defText);
		defPanel.add(scrollDef);
		JButton addDef = new JButton("<html>Add<br/>Definition</html>");
		addDef.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Add Definition pressed");
				String def = defText.getText();
				System.out.println("Definition: " + def);
				if (def.length()>0) {
					defListModel.addElement(def);
					defText.setText("");
				}
			}
		});
		defPanel.add(addDef);
		vertical.add(defPanel);

		// Added Definitions panel
		JPanel addedDefsPanel = new JPanel();
		addedDefsPanel.add(new JLabel("<html>Your<br/>Defs:</html>"));
		addedDefs.setPreferredSize(new Dimension((int) (getWidth() * .45), 70));
		addedDefsPanel.add(addedDefs);
		vertical.add(addedDefsPanel);

		addSeparator(vertical);

		// Notes panel
		JPanel notesPanel = new JPanel();
		notesPanel.add(new JLabel("Notes: "));
		final JTextArea notes = new JTextArea(3, 20);
		notes.setLineWrap(true);
		notes.setWrapStyleWord(true);
		JScrollPane scrollNotes = new JScrollPane(notes);
		notesPanel.add(scrollNotes);
		vertical.add(notesPanel);

		addSeparator(vertical);
		
		/** EXAMPLES **/
		
		// Examples panel
		JPanel examplesPanel = new JPanel();
		examplesPanel.add(new JLabel("<html>Examples<br>Using<br>Base<br>Word:</html>"));
		examples.setPreferredSize(new Dimension((int) (getWidth() * .35), 70));
		examplesPanel.add(examples);
		JButton addExample = new JButton("<html>Add<br/>Example(s)</html>");
		addExample.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Add Example(s) clicked");
				for (int index : exampleList.getSelectedIndices()) {
					System.out.println(exampleList.getModel().getElementAt(index));
					aEListModel.addElement(exampleList.getModel().getElementAt(index));
				}
			}
		});
		examplesPanel.add(addExample);
		vertical.add(examplesPanel);
		
		// Other Forms Examples panel
		JPanel formsExPanel = new JPanel();
		formsExPanel.add(new JLabel("<html>Examples<br>Using<br>Added<br>Forms:</html>"));
		formsExs.setPreferredSize(new Dimension((int) (getWidth() * .35), 70));
		formsExPanel.add(formsExs);
		Box vertButtons = Box.createVerticalBox();
		formsExPanel.add(vertButtons);
		JButton updateFormsEx = new JButton("Update");
		updateFormsEx.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Clicked update (forms)");
				if (formsListModel.getSize()>0) {
					ArrayList<String> currentForms = new ArrayList<>();
					for (int i = 0; i < formsListModel.getSize(); i++) {
						currentForms.add(formsListModel.get(i));
					}
					for (String s : currentForms) System.out.println(s);
					mapToExamples(currentForms, null, formsExListModel, null);					
				} else {
					formsExListModel.clear();
				}
			}
		});
		vertButtons.add(updateFormsEx);
		JButton addFormsEx = new JButton("<html>Add<br/>Example(s)</html>");
		addFormsEx.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Clicked add examples (forms)");
				for (int index : formsExList.getSelectedIndices()) {
					System.out.println(formsExList.getModel().getElementAt(index));
					aEListModel.addElement(formsExList.getModel().getElementAt(index));
				}
			}
		});
		vertButtons.add(addFormsEx);
		vertical.add(formsExPanel);

		// Added Examples panel
		JPanel addedExamplesPanel = new JPanel();
		addedExamplesPanel.add(new JLabel("<html>Chosen<br/>Example(s):</html>"));
		addedExamples.setPreferredSize(new Dimension((int) (getWidth() * .35), 70));
		addedExamplesPanel.add(addedExamples);
		vertical.add(addedExamplesPanel);

		addSeparator(vertical);

		// panel with Add Entry button (call another function to do all the things, and pass in info as parameters)
		JPanel addEntryPanel = new JPanel();
		JButton addEntryB = new JButton("Add Entry");
		final DictionaryEditor DE = this;
		addEntryB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (wordListModel.size() > 0 && listOfWords.getSelectedIndex() != -1) {
					List<String> ds = new ArrayList<>();
					List<String> aes = new ArrayList<>();
					List<String> forms = new ArrayList<>();
					for (int i = 0; i < defListModel.getSize(); i++)
						ds.add(defListModel.get(i));
					defListModel.clear();
					for (int i = 0; i < aEListModel.getSize(); i++)
						aes.add(aEListModel.get(i));
					aEListModel.clear();
					for (int i = 0; i < formsListModel.getSize(); i++)
						forms.add(formsListModel.get(i));
					formsListModel.clear();
					String posTag = dropDownList.getSelectedItem().toString();
					String word = listOfWords.getSelectedValue();
					addEntry(word, posTag, ds, aes, notes.getText(), forms);
					notes.setText("");
					updatePOSTags(dictionaryWords.get(listOfWords.getSelectedValue()));
					mapToExamples(forms, entriesToExamples.get(word), null, posTag);
				} else {
					JOptionPane.showMessageDialog(DE, "To add words, go to \"Edit\" and click \"Type Search Words\" or \"Add Search Words\".", "ERROR: NO WORD SELECTED", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		addEntryPanel.add(addEntryB);
		vertical.add(addEntryPanel);

		getContentPane().add(wordPanel, BorderLayout.WEST);
		getContentPane().add(entryPanel, BorderLayout.CENTER);

	}
	
	/** Used to generate pop up menus for the main GUI. */
	public JPopupMenu genMainPM(final DefaultListModel<String> model, final JList<String> list, String type) {
		JPopupMenu popup = new JPopupMenu();
		ActionListener menuListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.out.println("Popup menu item [" + event.getActionCommand() + "] was pressed.");
				int[] s = list.getSelectedIndices();
				for (int num = 0; num < s.length; num++) {
					//Every time an item is removed, the indices of all the other elements decrease by one; this accounts for that by using the
					s[num] -= num; //fact that .getSelectedIndices returns an array of all of the selected indices in increasing order.
					System.out.println("removing item " + model.get(s[num]) + " at index " + s[num]);
					model.removeElementAt(s[num]);
				}
			}
		};
		JMenuItem item;
		popup.add(item = new JMenuItem("Remove Selected " + type));
		item.addActionListener(menuListener);		
		return popup;
	}
	
	/** Used to generate pop up menus for the view entry frame. */
	public JPopupMenu[] genViewPM(final DefaultListModel<String> model, final JList<String> list) {
		JPopupMenu entryPM = new JPopupMenu();
		ActionListener entryListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String choice = event.getActionCommand();
				int index = list.getSelectedIndex();
				String word = entryViewInfo.get(index)[0], pos = entryViewInfo.get(index)[1];
				System.out.println(choice + " was pressed at index " + index);
				switch(choice) {
					case "Edit Part Of Speech Tag": {
						editPOS(word, pos, model, index);
						break;
					} case "Edit Definitions": {
						editDefs(word, pos, model, index);
						break;
					} case "Edit Note": {
						editNote(word, pos, model, index);
						break;
					} case "Edit Examples": {
						editExs(word, pos, model, index);
						break;
					} case "Remove Entry": {
						remEntry(word, pos, model, index);
						break;
					} case "Edit Forms": {
						editForms(word, pos, model, index);
						break;
					}
				}
			}
		};
		JMenuItem item;
		entryPM.add(item = new JMenuItem("Edit Part Of Speech Tag"));
		item.addActionListener(entryListener);
		entryPM.add(item = new JMenuItem("Edit Definitions"));
		item.addActionListener(entryListener);
		entryPM.add(item = new JMenuItem("Edit Note"));
		item.addActionListener(entryListener);
		entryPM.add(item = new JMenuItem("Edit Examples"));
		item.addActionListener(entryListener);
		entryPM.add(item = new JMenuItem("Edit Forms"));
		item.addActionListener(entryListener);
		entryPM.add(item = new JMenuItem("Remove Entry"));
		item.addActionListener(entryListener);
		
		JPopupMenu wordPM = new JPopupMenu();
		ActionListener wordListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Gets word to be removed
				int index = list.getSelectedIndex();
				String word = entryViewInfo.get(index)[0];
				// Removes the word
				dictionaryWords.remove(word);
				// Update the Entry View and the variable entryViewInfo
				model.remove(index);
				entryViewInfo.remove(index);
			}
		};
		wordPM.add(item = new JMenuItem("Remove Word"));
		item.addActionListener(wordListener);
		
		return new JPopupMenu[] {wordPM, entryPM};
	}
	
	/**Opens frame to edit POS tag of entry selected in Entry View then updates Entry View */
	public void editPOS(final String word, final String oldPOS, final DefaultListModel<String> model, final int indexInEV) {
		final JFrame frame = new JFrame();
		
		/** Main panel **/		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		Box vertical = Box.createVerticalBox();
		panel.add(vertical);
		
		/** Part Of Speech panel **/		
		// Create drop down menu
		final JComboBox<String> posDDL = new JComboBox<String>();
		genPOSDDL(posDDL, dictionaryWords.get(word));
		JPanel posPanel = new JPanel();
		posPanel.add(new JLabel("Choose New POS Tag: "));
		posPanel.add(posDDL);
		vertical.add(posPanel);
		
		/** Button panel **/		
		JPanel buttonPanel = new JPanel();
		JButton setPOS = new JButton("Set POS");
		setPOS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String chosenPOS = posDDL.getSelectedItem().toString();
				System.out.println("You clicked set pos to " + chosenPOS);
				dictionaryWords.get(word).get(oldPOS).setPOS(chosenPOS); //sets POS tag in entry to new POS tag
				Entry updatedEntry = dictionaryWords.get(word).get(chosenPOS); //saves entry
				dictionaryWords.get(word).remove(oldPOS); //removes old POS tag key
				dictionaryWords.get(word).put(chosenPOS, updatedEntry); // maps new key to saved entry

				// Update model for entry view list and update the variable entryViewInfo
				String newEntry = "<html>";
				newEntry = popModEntry(newEntry, updatedEntry, model, new ArrayList<String[]>(), word);
				model.setElementAt(newEntry, indexInEV);
				System.out.println(newEntry);
				entryViewInfo.get(indexInEV)[1] = chosenPOS;
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		buttonPanel.add(setPOS);
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		buttonPanel.add(close);
		vertical.add(buttonPanel);		
		
		/** Display Frame **/
		displFrame(frame, 300, 100, true);
	}
	
	/**Opens frame to edit forms of entry selected in Entry View then updates Entry View */
	public void editForms(final String word, final String posTag, final DefaultListModel<String> model, final int indexInEV) {
		final JFrame frame = new JFrame();
		
		/** Main Panel **/
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		Box vertical = Box.createVerticalBox();
		panel.add(vertical);
		
		/** Get Entry **/
		Entry currEntry = dictionaryWords.get(word).get(posTag);
		
		/** Generate JScrollPane, DefaultListModel, JList and mouse listener for added forms **/
		final DefaultListModel<String> formLiMod = new DefaultListModel<>();
		final JList<String> formList = new JList<>(formLiMod);
		final JScrollPane forms = new JScrollPane(formList);
		forms.setPreferredSize(new Dimension(450, 150));
		for (String form : safe(currEntry.getForms())) formLiMod.addElement(form);
		formList.addMouseListener(new MousePopupListener(formList, new JPopupMenu[] {genMainPM(formLiMod, formList, "Form(s)")}, this));
		
		/** Forms Panel **/
		JPanel formsPanel = new JPanel();
		final JTextArea formText = new JTextArea(4, 25);
		formText.setLineWrap(true);
		formText.setWrapStyleWord(true);
		JScrollPane scrollForms = new JScrollPane(formText);
		formsPanel.add(scrollForms);
		JButton addForm = new JButton("<html>Add<br/>Forms</html>");
		addForm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Add Forms pressed");
				String form = formText.getText();
				System.out.println("Form: " + form);
				formLiMod.addElement(form);
				formText.setText("");
			}
		});
		formsPanel.add(addForm);
		vertical.add(formsPanel);
		
		/** Added Forms Panel **/
		JPanel addedFormsPanel = new JPanel();
		addedFormsPanel.add(new JLabel("<html>Your<br/>Forms:</html>"));
		addedFormsPanel.add(forms);
		vertical.add(addedFormsPanel);
		
		/** Update Button **/
		JPanel button = new JPanel();
		vertical.add(button);
		JButton update = new JButton("Update");
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked update");
				ArrayList<String> newForms = new ArrayList<>();
				for (int i = 0; i < formLiMod.getSize(); i++) newForms.add(formLiMod.get(i));
				
				// debugging print line statement
				for (String s : newForms) System.out.println(s);
				
				// updates dictionaryWords with new forms
				dictionaryWords.get(word).get(posTag).setForms(newForms);
				
				// updates entriesToExamples with new forms
				mapToExamples(newForms, entriesToExamples.get(word), null, posTag);
				
				String newEntry = "<html>";
				newEntry = popModEntry(newEntry, dictionaryWords.get(word).get(posTag), model, new ArrayList<String[]>(), word);
				model.setElementAt(newEntry, indexInEV);
				System.out.println(newEntry);
				
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		button.add(update);
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		button.add(close);
		
		/** Display Frame **/
		displFrame(frame, 600, 300, true);	
	}
	
	/** Generates a POS Drop Down List that doesn't have POS Tags that have already been added to the word given as a parameter */
	public void genPOSDDL(JComboBox<String> ddl, Map<String, Entry> entries) {
		// String of POS tags that won't be added to the drop down menu b/c each entry for one word must have a unique POS tag
		String posToRemove = "";
		
		if (entries != null) {
			for (String partOfSpeech : safe(entries.keySet())) {
				posToRemove += partOfSpeech + " ";
			}
		}
		for (String p : pos) {
			if (!posToRemove.matches(".*\\b" + p + "\\b.*")) {
				ddl.addItem(p);
			}
		}
	}
	
	/**Opens frame to edit examples of entry selected in Entry View then updates dictionaryWords and Entry View */
	public void editExs(final String word, final String posTag, final DefaultListModel<String> model, final int indexInEV) {
		final JFrame frame = new JFrame();
		/** Main Panel **/
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		Box vertical = Box.createVerticalBox();
		panel.add(vertical);
		
		/** Get Entry **/
		// Using an array so that I can add a value to it after declaring it
		// and it has to be final because using value in actionPerformed method
		Entry currEntry = dictionaryWords.get(word).get(posTag);
		
		/** Generate JScrollPane, DefaultListModel, and JList for examples and added examples; add mouse listener to added examples**/
		//Examples
		final DefaultListModel<String> exLiMod = new DefaultListModel<>();
		final JList<String> exList = new JList<>(exLiMod);
		JScrollPane exs = new JScrollPane(exList);
		exs.setPreferredSize(new Dimension(350, 150));
		for (String ex : safe(wordsToExamples.get(word))) exLiMod.addElement(ex);
		//Other Forms Examples
		final DefaultListModel<String> formsExLiMod = new DefaultListModel<>();
		final JList<String> formsExList = new JList<>(formsExLiMod);
		JScrollPane formsExs = new JScrollPane(formsExList);
		formsExs.setPreferredSize(new Dimension(350, 150));
		for (String fEx : safe(entriesToExamples.get(word).get(posTag))) formsExLiMod.addElement(fEx);
		//Added Examples
		final DefaultListModel<String> aELiMod = new DefaultListModel<>();
		JList<String> aEList = new JList<>(aELiMod);
		final JScrollPane aExs = new JScrollPane(aEList);
		aExs.setPreferredSize(new Dimension(450, 150));
		for (String ex : safe(currEntry.getExamples())) aELiMod.addElement(ex);
		aEList.addMouseListener(new MousePopupListener(aEList, new JPopupMenu[] {genMainPM(aELiMod, aEList, "Example(s)")}, this));
		
		/** Examples **/
		JPanel ePanel = new JPanel();
		vertical.add(ePanel);
		ePanel.add(exs);
		JButton addExample = new JButton("<html>Add<br/>Example(s)</html>");
		addExample.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Add Example(s) clicked");
				for (int index : exList.getSelectedIndices()) {
					System.out.println(exLiMod.get(index));
					aELiMod.addElement(exLiMod.get(index));
				}
			}
		});
		ePanel.add(addExample);
		
		/** Other Forms Examples **/
		JPanel formsEPanel = new JPanel();
		vertical.add(formsEPanel);
		formsEPanel.add(formsExs);
		JButton addFormsExample = new JButton("<html>Add<br/>Example(s)</html>");
		addFormsExample.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Add Example(s) clicked (forms)");
				for (int index : formsExList.getSelectedIndices()) {
					System.out.println(formsExLiMod.get(index));
					aELiMod.addElement(formsExLiMod.get(index));
				}
			}
		});
		formsEPanel.add(addFormsExample);
		
		/** Added Examples **/
		JPanel aePanel = new JPanel();
		vertical.add(aePanel);
		aePanel.add(aExs);
		
		/** Update Button **/
		JPanel button = new JPanel();
		vertical.add(button);
		JButton update = new JButton("Update");
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked update");
				ArrayList<String> newExamples = new ArrayList<>();
				for (int i = 0; i < aELiMod.getSize(); i++) newExamples.add(aELiMod.get(i));
				
				dictionaryWords.get(word).get(posTag).setExamples(newExamples);
				
				String newEntry = "<html>";
				newEntry = popModEntry(newEntry, dictionaryWords.get(word).get(posTag), model, new ArrayList<String[]>(), word);
				model.setElementAt(newEntry, indexInEV);
				System.out.println(newEntry);
				
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		button.add(update);
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		button.add(close);
		
		displFrame(frame, 600, 550, true);
	}
	
	/**Opens frame to edit notes of entry selected in Entry View then updates dictionaryWords and Entry View*/
	public void editNote(final String word, final String posTag, final DefaultListModel<String> model, final int indexInEV) {
		final JFrame frame = new JFrame();
		
		/** Main Panel **/
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		Box vertical = Box.createVerticalBox();
		panel.add(vertical);
				
		/** Note Panel **/
		JPanel notePanel = new JPanel();
		final JTextArea noteText = new JTextArea(4, 25);
		noteText.setText(dictionaryWords.get(word).get(posTag).getNotes());
		noteText.setLineWrap(true);
		noteText.setWrapStyleWord(true);
		JScrollPane scrollNote = new JScrollPane(noteText);
		notePanel.add(scrollNote);
		vertical.add(notePanel);
		
		/** Buttons Panel **/
		JPanel buttons = new JPanel();
		JButton setNote = new JButton("Set New Note");
		setNote.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Set New Note pressed");
				String note = noteText.getText();
				System.out.println("New Note: " + note);
				
				dictionaryWords.get(word).get(posTag).setNotes(note);
				
				String newEntry = "<html>";
				newEntry = popModEntry(newEntry, dictionaryWords.get(word).get(posTag), model, new ArrayList<String[]>(), word);
				model.setElementAt(newEntry, indexInEV);
				System.out.println(newEntry);
				
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		buttons.add(setNote);
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		buttons.add(close);
		vertical.add(buttons);
		
		displFrame(frame, 400, 150, true);
		System.out.println("Edit Note Called");
	}
	
	/**Opens frame to edit definitions of entry selected in Entry View then updates dictionaryWords and Entry View*/
	public void editDefs(final String word, final String posTag, final DefaultListModel<String> model, final int indexInEV) {
		final JFrame frame = new JFrame();
		
		/** Main Panel **/
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		Box vertical = Box.createVerticalBox();
		panel.add(vertical);
		
		/** Get Entry **/
		Entry currEntry = dictionaryWords.get(word).get(posTag);
		
		/** Generate JScrollPane, DefaultListModel, JList and mouse listener for added definitions **/
		final DefaultListModel<String> defLiMod = new DefaultListModel<>();
		final JList<String> defList = new JList<>(defLiMod);
		final JScrollPane defs = new JScrollPane(defList);
		defs.setPreferredSize(new Dimension(450, 150));
		for (String def : safe(currEntry.getDef())) defLiMod.addElement(def);
		defList.addMouseListener(new MousePopupListener(defList, new JPopupMenu[] {genMainPM(defLiMod, defList, "Definition(s)")}, this));
		
		/** Definition Panel **/
		JPanel defPanel = new JPanel();
		final JTextArea defText = new JTextArea(4, 25);
		defText.setLineWrap(true);
		defText.setWrapStyleWord(true);
		JScrollPane scrollDef = new JScrollPane(defText);
		defPanel.add(scrollDef);
		JButton addDef = new JButton("<html>Add<br/>Definition</html>");
		addDef.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Add Definition pressed");
				String def = defText.getText();
				System.out.println("Definition: " + def);
				defLiMod.addElement(def);
				defText.setText("");
			}
		});
		defPanel.add(addDef);
		vertical.add(defPanel);
		
		/** Added Definitions Panel **/
		JPanel addedDefsPanel = new JPanel();
		addedDefsPanel.add(new JLabel("<html>Your<br/>Defs:</html>"));
		addedDefsPanel.add(defs);
		vertical.add(addedDefsPanel);
		
		/** Update Button **/
		JPanel button = new JPanel();
		vertical.add(button);
		JButton update = new JButton("Update");
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked update");
				ArrayList<String> newDefs = new ArrayList<>();
				for (int i = 0; i < defLiMod.getSize(); i++) newDefs.add(defLiMod.get(i));
				
				dictionaryWords.get(word).get(posTag).setDef(newDefs);
				
				String newEntry = "<html>";
				newEntry = popModEntry(newEntry, dictionaryWords.get(word).get(posTag), model, new ArrayList<String[]>(), word);
				model.setElementAt(newEntry, indexInEV);
				System.out.println(newEntry);
				
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		button.add(update);
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		button.add(close);
		
		/** Display Frame **/
		displFrame(frame, 600, 300, true);	
	}
	
	/** Removes entry selected in Entry View then updates dictionaryWords and Entry View*/
	public void remEntry(String word, String posTag, DefaultListModel<String> model, int indexInEV) {
		//Removes Entry
		dictionaryWords.get(word).remove(posTag);
				
		// Updates model for Entry View list and variable entryViewInfo
		model.remove(indexInEV);
		entryViewInfo.remove(indexInEV);
	}
	
	/** Sets size, location, and ability to be resized of frame given as parameter based on the other parameters and sets it visible */
	private void displFrame(JFrame frame, int w, int h, boolean resizable) {
		frame.setSize(w, h);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setResizable(resizable);
	}
	
	/**If the list inputed as a parameter is null, it returns an empty list. Otherwise, it returns the list.*/
	private List<String> safe(List<String> list) {
		return list == null ? new ArrayList<String>() : list;
	}

	/** If set is null, returns empty set. Otherwise, returns set. */
	private Set<String> safe(Set<String> set) {
		System.out.println(set == null);
		return set == null? new HashSet<String>() : set;
	}

	/** Adds a separator to the Box component given as a parameter (creates two rigid areas of dimension (0,5) and puts a JSeparator between them). */
	public void addSeparator(Box vertical) {
		vertical.add(Box.createRigidArea(new Dimension(0, 5)));
		vertical.add(new JSeparator());
		vertical.add(Box.createRigidArea(new Dimension(0, 5)));
	}

	/**Adds entry into dictionary after Add Entry button is clicked.*/
	public void addEntry(String word, String posTag, List<String> def, List<String> exs, String notes, List<String> forms) {
		if (dictionaryWords.containsKey(word)) {
			dictionaryWords.get(word).put(posTag, new Entry(posTag, def, notes, exs, forms));
			//dictionaryWords.get(word).add(new Entry(posTag, def, notes, exs));
		} else {
			Map<String, Entry> entryList = new HashMap<>();
			entryList.put(posTag, new Entry(posTag, def, notes, exs, forms));
			dictionaryWords.put(word, entryList);
		}
		for (String w : dictionaryWords.keySet()) {
			for (Entry e : dictionaryWords.get(w).values()) {
				System.out.println("Word: " + w);
				System.out.println("POS: " + e.getPOS());
				System.out.println("Def: " + e.getDef());
				System.out.println("Examples: " + e.getExamples());
				System.out.println("Notes: " + e.getNotes());
			}
		}
	}

	/** Maps given words/forms to instances of themselves in the resources. Called when words are added, forms are added, and
	 * resources are added by the user.
	 * Case 1: posTag is null => searching for words in resources.
	 * Case 2: posTag is not null and model is null => adding additional forms and their instances in the resources to the map 
	 * 		entriesToExamples.
	 * Case 3: posTag is null and map is null => searching for additional forms and their instances in the resources and adding
	 * 		those instances to the model.
	 */
	private void mapToExamples(List<String> wds, Map<String, List<String>> map, DefaultListModel<String> model, String posTag) {
		List<String> exs = new ArrayList<>(); // Use if map == null
		if (posTag != null) { //model is null
			map.put(posTag, new ArrayList<String>());
		}
		for (String filename : resourceFNs) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(filename));
				String prevLine = "", thisLine = "", nextLine = "", readLine = "";
				while ((readLine = in.readLine()) != null) {
					// makes sure that nextLine isn't assigned to null because it is looped through for instances
					// of the search word in the last line after the program has finished reading through the text.
					// this is done so that if the user wants to get more characters after the word than are in
					// thisLine, the user can get those characters from nextLine.
					nextLine = readLine;
					if (!nextLine.equals("")) {
						// Loop through all the words
						for (String w : wds) {
							int l = w.length();
							String wlc = w.toLowerCase();
							String wuc = w.substring(0, 1).toUpperCase() + (l > 1 ? w.substring(1) : "");
							// Searches for lower case and first letter is upper case version of the word; \\b makes sure that it
							// only finds the word itself and not also the word inside another word (for example, not getting banana
							// as an example of the word "an").
							Pattern pattern = Pattern.compile("\\b(" + wlc + "|" + wuc + ")\\b");
							Matcher matcher = pattern.matcher(thisLine);
							// Find all occurrences
							while (matcher.find()) {
								System.out.println("Start index: " + matcher.start());
								String ex = "";
								int index = matcher.start();
								int startIndex = index - before;
								int endIndex = index + l + after;
								// If user wants more characters before the word than are in thisLine, this checks if prevLine has a
								// length greater than 0, and if it does, this gets those characters from prevLine. If prevLine doesn't
								// have enough characters, it defaults to getting adding the entire prevLine string to the example.
								if (startIndex < 0) {
									if (prevLine.length() > 0) {
										int start = prevLine.length() - Math.abs(startIndex) - 1;
										ex += prevLine.substring(start < 0 ? 0 : start);
									}
									startIndex = 0;
								}
								if (endIndex + 1 > thisLine.length()) {
									int end = endIndex - thisLine.length() + 1;
									ex += thisLine.substring(startIndex) + nextLine.substring(0, end > nextLine.length() ? nextLine.length() - 1 : end);
								} else {
									ex += thisLine.substring(startIndex, endIndex);
								}
								int f = index - startIndex;
								System.out.println("(2) Start index: " + f);
								String highlighted = "<html>" + ex.substring(0, f) + "<font color=red>" + ex.substring(f, f + l) + "</font>" + ex.substring(f + l) + "</html>";
								if (map == null) {
									exs.add(highlighted);
								} else if (posTag == null) {
									map.get(w).add(highlighted);
								} else {
									map.get(posTag).add(highlighted);
								}
							}
						}
					}
					// sets prevLine equal to thisLine and thisLine equal to nextLine and nextLine will be assigned to
					// the next line in the text if there is one. if not, nextLine will be searched for instances
					// of the search word in the loop below.
					prevLine = thisLine;
					thisLine = nextLine;
				}
				for (String w : wds) {
					int l = w.length();
					String wlc = w.toLowerCase();
					String wuc = w.substring(0, 1).toUpperCase() + (l > 1 ? w.substring(1) : "");
					Pattern pattern = Pattern.compile("\\b(" + wlc + "|" + wuc + ")\\b");
					Matcher matcher = pattern.matcher(nextLine);
					// Find all occurrences
					while (matcher.find()) {
						String ex = "";
						int index = matcher.start();
						int startIndex = index - before;
						int endIndex = index + l + after;
						if (startIndex < 0) {
							if (thisLine.length() > 0) {
								int start = l - Math.abs(startIndex) - 1;
								ex += thisLine.substring(start < 0 ? 0 : start);
							}
							startIndex = 0;
						}
						if (endIndex + 1 > nextLine.length()) {
							ex += nextLine.substring(startIndex);
						} else {
							ex += thisLine.substring(startIndex, endIndex);
						}
						matcher = pattern.matcher(ex);
						while (matcher.find()) {
							int f = matcher.start();
							System.out.println("(2) Start index: " + f);
							String highlighted = "<html>" + ex.substring(0, f) + "<font color=red>" + ex.substring(f, f + l) + "</font>" + ex.substring(f + l) + "</html>";
							if (map == null) {
								exs.add(highlighted);
							} else if (posTag == null) {
								map.get(w).add(highlighted);
							} else {
								map.get(posTag).add(highlighted);
							}
						}
					}
				}
				in.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// updates the list of examples
		if (model != null) { //posTag == null
			model.clear();
			if (map != null) {
				for (String ex : safe(map.get(listOfWords.getSelectedValue()))) {
					model.addElement(ex);
				}
			} else {
				System.out.println("map is null");
				for (String ex : exs) {
					model.addElement(ex);
				}
			}
		}
	}
	
	/** Updates the panel of words on the left. Called after search words are added by the user or after word sorting is changed by user.
	 * Updates wordsToExamples and entriesToExamples so that they contain all the current words. */
	public void updateWordPanel(List<String> newWords, boolean updateExamples) {
		System.out.println("Called update word panel");
		int selectedIndex = listOfWords.getSelectedIndex();

		if (updateExamples) {
			for (String s : words) {
				if (!wordsToExamples.containsKey(s)) {
					wordsToExamples.put(s, new ArrayList<String>());
				}
				if (!entriesToExamples.containsKey(s)) {
					entriesToExamples.put(s,  new HashMap<String, List<String>>());
				}
			}
			mapToExamples(newWords, wordsToExamples, exampleListModel, null);
		}

		wordListModel.clear();
		if (frequency && resourceFNs.size()>0) {
			HashMap<String, Integer> m = new HashMap<>();
			for (String s : words) {
				m.put(s, new Integer(wordsToExamples.get(s).size()));
			}
			for (String s : sortByValue(m).keySet())
				wordListModel.addElement(s);
		} else if (alphabetical) {
			Collection<String> ws = new TreeSet<String>(Collator.getInstance());
			for (String s : words)
				ws.add(s);
			for (String s : ws) {
				wordListModel.addElement(s);
				System.out.println(s);
			}
		} else {
			for (String s : words)
				wordListModel.addElement(s);
		}
		if (selectedIndex == -1)
			listOfWords.setSelectedIndex(0);
		else
			listOfWords.setSelectedIndex(selectedIndex);
	}

	/** Sorts a HashMap by value and outputs it as a TreeMap because HashMaps don't have an order. */
	public TreeMap<String, Integer> sortByValue(HashMap<String, Integer> map) {
		ValueComparator vc = new ValueComparator(map);
		TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(vc);
		sortedMap.putAll(map);
		return sortedMap;
	}
	
	/** Returns entryViewInfo variable so that the class MousePopupListener can access it. */
	public ArrayList<String[]> getEVI() {
		return entryViewInfo;
	}
	
	public static void main(String[] args) {
		new DictionaryEditor();
	}
}
