package org.jboss.tools.vpe.vpv.transform;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

public class CustomFunction extends BrowserFunction {

	public CustomFunction(Browser browser, String name) {
		super(browser, name);
	}

	@Override
	public Object function(Object[] arguments) {
		for(Object arg : arguments) {
			System.out.println(arg);
		}
		return super.function(arguments);
	}
}
