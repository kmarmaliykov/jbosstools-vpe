package org.jboss.tools.vpe.vpv.views;

public enum SuitableFileExtensions {
	HTML("html"), 
	HTM("htm"), 
	XHTML("xhtml"), 
	JSP("jsp");

	private final String value;

	private SuitableFileExtensions(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static boolean contains(String fileExtension) {

		for (SuitableFileExtensions extension : SuitableFileExtensions.values()) {
			if (extension.value.equals(fileExtension)) {
				return true;
			}
		}

		return false;
	}
}