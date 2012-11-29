package org.jboss.tools.vpe.vpv.views;

/**
 * @author Yahor Radtsevich (yradtsevich)
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.jboss.tools.vpe.vpv.Activator;
import org.jboss.tools.vpe.vpv.listener.EditorListener;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModel;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModelHolder;

public class VpvView extends ViewPart implements VpvVisualModelHolder {

	public static final String ID = "org.jboss.tools.vpe.vpv.views.VpvView";

	private Browser browser;
	
	private VpvVisualModel visualModel;
	private int modelHolderId;

	private EditorListener editorListener;

	public VpvView() {
		setModelHolderId(Activator.getDefault().getVisualModelHolderRegistry().registerHolder(this));
	}
	
	@Override
	public void dispose() {
		Activator.getDefault().getVisualModelHolderRegistry().unregisterHolder(this);
		getSite().getPage().removePartListener(editorListener);
	}
	
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		browser = new Browser(parent, SWT.WEBKIT);
		browser.setUrl("http://ww.google.com");
		
		inizializeEditorListener(browser, modelHolderId);
	}

	private void inizializeEditorListener(Browser browser, int modelHolderId ) {
		editorListener = new EditorListener(browser, modelHolderId);
		getSite().getPage().addPartListener(editorListener);
		editorListener.showBootstrapPart();
	}

	public void setFocus() {
		browser.setFocus();
	}

	@Override
	public void setVisualModel(VpvVisualModel visualModel) {
		this.visualModel = visualModel;
	}

	public void setModelHolderId(int modelHolderId) {
		this.modelHolderId = modelHolderId;
	}
}

