/*******************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.vpe.preview.core.Activator;
import org.jboss.tools.vpe.preview.core.transform.ResourceAcceptor;
import org.jboss.tools.vpe.preview.core.transform.VpvController;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class VpvSocketProcessor implements Runnable {

    public static final String INITIAL_REQUEST_LINE = "Initial request line"; //$NON-NLS-1$
    public static final String REFERER = "Referer"; //$NON-NLS-1$
    public static final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
    public static final String HOST = "Host"; //$NON-NLS-1$
    public static final String UTF_8 = "UTF-8";  //$NON-NLS-1$

	private Socket clientSocket;
	private VpvController vpvController;


	public VpvSocketProcessor(Socket clientSocket, VpvController vpvController) {
		this.clientSocket = clientSocket;
		this.vpvController = vpvController;
	}

	@Override
	public void run() {
		try {	
			BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), UTF_8));
			DataOutputStream outputToClient = new DataOutputStream(clientSocket.getOutputStream());
			
			String initialContextLine = getInitialRequestLine(inputFromClient);
			if (initialContextLine != null) {
				Map<String, String> requestHeader = getRequestHeader(inputFromClient);				
			
				if (requestHeader.isEmpty()) {
				    processNotFound(outputToClient);	    
				    return;
				}
				
				processRequest(initialContextLine, requestHeader, outputToClient, inputFromClient);
			}
		} catch (IOException e) {
			Activator.logError(e);
		}
	}

	private void processRequest(String initialRequestLine, final Map<String, String> requestHeaders, final DataOutputStream outputToClient, final BufferedReader inputFromClient) {
		String httpRequestString = getHttpRequestString(initialRequestLine);
		Map<String, String> queryParametersMap = parseUrlParameters(httpRequestString);

		String path = getPath(httpRequestString);
		String webrootPath = getWebrootPath(queryParametersMap, requestHeaders);
		String fullPath = webrootPath + path;
		Integer viewId = getViewId(queryParametersMap);
		vpvController.getResource(fullPath, viewId, new ResourceAcceptor() {

            @Override
            public void acceptText(String text, String mimeType) {
            	String etag = formEtagForText();
                String responceHeader = getOkResponceHeader(mimeType,  etag);
                sendOkResponse(responceHeader, outputToClient, text);
            }

			@Override
			public void acceptFile(File file, String mimeType) {
				String okHeader = getOkResponceHeader(mimeType, null);
				sendOkResponse(okHeader, outputToClient, file);
			}


			@Override
			public void acceptError() {
				 processNotFound(outputToClient);	   
			}
			
			
			private String formEtagForText() {
				return String.valueOf(new Date().getTime());
			}
			

			private void sendOkResponse(String header, DataOutputStream outputToclient, File file) {
				try {
					outputToClient.write(header.getBytes(UTF_8));
					sendFile(file, outputToClient);
				} catch (IOException e) {
					//Reporting errors disabled. See JBIDE-17705 
				} finally {
					try {
						outputToClient.close();
						inputFromClient.close();
						clientSocket.close();
					} catch (IOException e) {
						Activator.logError(e);
					}
				}
			}
			
			private void sendOkResponse(String header, DataOutputStream outputToclient, String text) {
				try {
					outputToClient.write(header.getBytes(UTF_8));
					outputToClient.write(text.getBytes(UTF_8));
				} catch (IOException e) {
					//Reporting errors disabled. See JBIDE-17705
				} finally {
					try {
						outputToClient.close();
						inputFromClient.close();
						clientSocket.close();
					} catch (IOException e) {
						Activator.logError(e);
					}
				}
			}
        });
    }
	
	private void processNotFound(DataOutputStream outputToClient) {
		String notFoundHeader = getNotFoundHeader();
		try {
			outputToClient.write(notFoundHeader.getBytes(UTF_8));
		} catch (IOException e) {
			//Reporting errors disabled. See JBIDE-17705
		} finally {
			try {
				outputToClient.close();
			} catch (IOException e) {
				Activator.logError(e);
			}
		}
	}

	private Map<String, String> parseUrlParameters(String urlString) {
		int delimiterPosition = getDilimiterPosition(urlString);

		if (delimiterPosition == -1) {
			return Collections.emptyMap();
		}

		String parameterString = urlString.substring(delimiterPosition + 1, urlString.length());

		String[] parameterArray = parameterString.split("&"); //$NON-NLS-1$
		Map<String, String> parameterMap = new HashMap<String, String>();
		for (String param : parameterArray) {
			if (param.length() > 0) {
				String[] nameValue = param.split("="); //$NON-NLS-1$
				String name = nameValue[0];
				String value = nameValue.length > 1 ? nameValue[1] : null;
				if(value!=null) {
					try {
						value = URLDecoder.decode(value, UTF_8);
					} catch (UnsupportedEncodingException e) {
						Activator.logError(e);
					}
				}
				parameterMap.put(name, value);
			}
		}
		return parameterMap;
	}

	int getDilimiterPosition(String httpRequestString) {
		return httpRequestString.indexOf('?');
	}

	private String getHttpRequestString(String initialRequestLine) {
		String[] data = initialRequestLine.split(" "); //$NON-NLS-1$
		return data[1];
	}

	private String getPath(String httpRequestString) {
		String path = httpRequestString;
		int delimiter = getDilimiterPosition(httpRequestString);
		int pathEnd = delimiter != -1 ? delimiter : path.length();
		path = path.substring(0, pathEnd); 
		try {
			path = URLDecoder.decode(path, UTF_8);
		} catch (UnsupportedEncodingException e) {
			Activator.logError(e);
		}
		return path;
	}

	private String getWebrootPath(Map<String, String> queryParametersMap, Map<String, String> requestHeaders) {
		String path = queryParametersMap.get(HttpConstants.WEBROOT_PATH);
		if (path == null) {
			String referer = requestHeaders.get(REFERER);
			if (referer != null) {
				path = parseUrlParameters(referer).get(HttpConstants.WEBROOT_PATH);
			}
		}
		return path != null ? path : "";
	}

	private Integer getViewId(Map<String, String> queryParametersMap) {
		String viewId = queryParametersMap.get(HttpConstants.VIEW_ID);
		if (viewId != null) {
			return Integer.parseInt(viewId);
		}

		return null;
	}

	private String getInitialRequestLine(BufferedReader inputFromClient) {
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

    private void sendFile(File file, DataOutputStream outputToClient) {
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
        	//Reporting errors disabled. See JBIDE-17705
        } finally {
            try {
            	if (fileInputStream != null) {
            		fileInputStream.close();
            	}
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
    	

	private String getOkResponceHeader(String mimeType, String eTag) {
		String responceHeader = "HTTP/1.1 200 OK\r\n" + //$NON-NLS-1$
				"Server: VPV server" +"\r\n"+ //$NON-NLS-1$ //$NON-NLS-2$
				"Content-Type: " + mimeType + "; charset=UTF-8\r\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"Cache-Control: max-age=0\r\n" +  //$NON-NLS-1$
				"Etag: " + eTag + "\r\n" +  //$NON-NLS-1$//$NON-NLS-2$
				"Connection: close\r\n\r\n"; //$NON-NLS-1$
		return responceHeader;
	}
	
	private String getNotFoundHeader() {
        String responceHeader = "HTTP/1.1 404 Not Found\r\n" + //$NON-NLS-1$
                "Content-Type: text/html; charset=UTF-8\r\n" + //$NON-NLS-1$
                "Connection: close\r\n\r\n" + //$NON-NLS-1$
                "<!DOCTYPE HTML>" + //$NON-NLS-1$
                "<h1>404 Not Found<//h1>"; //$NON-NLS-1$
        return responceHeader;
	} 
	
}
