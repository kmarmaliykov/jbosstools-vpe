package org.jboss.tools.vpe.vpv.views;

public interface IVisualController {
	public void refreshCommands();
	public void visualRefresh();
	public void sourceSelectionChanged();
	public boolean isVisualEditorVisible();
	public void refreshTemplates();
}
