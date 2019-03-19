package sig.ccsd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

public class Analyzer {
	public static String BASEPATH;
	public static List<CCSD> ccsd_list = new ArrayList<CCSD>();
	public static Analyzer ANALYZER;
	public static boolean SILENT = false;
	
	static void validate(File f) {
		 DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		String datestamp = dtf.format(LocalDateTime.now());
		if (f==null) {
			Analyzer.writetoFile(new StringBuilder("["+datestamp+"] null folder detected. THIS SHOULD NOT BE HAPPENING."), new File("error.log"));
		} else
		if (f.exists()) {
			Analyzer.writetoFile(new StringBuilder("["+datestamp+"] Could not find folder "+f.getAbsolutePath()), new File("error.log"));
		} else
		if (!f.isDirectory()) {
			Analyzer.writetoFile(new StringBuilder("["+datestamp+"] Location "+f.getAbsolutePath()+" is not a directory."), new File("error.log"));
		}
	}
	
	public static void main(String[] args) {
		
		File user_folder_path = null;
		File results_folder_path = null;
		
		for (int i=0;i<args.length;i++) {
			String arg = args[i];
			switch (i) {
				case 0:{
					user_folder_path = new File(arg);
					validate(user_folder_path);
				}break;
				case 1:{
					results_folder_path = new File(arg);
					validate(results_folder_path);
				}break;
				default:{
					if (arg.equalsIgnoreCase("silent")) {
						SILENT=true;
						System.out.println("Running in silent mode...");
					}
				}
			}
		}
		
		final JFrame frame = new JFrame("CCSD Analyzer");
		final JFrame warning_frame = new JFrame("CCSD Analyzer - Warnings");
		ANALYZER = new Analyzer();
		if (user_folder_path!=null && user_folder_path.exists() && user_folder_path.isDirectory()) {
			BASEPATH = user_folder_path.getAbsolutePath();
		} else {
			JFileChooser j = new JFileChooser();
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			j.setDialogTitle("Select a folder containing all CCSD folders.");
			while (BASEPATH==null) {
				if (j.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					BASEPATH = j.getSelectedFile().getAbsolutePath();
				}
			}
		}
		List<File> directorylist = GetDirectories(BASEPATH);
		ANALYZER.AnalyzeDirectories(directorylist);
		//System.out.println(directorylist);
		
		String[] columnlist = new String[]{
			"CCSD","T&A","TSO","SAM","CLR","UNL","TSR","IER","EXR","DSR","RFU","MISC"	
		};
		Object[][] objects = new Object[ANALYZER.ccsd_list.size()][];
		int count=0;
		for (CCSD c : ANALYZER.ccsd_list) {
			System.out.println("S:"+c);
			objects[count++] = c.getTableData();
		}
		//System.out.println(Arrays.deepToString(objects));

		
		String[] warningcolumnlist = new String[]{
			"CCSD","Component","Location","Warning Description"	
		};
		Object[][] warningobjects = new Object[ANALYZER.GetWarningSize()][];
		count=0;
		for (CCSD c : ANALYZER.ccsd_list) {
			for (Warning w : c.warnings) {
				warningobjects[count++] = 
						new Object[]{
								c.id,
								"",
								w.f,
								w.type.desc
						};
				//System.out.print("count: "+count);
			}
			for (String s : c.map.keySet()) {
				for (CCSDComponent cc : c.map.get(s)) {
					for (Warning w : cc.warnings) {
						warningobjects[count++] = 
								new Object[]{
										c.id,
										cc.label,
										w.f,
										w.type.desc
								};
						//System.out.print("count: "+count);
					}
				}
			}
		}
		
		final JTable table = new JTable(objects,columnlist);
		JButton button = new JButton("Select All");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				table.selectAll();
			}
		});
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setColumnSelectionAllowed(true);
		table.setDefaultRenderer(Object.class, new TableRenderer());
		JScrollPane scroll = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(scroll,BorderLayout.CENTER);
		frame.getContentPane().add(button,BorderLayout.PAGE_END);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		if (!SILENT) {
			frame.setVisible(true);
		}
		
		JTable warning_table = new JTable(warningobjects,warningcolumnlist);
		JButton fixbutton = new JButton("Create Missing Folders");
		fixbutton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CreateMissingFolders();
			}

			private void CreateMissingFolders() {
				for (CCSD cc : Analyzer.ANALYZER.ccsd_list) {
					for (Warning w : cc.warnings) {
						if (w.type == WarningLabel.MISSING_FOLDER) {
							w.f.mkdirs();
						}
					}
				}
				frame.dispose();
				warning_frame.dispose();
				//Analyzer.BASEPATH = null;
				Analyzer.ANALYZER = null;
				Analyzer.ccsd_list.clear();
				Analyzer.main(new String[]{});
			}
		});
		warning_table.setAutoCreateRowSorter(true);
		warning_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		warning_table.setColumnSelectionAllowed(true);
		warning_table.setDefaultRenderer(Object.class, new TableRenderer());
		warning_table.getColumnModel().getColumn(0).setWidth(120);
		warning_table.getColumnModel().getColumn(1).setWidth(120);
		warning_table.getColumnModel().getColumn(2).setWidth(320);
		warning_table.getColumnModel().getColumn(3).setWidth(480);
		JScrollPane warning_scroll = new JScrollPane(warning_table);
		warning_table.setFillsViewportHeight(true);
		warning_frame.getContentPane().setLayout(new BorderLayout());
		warning_frame.getContentPane().add(warning_scroll,BorderLayout.CENTER);
		warning_frame.getContentPane().add(fixbutton,BorderLayout.PAGE_END);
		warning_frame.setSize(800, 600);
		if (!SILENT) {
			warning_frame.setVisible(true);
		}
		
		if (results_folder_path!=null && results_folder_path.exists() && results_folder_path.isDirectory()) {
			StringBuilder sb_results1 = new StringBuilder();
			StringBuilder sb_results2 = new StringBuilder();
			for (int i=0;i<columnlist.length;i++) {
				if (sb_results1.length()!=0) {
					sb_results1.append("\t");
				}
				sb_results1.append(columnlist[i]);
			}
			for (int i=0;i<objects.length;i++) {
				sb_results1.append("\n");
				Object[] data = objects[i];
				for (int j=0;j<data.length;j++) {
					if (j!=0) {
						sb_results1.append("\t");
					}
					sb_results1.append((String)data[j]);
				}
			}
			Analyzer.writetoFile(sb_results1,new File(results_folder_path,"ccsd_results.txt"));
			
			for (int i=0;i<warningcolumnlist.length;i++) {
				if (sb_results2.length()!=0) {
					sb_results2.append("\t");
				}
				sb_results2.append(warningcolumnlist[i]);
			}
			for (int i=0;i<warningobjects.length;i++) {
				sb_results2.append("\n");
				Object[] data = warningobjects[i];
				for (int j=0;j<data.length;j++) {
					if (j!=0) {
						sb_results2.append("\t");
					}
					if (data[j] instanceof File) {
						sb_results2.append(((File)(data[j])).getAbsolutePath());
					} else {
						sb_results2.append((String)data[j]);
					}
				}
			}
			Analyzer.writetoFile(sb_results2,new File(results_folder_path,"warning_results.txt"));
		}
	}
	
	public static void writeToFile(StringBuilder data, File filename, boolean append) {
		  File file = filename;
			try {

				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file,append);
				PrintWriter pw = new PrintWriter(fw);
				
				pw.print(data.toString());
				pw.flush();
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	  
  public static void writetoFile(StringBuilder data, File filename) {
	  Analyzer.writeToFile(data, filename, false);
  }

	private int GetWarningSize() {
		int warnings=0;
		for (CCSD c : ccsd_list) {
			warnings+=c.warnings.size();
			for (String s : c.map.keySet()) {
				for (CCSDComponent cc : c.map.get(s)) {
					warnings+=cc.warnings.size();
				}
			}
		}
		return warnings;
	}

	public static List<File> GetDirectories(String basepath) {
		File[] filelist = new File(basepath).listFiles();
		List<File> directorylist = new ArrayList<File>();
		if (filelist!=null) {
			for (File f : filelist) {
				if (f!=null && f.isDirectory() && !f.isFile()) {
					directorylist.add(f);				
				}
			}
		}
		return directorylist;
	}
	
	void AnalyzeDirectories(List<File> directories) {
		JFrame progressmeter = new JFrame("Progress Meter");
		JProgressBar progressbar = new JProgressBar(0,directories.size());
		if (!SILENT) {
			progressbar.setStringPainted(true);
			progressmeter.getContentPane().setLayout(new BorderLayout());
			progressmeter.getContentPane().add(progressbar, BorderLayout.CENTER);
			progressmeter.setSize(240,120);
			progressmeter.setVisible(true);
		}
		int count=0;
		for (File f : directories) {
			ccsd_list.add(new CCSD(f));
			if (!SILENT) {
				progressbar.setValue(++count);
			}
		} 
		for (int i=0;i<ccsd_list.size();i++) {
			CCSD c = ccsd_list.get(i);
			if (c.toBeRemoved) {
				ccsd_list.remove(i--);
			}
		}
		if (!SILENT) {
			progressmeter.dispose();
		}
	}
}
