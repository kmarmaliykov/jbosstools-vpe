package org.jboss.tools.vpe.vpv.exceptions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.vpe.vpv.views.Messages;

public class ExceptionNotifier {

	public static void showErrorMessage(Shell shell, String message) {
		MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
		messageBox.setText(Messages.VpvView_ERROR);
		messageBox.setMessage(message);
		messageBox.open();
	}

}
