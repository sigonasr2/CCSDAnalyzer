package sig.ccsd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CCSDComponent {
	File f;
	String label;
	CCSD ccsd_parent;
	List<Warning> warnings = new ArrayList<Warning>();
	List<File> filelist = new ArrayList<File>();
	
	public CCSDComponent(File f, CCSD parent) {
		this.ccsd_parent = parent;
		this.f=f;
		this.label = CCSD.RemoveErroneousData(f.getName());
		File[] files = f.listFiles();
		for (File f1 : files) {
			if (f1.isFile() || f1.isDirectory()) {
				filelist.add(f1);
				if (f1.isDirectory()) {
					if (ccsd_parent.map.containsKey(f1.getName())) {
						List<CCSDComponent> list = ccsd_parent.map.get(f1.getName());
						CCSDComponent cc = new CCSDComponent(f1,ccsd_parent);
						list.add(cc);
						if (list.size()>1) {
							warnings.add(new Warning(f,WarningLabel.DUPLICATE_FOLDER));
						}
						ccsd_parent.map.put(f1.getName(), list);
						boolean found=false;
						if (label.equalsIgnoreCase("REPORTS")) {
							String[] correct_location_list = new String[]{"IER","EXR","DSR","RFU"};
							for (String s : correct_location_list) {
								if (s.equalsIgnoreCase(f1.getName())) {
									found=true;
								}
							}
						}
						if (!found) {
							cc.warnings.add(new Warning(f1,WarningLabel.WRONG_LOCATION));
						}
					}
				}
			}
		}
		if (filelist.size()==0) {
			warnings.add(new Warning(f,WarningLabel.MISSING_FILES));
		}
	}
}
