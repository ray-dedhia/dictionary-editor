package dictionary_editor_stuff;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

class MyCellRenderer extends DefaultListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 47512407488116983L;
	public static final String HTML_1 = "<html><body style='width: ";
	public static final String HTML_2 = "px'>";
	public static final String HTML_3 = "</html>";
	private int width;

	public MyCellRenderer(int width) {
		this.width = width;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		String text = HTML_1 + String.valueOf(width) + HTML_2 + value.toString() + HTML_3;
		return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
	}
}