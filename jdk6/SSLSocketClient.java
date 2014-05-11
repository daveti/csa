/*
 * SSLSocektClient.java
 * An example using SSLSocketFactory() to create SSL connection
 * Ref: http://www.jguru.com/faq/view.jsp?EID=32388
 * BTW: I am NOT jguru!
 * May 6, 2014
 * daveti@cs.uoregon.edu
 * http://davejingtian.org
 */
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;

public class SSLSocketClient {

  public static void main(String args[]) throws Exception {

    String urlString = (args.length == 1) ? 
      args[0] : "http://twitter.com";
    URL url = new URL(urlString);

    Security.addProvider(
      new com.sun.net.ssl.internal.ssl.Provider());

    SSLSocketFactory factory = 
      (SSLSocketFactory)SSLSocketFactory.getDefault();
    SSLSocket socket = 
      (SSLSocket)factory.createSocket(url.getHost(), 443);

    /* Print the SSL parameter - algorithm */
    SSLParameters sslPara = socket.getSSLParameters();
    System.out.println("getCipherSuites(): " +
		sslPara.getCipherSuites() + "\n" +
		"getNeedClientAuth(): " +
		sslPara.getNeedClientAuth() + "\n" +
		"getProtocols(): " +
		sslPara.getProtocols() + "\n" +
		"getWantClientAuth(): " +
		sslPara.getWantClientAuth() + "\n"
		//"getEndpointIdentificationAlgorithm() [1.7]: " +
		//sslPara.getEndpointIdentificationAlgorithm()
		);

    PrintWriter out = new PrintWriter(
        new OutputStreamWriter(
          socket.getOutputStream()));
    out.println("GET " + urlString + " HTTP/1.1");
    out.println();
    out.flush();

    BufferedReader in = new BufferedReader(
      new InputStreamReader(
      socket.getInputStream()));

    String line;

    while ((line = in.readLine()) != null) {
      System.out.println(line);
    }

    out.close();
    in.close();
  }
}

