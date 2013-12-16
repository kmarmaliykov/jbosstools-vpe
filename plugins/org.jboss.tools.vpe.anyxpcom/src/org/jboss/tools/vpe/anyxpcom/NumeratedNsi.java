package org.jboss.tools.vpe.anyxpcom;

import org.eclipse.swt.browser.Browser;
import org.jboss.tools.vpe.browsersim.browser.javafx.JavaFXBrowser;

public interface NumeratedNsi {
	int getNsiId();
	JavaFXBrowser getBrowser(); 
}
