package sig.ccsd;

public enum WarningLabel {
	WRONG_LOCATION("The folder is located in the wrong place."),
	TYPO("The folder is not named properly."),
	MISSING_FILES("There are no files located in this folder."),
	EXTRA_FILES("There are extra folders that don't belong in this folder."),
	DUPLICATE_FOLDER("There is already a copy of this folder elsewhere for this CCSD."),
	MISSING_FOLDER("Folder does not exist in this location.");
	
	String desc;
	
	WarningLabel(String desc) {
		this.desc=desc;
	}
	
	public String getDescription() {
		return desc;
	}
}
