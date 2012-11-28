package org.jboss.tools.vpe.vpv.listener;

import java.io.File;

import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.EditorReference;
import org.jboss.tools.vpe.vpv.Activator;

public class EditorListener implements IPartListener2 {
	
	private Browser browser;

	public EditorListener(Browser browser) {
		this.browser = browser;
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		Activator.logInfo(partRef + " is Activated");

	}
	

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		Activator.logInfo(partRef + " is Opened");
		if (partRef instanceof EditorReference){
			Activator.logInfo("instance of Editor reference");
		}
	}
	
	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		Activator.logInfo(partRef + " is Closed");

	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}


	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}


	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}
}
