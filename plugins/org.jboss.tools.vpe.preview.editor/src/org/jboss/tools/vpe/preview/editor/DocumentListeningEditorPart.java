/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.UIJob;

/**Editor part which listens to document change and performs action after change happens
 * 
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */
public abstract class DocumentListeningEditorPart extends EditorPart{
	private Job currentJob;
	private IDocumentListener documentListener;
	
	/**
	 * Action that should happen after document change
	 */
	protected abstract void performAction();
	
	/**
	 * @return <code>true</code> if action should happen after document change
	 */
	protected abstract boolean actionHappening();
	
	private void updatePreview() {
		if (currentJob == null || currentJob.getState() != Job.WAITING) {
			if (currentJob != null && currentJob.getState() == Job.SLEEPING) {
				currentJob.cancel();
			}
			currentJob = createPreviewUpdateJob();
		}

		currentJob.schedule(500);
	}
	
	private Job createPreviewUpdateJob() {
		Job job = new UIJob("Preview Update") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				performAction();
				return Status.OK_STATUS;
			}
		};
		return job;
	}
	
	protected void removeDocumentListener(IEditorPart sourceEditor) {
		if (sourceEditor != null) {
			IDocument document = getDocument(sourceEditor);
			if (document != null) {
				document.removeDocumentListener(getDocumentListener());
			}
		}
	}
	
	protected void addDocumentListener(IEditorPart sourceEditor) {
		if (sourceEditor != null) {
			IDocument document = getDocument(sourceEditor);
			if (document != null) {
				document.addDocumentListener(getDocumentListener());
			}
		}
	}
	
	private IDocument getDocument(IEditorPart sourceEditor) {
		return (IDocument) sourceEditor.getAdapter(IDocument.class);
	}
	
	private IDocumentListener getDocumentListener() {
		if (documentListener == null) {
			documentListener = new IDocumentListener() {

				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
				}

				@Override
				public void documentChanged(DocumentEvent event) {
					if (actionHappening()) {
						updatePreview();
					}
				}

			};
		}
		return documentListener;
	} 
}
