/*******************************************************************************
 * Copyright (c) 2007-2009 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.vpe.resref.core;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.jboss.tools.common.model.ui.ModelUIImages;
import org.jboss.tools.common.resref.core.ResourceReference;

public class VpvResourcesDialog extends TitleAreaDialog {

	Object fileLocation = null;
	CssReferencesComposite css = null;
    AbsoluteFolderReferenceComposite absFolder = null;
    RelativeFolderReferenceComposite relFolder = null;
    
    private final int DIALOG_WIDTH = 400;
	private final int DIALOG_HEIGHT = 300;
    

	public VpvResourcesDialog(Shell parentShell, Object fileLocation,
			ResourceReference defaultAbsReference, ResourceReference defaultRelReference) {
		super(parentShell);
		setHelpAvailable(false);
		
		this.fileLocation = fileLocation;
		css = new CssReferencesComposite();
		absFolder = new AbsoluteFolderReferenceComposite();
		relFolder = new RelativeFolderReferenceComposite();
		css.setObject(fileLocation);
		absFolder.setObject(fileLocation, defaultAbsReference);
		relFolder.setObject(fileLocation, defaultRelReference);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.VRD_DEFAULT_WINDOW_TITLE);
		setTitle(Messages.VRD_DEFAULT_TITLE);
		setTitleImage(ModelUIImages.getImage(ModelUIImages.WIZARD_DEFAULT));  // image is managed by registry
		setMessage(Messages.VRD_PAGE_DESIGN_OPTIONS_ABOUT);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		
		Label dialogAreaSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		dialogAreaSeparator.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		Control pageArea = createTabFolder(composite);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		pageArea.setLayoutData(gd);
		
		dialogAreaSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		dialogAreaSeparator.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = DIALOG_WIDTH;
		gd.heightHint = DIALOG_HEIGHT;
		composite.setLayoutData(gd);
		
		return composite;
	}
	
	public Control createTabFolder(Composite parent) {
		final TabFolder tabFolder = new TabFolder(parent, SWT.FILL);
		
		TabItem foldersTab = new TabItem(tabFolder, SWT.NONE);
		TabItem cssTab = new TabItem(tabFolder, SWT.NONE);
		
		Composite foldersControl = new Composite(tabFolder, SWT.NONE);
		foldersControl.setLayout(new GridLayout(1, false));
		absFolder.createControl(foldersControl);
		relFolder.createControl(foldersControl);
		
		Control cssControl = css.createControl(tabFolder);

		foldersTab.setText(Messages.VRD_ACTUAL_RUN_TIME_FOLDERS);
		foldersTab.setToolTipText(Messages.VRD_ACTUAL_RUN_TIME_FOLDERS);
		foldersTab.setControl(foldersControl);

		cssTab.setText(Messages.VRD_INCLUDED_CSS_FILES);
		cssTab.setToolTipText(Messages.VRD_INCLUDED_CSS_FILES);
		cssTab.setControl(cssControl);
		
		tabFolder.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				String selectedTabText = tabFolder.getSelection()[0].getText();
				if(Messages.VRD_ACTUAL_RUN_TIME_FOLDERS.equals(selectedTabText)) {
					 setMessage(Messages.VRD_ACTUAL_RUN_TIME_FOLDERS_ABOUT);
				}else if(Messages.VRD_INCLUDED_CSS_FILES.equals(selectedTabText)){
					setMessage(Messages.VRD_INCLUDED_CSS_FILES_ABOUT);
				}
			}});
		return tabFolder;
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		/*
		 * When dialog OK is pressed - store all resource references
		 * from all tabs as preferences to the selected file.
		 */
		css.commit();
		absFolder.commit();
	    relFolder.commit();
	}

}
