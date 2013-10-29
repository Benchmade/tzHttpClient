package com.tmall.search.aio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class BIOHttpClient {
	public BIOHttpClient() throws IOException {
		
	}
	public void doGet() throws IOException{
		Socket socket = new Socket("localhost", 8080);
		StringBuilder sb = new StringBuilder();
		sb.append("GET /BenchmadeWeb/xxx?q=1 HTTP/1.0");
		sb.append("\r\n");
		sb.append("Host: localhost:8080");
		sb.append("\r\n");
		sb.append("\r\n");
		OutputStream os = socket.getOutputStream();
		os.write(sb.toString().getBytes());
		os.flush();
		InputStream is = socket.getInputStream();
		BufferedReader lnr = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = lnr.readLine()) != null) {
			//System.out.println(line);
		}
		socket.close();
	
	}
	

	public static void main(String[] args) throws Exception {
		BIOHttpClient b = new BIOHttpClient();
		long s = System.currentTimeMillis();
		for(int i=0;i<1;i++){
			b.doGet();
		}
		System.out.println(System.currentTimeMillis()-s);
	}
}
