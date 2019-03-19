package sig.ccsd;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CCSD{
	String id;
	File file;
	HashMap<String,List<CCSDComponent>> map = new HashMap<String,List<CCSDComponent>>();
	List<Warning> warnings = new ArrayList<Warning>();
	public static String[] map_req_list = new String[]{
			"T&A","TSO","IER","EXR","DSR","SAM","MISC","CLR","UNL","TSR","RFU","REPORTS"
	};
	boolean toBeRemoved=false;
	
	public CCSD(File f) {
		if (f.getName().equalsIgnoreCase("FIBER LINK")) {
			List<File> directories = Analyzer.GetDirectories(f.getAbsolutePath());
			for (File ff : directories) {
				Analyzer.ccsd_list.add(new CCSD(ff));
			}
			toBeRemoved=true;
		} else {
			id = RemoveErroneousData(f.getName());
			this.file = f;
			PopulateHashMap();
			IterateThroughDirectories();
			System.out.println(this);
		}
	}
	private void IterateThroughDirectories() {
		List<File> directorylist = Analyzer.GetDirectories(file.getAbsolutePath());
		for (File f : directorylist) {
			String actualname = f.getName();
			String[] wrong_location_list = new String[]{"IER","EXR","DSR","RFU"};
			if (!CheckIfExistsInMapAndAddToComponentList(f, actualname, wrong_location_list)) {
				String fixedname = RemoveErroneousData(actualname);
				if (!CheckIfExistsInMapAndAddToComponentList(f, fixedname, wrong_location_list)) {
					warnings.add(new Warning(f,WarningLabel.EXTRA_FILES));
				} else {
					warnings.add(new Warning(f,WarningLabel.TYPO));
				}
			}
		}
		for (String s : map.keySet()) {
			if (map.get(s).size()==0) {
				String[] wrong_location_list = new String[]{"IER","EXR","DSR","RFU"};
				boolean belongsInReport = false;
				for (String ss : wrong_location_list) {
					if (ss.equalsIgnoreCase(s)) {
						belongsInReport=true;
						break;
					}
				}
				File newfile;
				if (belongsInReport) {
					newfile = new File(new File(file.getAbsolutePath(),"REPORTS"),s);
				} else {
					newfile = new File(file.getAbsolutePath(),s);
				}
				warnings.add(new Warning(newfile,WarningLabel.MISSING_FOLDER));
			}
		}
	}
	private boolean CheckIfExistsInMapAndAddToComponentList(File f, String actualname, String[] wrong_location_list) {
		if (map.containsKey(actualname)) {
			List<CCSDComponent> tempmap = map.get(actualname);
			CCSDComponent cc = new CCSDComponent(f,this);
			tempmap.add(cc);
			map.put(actualname, tempmap);
			for (String s : wrong_location_list) {
				if (actualname.equalsIgnoreCase(s)) {
					cc.warnings.add(new Warning(f,WarningLabel.WRONG_LOCATION));
				}
			}
			return true;
		}
		return false;
	}
	private void PopulateHashMap() {
		for (String s : map_req_list) {
			map.put(s, new ArrayList<CCSDComponent>());
		}
	}
	public static String RemoveErroneousData(String name) {
		for (int i=0;i<name.length();i++) {
			if (!ComposedOfAlphanumbericCharacters(name, i)) {
				name = name.substring(0,i);
			}
		}
		//System.out.println("Created CCSD with id "+name+".");
		return name;
	}
	public static boolean ComposedOfAlphanumbericCharacters(String name, int i) {
		return (name.charAt(i)>='0' && name.charAt(i)<='9') || 
			(name.charAt(i)>='A' && name.charAt(i)<='Z') || 
			(name.charAt(i)>='a' && name.charAt(i)<='z') || name.charAt(i)=='&';
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("CCSD{\n");
		sb.append("  "+id+" ("+warnings.size()+" warnings):\n");
		for (String s : map.keySet()) {
			int count=0;
			for (CCSDComponent cc : map.get(s)) {
				sb.append("    "+cc.label+"["+(count++)+"]: "+cc.filelist.size()+" files, "+cc.warnings.size()+" warnings\n");
			}
		}
		sb.append("}");
		return sb.toString();
	}
	public Object[] getTableData() {
		return new Object[]{id,
			GetOutput("T&A"),
			GetOutput("TSO"),
			GetOutput("SAM"),
			GetOutput("CLR"),
			GetOutput("UNL"),
			GetOutput("TSR"),
			GetOutput("IER"),
			GetOutput("EXR"),
			GetOutput("DSR"),
			GetOutput("RFU"),
			GetOutput("MISC"),	
		};
	}
	private Object GetOutput(String string) {
		if (map.get(string).size()==0) {
			return "DOES NOT EXIST";
		} else 
		if (map.get(string).size()>1){
			return "DUPLICATE";
		} else {
			return map.get(string).get(0).filelist.size()+"";
		}
	}
}
