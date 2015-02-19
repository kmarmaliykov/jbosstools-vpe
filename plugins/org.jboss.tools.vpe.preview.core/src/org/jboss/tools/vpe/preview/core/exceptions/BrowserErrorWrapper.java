package org.jboss.tools.vpe.preview.core.exceptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.part.StatusPart;
import org.jboss.tools.vpe.preview.core.Activator;

public class BrowserErrorWrapper {
	public void showError(Composite parent,	Throwable originalThrowable) {
		Throwable throwable = wrapXulRunnerError(originalThrowable);
		String errorMessage = MessageFormat.format(Messages.MOZILLA_LOADING_ERROR, throwable.getMessage());
		Activator.logError(throwable, errorMessage);

		parent.setLayout(new GridLayout());
		Composite statusComposite = new Composite(parent, SWT.NONE);
		Color bgColor= parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		Color fgColor= parent.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		parent.setBackground(bgColor);
		parent.setForeground(fgColor);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 0;
		gridData.heightHint = 0;
		statusComposite.setLayoutData(gridData);

		IStatus displayStatus = new Status(IStatus.ERROR,
				Activator.PLUGIN_ID, errorMessage, throwable);
		new StatusPart(statusComposite, displayStatus);

		final Link link = new Link(parent, SWT.WRAP);
		link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		link.setBackground(bgColor);
		link.setText(Messages.MOZILLA_LOADING_ERROR_LINK_TEXT);
		link.setToolTipText(Messages.MOZILLA_LOADING_ERROR_LINK);
		link.setForeground(link.getDisplay().getSystemColor(SWT.COLOR_BLUE));

		link.addMouseListener(new MouseListener() {
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				BusyIndicator.showWhile(link.getDisplay(), new Runnable() {
					public void run() {
						URL theURL = null;
						;
						try {
							theURL = new URL(Messages.MOZILLA_LOADING_ERROR_LINK);
						} catch (MalformedURLException e) {
							Activator.logError(e);
						}
						IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
						try {
							support.getExternalBrowser().openURL(theURL);
						} catch (PartInitException e) {
							Activator.logError(e);
						}
					}
				});
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseUp(MouseEvent e) {
			}
		});
		parent.getParent().layout(true, true);
	}
	
	protected Throwable wrapXulRunnerError(Throwable originalThrowable) {
		if (originalThrowable instanceof SWTError && originalThrowable.getMessage() != null) {
			String message = originalThrowable.getMessage(); 
			if (message.contains("No more handles")) {//$NON-NLS-1$
				/*running under GTK3 and no webkit installed
				or Xulrunner disbaled by -Dorg.jboss.tools.vpe.loadxulrunner=false flag and no webkit installed
				this error can be only under Linux. On Windows and OSX default browser always can be created.*/
				return new NoEngineException(Messages.NO_ENGINE_ERROR, originalThrowable);
			}
		}
		return originalThrowable;
	}
}
