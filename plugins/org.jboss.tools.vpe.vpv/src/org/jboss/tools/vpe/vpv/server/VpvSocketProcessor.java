package org.jboss.tools.vpe.vpv.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.vpe.vpv.Activator;
import org.jboss.tools.vpe.vpv.transform.ResourceAcceptor;
import org.jboss.tools.vpe.vpv.transform.VpvController;

public class VpvSocketProcessor implements Runnable {

    public static final String PROJECT_NAME = "projectName";
    public static final String VIEW_ID = "viewId";
    public static final String INITIAL_REQUEST_LINE = "Initial request line";
    public static final String REFERER = "Referer";
    public static final String HTTP = "http://";
    public static final String HOST = "Host";

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

			BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(inputStream));
			DataOutputStream outputToClient = new DataOutputStream(outputStream);
			Map<String, String> requestHeader = getRequestHeader(inputFromClient);
			
			if (requestHeader.isEmpty()) {
			    processNotFound(outputToClient);	    
			    return;
			}
			
			processRequest(requestHeader, outputToClient);
		} catch (IOException e) {
			Activator.logError(e);
		}
		
	}

	private void processRequest(Map<String, String> requestHeaders, final DataOutputStream outputToClient) {
		String initialRequestLine = requestHeaders.get(INITIAL_REQUEST_LINE);
		String httpRequestString = getHttpRequestString(initialRequestLine);
		Map<String, String> queryParametersMap = parseRequestParameters(httpRequestString);
		
		if (!queryParametersMap.containsKey(PROJECT_NAME)){
		    processRequestHeaders(requestHeaders, outputToClient, httpRequestString);
		    return;
		}
		
		String path = getPath(httpRequestString);
		String projectName = getProjectName(queryParametersMap);
		int viewId = getViewId(queryParametersMap);

        VpvController.getResource(projectName, path, viewId, new ResourceAcceptor() {

            @Override
            public void acceptText(String text, String mimeType) {
                String responceHeader = getOkResponceHeader(mimeType);
                try {
                    outputToClient.writeBytes(responceHeader);
                    outputToClient.writeBytes(text);
                } catch (IOException e) {
                    Activator.logError(e);
                } finally {
                    try {
                        outputToClient.close();
                    } catch (IOException e) {
                        Activator.logError(e);
                    }
                }
            }

            @Override
            public void acceptFile(File file, String mimeType) {
                String responceHeader = getOkResponceHeader(mimeType);
                try {
                    outputToClient.writeBytes(responceHeader);
                    sendFile(file, outputToClient);
                } catch (IOException e) {
                    Activator.logError(e);
                } finally {
                    try {
                        outputToClient.close();
                    } catch (IOException e) {
                       Activator.logError(e);
                    }
                }
            }
        });
    }

	private void processRequestHeaders(Map<String, String> requestHeaders, DataOutputStream outputToClient,
			String httpRequestString) {
		String referer = requestHeaders.get(REFERER);

		if (referer == null) {
			processNotFound(outputToClient);
			return;
		}

		String host = requestHeaders.get(HOST);
		String refererParameters = getRefererParameters(referer);

		if (refererParameters == null) {
			processNotFound(outputToClient);
			return;
		}

		String httpRequestStingWithoutParameters = getHttpRequestStringWithoutParameters(httpRequestString);
		String redirectURL = HTTP + host + httpRequestStingWithoutParameters + refererParameters;
		String redirectHeader = getRedirectHeader(redirectURL);

		processRedirectRequest(redirectHeader, outputToClient);
	}

	private void processRedirectRequest(String redirectHeader, DataOutputStream outputToClient) {
		try {
			outputToClient.writeBytes(redirectHeader);
		} catch (IOException e) {
			Activator.logError(e);
		} finally {
			try {
				outputToClient.close();
			} catch (IOException e) {
				Activator.logError(e);
			}
		}
	}

	private void processNotFound(DataOutputStream outputToClient) {
		String notFoundHeader = getNotFoundHeader();
		try {
			outputToClient.writeBytes(notFoundHeader);
		} catch (IOException e) {
			Activator.logError(e);
		} finally {
			try {
				outputToClient.close();
			} catch (IOException e) {
				Activator.logError(e);
			}
		}
	}

	private String getRefererParameters(String referer) {
		String refererParameters = referer;
		int delimiter = getDilimiterPosition(referer);
		if (delimiter == -1) {
			return null;
		}

		return refererParameters.substring(delimiter, referer.length());
	}

	private String getHttpRequestStringWithoutParameters(String httpRequestString) {
		String httpRequestStringWitoutParameters = httpRequestString;
		int delimiter = getDilimiterPosition(httpRequestString);

		if (delimiter == -1) {
			return httpRequestStringWitoutParameters;
		}

		return httpRequestStringWitoutParameters.substring(delimiter, httpRequestStringWitoutParameters.length());
	}

	private Map<String, String> parseRequestParameters(String httpRequestString) {
		int delimiterPosition = getDilimiterPosition(httpRequestString);

		if (delimiterPosition == -1) {
			return Collections.emptyMap();
		}

		String parameterString = httpRequestString.substring(delimiterPosition + 1, httpRequestString.length());

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

		return -1;
	}

	private Map<String, String> getRequestHeader(BufferedReader inputFromClient) {
		Map<String, String> requestHeaders = new HashMap<String, String>();
		try {
			String line;
			while ((line = inputFromClient.readLine()) != null && !line.isEmpty()) {
				if (!line.contains(": ")) {
					requestHeaders.put(INITIAL_REQUEST_LINE, line);
				} else {
					String[] nameValue = line.split(": ");
					String key = nameValue[0];
					String value = nameValue[1];
					requestHeaders.put(key, value);
				}
			}
		} catch (IOException e) {
			Activator.logError(e);
		}

		return requestHeaders;
	}

    private void sendFile(File file, OutputStream outputToClient) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) >= 0) {
                outputToClient.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            Activator.logError(e);
        } catch (IOException e) {
            Activator.logError(e);
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                Activator.logError(e);
            }
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
	
	private String getOkResponceHeader(String mimeType) {
		String responceHeader = "HTTP/1.1 200 OK\r\n" +
				"Server: VPV server" +"\r\n"+
				"Content-Type: " + mimeType + "\r\n" +
				"Cache-Control: no-cache\r\n" +
				"Connection: close\r\n\r\n";
		return responceHeader;
	}
	
	private String getNotFoundHeader(){
        String responceHeader = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Connection: close\r\n\r\n" +
                "<!DOCTYPE HTML>" +
                "<h1>404 Not Found<//h1>";
        return responceHeader;
	}
	
	private String getRedirectHeader(String location){
	    String responceHeader = "HTTP/1.1 301 Moved Permanently\r\n" +
                "Location: " + location +  "\r\n\r\n";
	    return responceHeader;
	}
}
