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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.jboss.tools.vpe.vpv.Activator;
import org.jboss.tools.vpe.vpv.transform.ResourceAcceptor;
import org.jboss.tools.vpe.vpv.transform.VpvController;

public class VpvSocketProcessor implements Runnable {

    public static final String INITIAL_REQUEST_LINE = "Initial request line";
    public static final String REFERER = "Referer";
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
			String initialContextLine = getItialRequestLine(inputFromClient);
			if (initialContextLine != null) {
				Map<String, String> requestHeader = getRequestHeader(inputFromClient);				
			
				if (requestHeader.isEmpty()) {
				    processNotFound(outputToClient);	    
				    return;
				}
				
				processRequest(initialContextLine, requestHeader, outputToClient);
			}
		} catch (IOException e) {
			Activator.logError(e);
		}
	}

	private void processRequest(String initialRequestLine, Map<String, String> requestHeaders, final DataOutputStream outputToClient) {
		String httpRequestString = getHttpRequestString(initialRequestLine);
		Map<String, String> queryParametersMap = parseUrlParameters(httpRequestString);
		
//		if (!queryParametersMap.containsKey(HttpConstants.PROJECT_NAME)){
//		    processRequestHeaders(requestHeaders, outputToClient, httpRequestString);
//		    return;
//		}
		
		String path = getPath(httpRequestString);
		String projectName = getProjectName(queryParametersMap, requestHeaders);
		String fullPath = projectName + path;
		Integer viewId = getViewId(queryParametersMap);
		vpvController.getResource(fullPath, viewId, new ResourceAcceptor() {

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

			@Override
			public void acceptError() {
				 processNotFound(outputToClient);	   
			}
        });
    }

//	private void processRequestHeaders(Map<String, String> requestHeaders, DataOutputStream outputToClient,
//			String httpRequestString) {
//		String referer = requestHeaders.get(REFERER);
//
//		if (referer == null) {
//			processNotFound(outputToClient);
//			return;
//		}
//
//		String host = requestHeaders.get(HOST);
//		String refererParameters = getRefererParameters(referer);
//
//		if (refererParameters == null) {
//			processNotFound(outputToClient);
//			return;
//		}
//
//		String httpRequestStingWithoutParameters = getHttpRequestStringWithoutParameters(httpRequestString);
//		String redirectURL = HttpConstants.HTTP + host + httpRequestStingWithoutParameters + refererParameters;
//		String redirectHeader = getRedirectHeader(redirectURL);
//
//		processRedirectRequest(redirectHeader, outputToClient);
//	}

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

	private Map<String, String> parseUrlParameters(String urlString) {
		int delimiterPosition = getDilimiterPosition(urlString);

		if (delimiterPosition == -1) {
			return Collections.emptyMap();
		}

		String parameterString = urlString.substring(delimiterPosition + 1, urlString.length());

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
		int pathEnd = delimiter != -1 ? delimiter : path.length();
		return path.substring(0, pathEnd);
	}

	private String getProjectName(Map<String, String> queryParametersMap, Map<String, String> requestHeaders) {
		String projectName = queryParametersMap.get(HttpConstants.PROJECT_NAME);
		if (projectName == null) {
			String referer = requestHeaders.get(REFERER);
			if (referer != null) {
				projectName = parseUrlParameters(referer).get(HttpConstants.PROJECT_NAME);
			}
		}
		return projectName;
	}

	private Integer getViewId(Map<String, String> queryParametersMap) {
		String viewId = queryParametersMap.get(HttpConstants.VIEW_ID);
		if (viewId != null) {
			return Integer.parseInt(viewId);
		}

		return null;
	}

	private String getItialRequestLine(BufferedReader inputFromClient) {
		String line = null;
		try {
			line = inputFromClient.readLine();
		} catch (IOException e) {
			Activator.logError(e);
		}
		
		if (line == null || line.isEmpty()) {
			return null;
		} else {
			return line;
		}
	}
	
	private Map<String, String> getRequestHeader(BufferedReader inputFromClient) {
		Map<String, String> requestHeaders = new HashMap<String, String>();
		try {
			String line;
			while ((line = inputFromClient.readLine()) != null && !line.isEmpty()) {
				int colonIndex = line.indexOf(':');
				if (colonIndex >= 0) {
					String key = line.substring(0, colonIndex).trim();
					String value = null;
					if (colonIndex < line.length()) {
						value = line.substring(colonIndex + 1).trim();
					}
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
		Date date = new Date();
		String HTTP_RESPONSE_DATE_HEADER =
			        "EEE, dd MMM yyyy HH:mm:ss zzz";
		DateFormat httpDateFormat =
		        new SimpleDateFormat(HTTP_RESPONSE_DATE_HEADER, Locale.US);
		httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String httpDate = httpDateFormat.format(date);
		
		String responceHeader = "HTTP/1.1 200 OK\r\n" +
				"Server: VPV server" +"\r\n"+
				"Content-Type: " + mimeType + "\r\n" +
				"Date: " + httpDate + "\r\n" +
				"Expires: Fri, 01 Jan 1990 00:00:00 GMT\r\n" +
				"Pragma: no-cache\r\n" +
				"Cache-Control: no-cache, must-revalidate, no-store\r\n" +
				"Vary: *\r\n" +

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
	
//	private String getRedirectHeader(String location){
//	    String responceHeader = "HTTP/1.1 302 Found\r\n" +
//                "Location: " + location +  "\r\n\r\n";
//	    return responceHeader;
//	}
}
