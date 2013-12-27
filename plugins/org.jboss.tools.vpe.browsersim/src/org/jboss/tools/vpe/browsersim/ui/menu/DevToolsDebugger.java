package org.jboss.tools.vpe.browsersim.ui.menu;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class DevToolsDebugger extends Application implements Runnable {
		
	@Override
	public void run() {
		start(new Stage());	
	}	

	@Override
	public void start(Stage stage) {
		final WebView webView = new WebView();
		final WebEngine engine = webView.getEngine();
		engine.load("http://localhost:8087/inspector.html?host=localhost:8087&page=dtdb");
	    stage.setScene(new Scene(webView));
	    stage.show();
	}
	
}
