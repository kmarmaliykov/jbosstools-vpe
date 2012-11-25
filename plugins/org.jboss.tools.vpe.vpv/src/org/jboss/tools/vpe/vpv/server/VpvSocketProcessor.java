package org.jboss.tools.vpe.vpv.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.vpe.vpv.Activator;
import org.jboss.tools.vpe.vpv.transform.VpvController;

public class VpvSocketProcessor implements Runnable {

	public static final String PROJECT_NAME = "projectName";
	public static final String VIEW_ID = "viewId";

	private Socket clientSocket;
	private VpvController vpvController;

	public VpvSocketProcessor(Socket clientSocket, VpvController vpvController) {
		this.clientSocket = clientSocket;
		this.vpvController = vpvController;
	}

	@Override
	public void run() {
		try {
			InputStream inputStream = clientSocket.getInputStream();
			OutputStream outputStream = clientSocket.getOutputStream();

			BufferedReader inputFromClient = new BufferedReader(
					new InputStreamReader(inputStream));
			DataOutputStream outputToClient = new DataOutputStream(outputStream);
			List<String> requestHeaders = getRequestHeader(inputFromClient);
			
			if (requestHeaders.isEmpty()) {
				return;
			}
			
			processRequest(requestHeaders);
		} catch (IOException e) {
			Activator.logError(e);
		}
		
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	private void processRequest(List<String> requestHeaders) {

		String initialRequestLine = requestHeaders.get(0);
		String httpRequestString = getHttpRequestString(initialRequestLine);

		Map<String, String> queryParametersMap = parseRequestParameters(httpRequestString);

		String path = getPath(httpRequestString);
		String projectName = getProjectName(queryParametersMap);
		int viewId = getViewId(queryParametersMap);

		// VpvController.getInstance.getResource(,,, new ResourceAcceptor() {
		//
		// @Override
		// public void acceptText(Text text, String mimeType) {
		//
		// output.write(createHeader(mimeType))
		// output
		//
		// }
		//
		// @Override
		// public void acceptFile(File file, String mimeType) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
	}

	private Map<String, String> parseRequestParameters(String httpRequestString) {
		int delimiterPosition = getDilimiterPosition(httpRequestString);

		if (delimiterPosition == -1) {
			return null;
		}

		String parameterString = httpRequestString.substring(
				delimiterPosition + 1, httpRequestString.length());

		String[] parameterArray = parameterString.split("&");
		Map<String, String> parameterMap = new HashMap<String, String>();
		for (String param : parameterArray) {
			if (param.length() > 0) {
				String[] nameValue = param.split("=");
				String name = nameValue[0];
				String value = nameValue.length > 1 ? nameValue[1] : null;
				parameterMap.put(name, value);
			}
		}
		return parameterMap;
	}

	int getDilimiterPosition(String httpRequestString) {
		return httpRequestString.indexOf('?');
	}

	private String getHttpRequestString(String initialRequestLine) {
		String[] data = initialRequestLine.split(" ");
		return data[1];
	}

	private String getPath(String httpRequestString) {
		String path = httpRequestString;
		int delimiter = getDilimiterPosition(httpRequestString);

		if (delimiter == -1) {
			return path.substring(1, path.length());
		}

		return path.substring(1, delimiter);

	}

	private String getProjectName(Map<String, String> queryParametersMap) {
		String projectName = queryParametersMap.get(PROJECT_NAME);
		return projectName;
	}

	private Integer getViewId(Map<String, String> queryParametersMap) {
		String viewId = queryParametersMap.get(VIEW_ID);
		if (viewId != null) {
			return Integer.parseInt(viewId);
		}

		return null;
	}

	private List<String> getRequestHeader(BufferedReader inputFromClient) {
		List<String> requestHeaders = new ArrayList<String>();

		try {
			while (inputFromClient.ready()) {
				requestHeaders.add(inputFromClient.readLine());
			}
		} catch (IOException e) {
			Activator.logError(e);
		}

		return requestHeaders;
	}
}
