package sig.ccsd;

import java.io.File;

public class Warning {
	WarningLabel type;
	File f;
	
	public Warning(File f, WarningLabel type) {
		this.f=f;
		this.type=type;
	}
}
