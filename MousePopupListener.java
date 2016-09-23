package dictionary_editor_stuff;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JPopupMenu;

public class MousePopupListener extends MouseAdapter {
	JList<String> list;
	JPopupMenu[] popup;
	DictionaryEditor frame;
	
	public MousePopupListener(JList<String> l, JPopupMenu[] p, DictionaryEditor f) {
		list = l;
		popup = p;
		frame = f;
	}	
	
    public void mousePressed(MouseEvent e) {
      checkPopup(e);
    }
    public void mouseClicked(MouseEvent e) {
      checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      checkPopup(e);
    }

    private void checkPopup(MouseEvent e) {
    	// An item is selected and the mouse event is a pop up trigger.
    	if (e.isPopupTrigger() && list.getSelectedIndex()>-1) {
    		// If the length of the pop up menu array is 1, the pop up menu is being drawn in added definitions or added examples.
    		// Since using || will not try to evaluate second part of expression.
    		// If length of pop up menu array is not 1 but the item in the ArrayList at list.getSelectedIndex() is an array of length 1
    		// (means user has selected a word and not an entry) draw the word pop up menu
	    	if(popup.length==1||frame.getEVI().get(list.getSelectedIndex()).length==1) {
	    		popup[0].show(list, e.getX(), e.getY());
	    	// otherwise (the user has selected an entry) draw the entry pop up menu.
	    	} else {
	    		popup[1].show(list, e.getX(), e.getY());
	    	}
    	}
    }
}