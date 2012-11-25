package org.jboss.tools.vpe.vpv.transform;

import java.io.File;

import org.w3c.dom.Text;

public interface ResourceAcceptor {

	void acceptText(Text text, String mimeType);

	void acceptFile(File file, String mimeType);

}
