package server;

import static java.lang.System.out;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import networking.C4Net;
import session.C4ServerSession;

/**
 * Launch point of the server application. Establishes connection to client.
 * 
 * @author Julien Comtois, Frank Birikundavyi, Marjorie Olano Morales
 * @version 12/6/2015
 */
public class C4Server {

	/**
	 * Starting point of the application.
	 * 
	 * @param args Command line arguments. (Not used for this application)
	 */
	public static void main(String[] args) {
		displayIP();
		acceptConnection();
		System.exit(0);
	}

	/**
	 * Accepts the connection from several clients one after another,
	 * one client per thread. Will run until the program is force-closed.
	 */
	private static void acceptConnection() {
		ServerSocket serverSocket;
		Socket clientSocket;
		try {
			// Create a ServerSocket used to accept the user's connection
			serverSocket = new ServerSocket(C4Net.SERVER_PORT);

			// Infinite loop to keep accepting new clients
			while (true) {
				out.println("Ready for new client.");
				// Block until a client requests connection
				clientSocket = serverSocket.accept();
				// Display server state
				out.println("Client "
						+ clientSocket.getInetAddress().getHostAddress()
						+ " connected on port " + clientSocket.getPort());
				// Run a new instance of the session on a separate thread
				new Thread(new C4ServerSession(clientSocket)).start();
			}
		} catch (IOException e) {
			out.println("A communication problem has occured while establishing connection to client.");
		}
	}

	/**
	 * Retrieves the machine's IP address and displays it in console.
	 */
	private static void displayIP() {
		// Make InetAddress object needed to retrieve the server's IP
		InetAddress address;
		try {
			// Retrieve the server's local IP
			address = InetAddress.getLocalHost();
			// Display the IP of the server on console
			out.println("Server IP: " + address.getHostAddress());
		} catch (UnknownHostException e) {
			out.println("The server's IP address could not be determined.");
		}
	}
}
