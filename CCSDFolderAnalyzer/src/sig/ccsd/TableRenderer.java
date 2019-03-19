package sig.ccsd;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableRenderer extends DefaultTableCellRenderer{
	public Component getTableCellRendererComponent(JTable table, Object value, boolean   isSelected, boolean hasFocus, int row, int column) 
	{ 
	    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    Color ccc = Color.WHITE;
	    if(column!=0 && value instanceof String) {
	        String s = (String)value;
	        switch (s.trim()) {
		        case "DOES NOT EXIST":{
		        	ccc=Color.RED;
		        }break;
		        case "DUPLICATE":{
		        	ccc=Color.MAGENTA;
		        }break;
		        case "0":{
		        	ccc=Color.YELLOW;
		        }break;
		        default:{
		        	ccc=Color.GREEN;
		        }
	        }
	    }
	    if (table.getValueAt(row, 3) instanceof String) {
		    String s = (String)table.getValueAt(row, 3);
		    if (s.equalsIgnoreCase(WarningLabel.DUPLICATE_FOLDER.desc)) {
		    	ccc = Color.MAGENTA;
		    } else
		    if (s.equalsIgnoreCase(WarningLabel.EXTRA_FILES.desc)) {
		    	ccc = Color.CYAN;
		    } else
		    if (s.equalsIgnoreCase(WarningLabel.MISSING_FILES.desc)) {
		    	ccc = Color.YELLOW;
		    } else
		    if (s.equalsIgnoreCase(WarningLabel.TYPO.desc)) {
		    	ccc = Color.GREEN;
		    } else
		    if (s.equalsIgnoreCase(WarningLabel.MISSING_FOLDER.desc)) {
		    	ccc = Color.RED;
		    } else
		    if (s.equalsIgnoreCase(WarningLabel.WRONG_LOCATION.desc)) {
		    	ccc = new Color(120,120,255);
		    }
	    }
	    if (isSelected) {
	    	ccc = new Color((int)(ccc.getRed()/1.5f),(int)(ccc.getGreen()/1.5f),(int)(ccc.getBlue()/1.5f));
	    } else {
	    	ccc = new Color((int)Math.min(ccc.getRed()+64,255),(int)Math.min((ccc.getGreen()+64),255),(int)Math.min((ccc.getBlue()+64),255));
	    }
	    c.setBackground(ccc);
	    return c; 
	} 
}
