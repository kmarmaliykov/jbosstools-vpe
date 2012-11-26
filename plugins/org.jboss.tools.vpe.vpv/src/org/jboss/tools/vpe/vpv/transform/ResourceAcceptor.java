package org.jboss.tools.vpe.vpv.transform;

import java.io.File;

public interface ResourceAcceptor {

	void acceptText(String text, String mimeType);

	void acceptFile(File file, String mimeType);

}
