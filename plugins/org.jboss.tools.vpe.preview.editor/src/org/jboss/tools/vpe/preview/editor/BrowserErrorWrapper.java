package org.jboss.tools.vpe.preview.editor;

import org.eclipse.swt.SWTError;
import org.jboss.tools.vpe.editor.mozilla.XulRunnerErrorWrapper;

public class BrowserErrorWrapper extends XulRunnerErrorWrapper {
	
	@Override
	protected Throwable wrapXulRunnerError(Throwable originalThrowable) {
		Throwable xulrunnerThrowable = super.wrapXulRunnerError(originalThrowable);
		if (xulrunnerThrowable instanceof SWTError && xulrunnerThrowable.getMessage() != null) {
			String message = xulrunnerThrowable.getMessage(); 
			if (message.contains("No more handles")) {//$NON-NLS-1$
				//running under GTK3 and no webkit installed
				//or Xulrunner disbaled by -Dorg.jboss.tools.vpe.loadxulrunner=false flag and no webkit installed
				xulrunnerThrowable = new NoEngineException("Webkit is not installed. Please install libwebkitgtk.",originalThrowable); //$NON-NLS-1$
			}
		}
		return xulrunnerThrowable;
	}
}
