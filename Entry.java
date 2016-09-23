package dictionary_editor_stuff;

import java.util.ArrayList;
import java.util.List;

public class Entry {
	/**The string <b>pos</b> holds the part of speech tag for the entry. Each entry can only have one POS tag. If a word
	 * has more than one part of speech, multiple entries can be added the the list of entries that the word is mapped to
	 * in DictionaryEditor, each with a different POS.*/
	private String pos;
	/**The string <b>def</b> holds the definition for the entry. Each entry can have more than one definition.*/
	private List<String> def;
	/**The variable <b>notes</b> is a string that holds any user-inputed notes for the entry*/
	private String notes;
	/**The variable <b>examples</b> is a list of portions of text in resources containing the word that the user has chosen to appear in the entry.*/
	private List<String> examples;
	/**The variable <b>forms</b> is a list of the different forms of the word for the entry's part of speech tag.*/
	private List<String> forms;
	
	public Entry (String p, List<String> d, String n, List<String> e, List<String> f) {
		setPOS(p == null ? "" : p);
		setDef(d == null ? new ArrayList<String>() : d);
		setNotes(n == null ? "" : n);
		setExamples(e == null ? new ArrayList<String>() : e);
		setForms(f == null ? new ArrayList<String>() : f);
	}
	
	public Entry () {
		setPOS("");
		setDef(new ArrayList<String>());
		setNotes("");
		setExamples(new ArrayList<String>());
		setForms(new ArrayList<String>());
	}

	public String getPOS() {
		return pos;
	}

	public void setPOS(String pos) {
		this.pos = pos;
	}

	public List<String> getDef() {
		return def;
	}

	public void setDef(List<String> dfs) {
		def = dfs;
	}
	
	public void addDef(String d) {
		def.add(d);
	}
	
	public void addDef(List<String> ds) {
		def.addAll(ds);
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public List<String> getExamples() {
		return examples;
	}

	public void setExamples(List<String> exs) {
		examples = exs;
	}
	
	public void addExample(String ex) {
		examples.add(ex);
	}
	
	public void addExamples(List<String> exs) {
		examples.addAll(exs);
	}

	public List<String> getForms() {
		return forms;
	}

	public void setForms(List<String> fs) {
		forms = fs;
	}
	
	public void addForm(String form) {
		forms.add(form);
	}
	
}