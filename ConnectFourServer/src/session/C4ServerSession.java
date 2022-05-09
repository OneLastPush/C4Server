package session;

import static java.lang.System.out;

import java.io.IOException;
import java.net.Socket;

import game.C4Game;
import networking.C4Msg;
import networking.C4Net;

/**
 * Session manages the game and handles packets.
 * 
 * @author Julien Comtois, Frank Birikundavyi, Marjorie Olano Morales
 * @version 12/6/2015
 */
public class C4ServerSession implements Runnable {
	private C4Game game;
	private C4Net net;
	// Stored as string because only ever used for displaying
	private String portString;

	public C4ServerSession(Socket clientSocket) throws IOException {
		net = new C4Net(clientSocket);
		portString = "Port " + clientSocket.getPort() + ": ";
	}

	/**
	 * Handles a single client until they no longer want to play. Sends and
	 * received packets based on the state of the game.
	 * 
	 * @throws IOException Is thrown if a communication error occurs.
	 */
	@Override
	public void run() {
		try {
			boolean playAgain = true;
			byte[] packet;
			while (playAgain) {
				playAgain = false;
				game = new C4Game();
				net.sendPacket(C4Msg.START_GAME);
				out.println(portString + "Game started.");
				GameLoop: while (true) {
					packet = net.receivePacket();
					switch (C4Msg.values()[packet[0]]) {
					case CLIENT_MOVE:
						// Tell client if move was invalid
						if (game.makeClientMove(packet[1]) == -1) {
							net.sendPacket(C4Msg.BAD_MOVE);
							out.println(portString + "Invalid move from client.");
							break;
						} else if (game.checkWin() == 1) {
							net.sendPacket(C4Msg.GAME_WON_CLIENT);
							out.println(portString + "Client won the game.");
							break;
						}
						int aiMove = game.makeAIMove();

						/*
						 * Check if the AI won based off a class fields is more
						 * efficient than checking the whole board again. The
						 * server already knows it's won when playing the final
						 * move because it uses the same code for making the
						 * move and checking for wins.
						 */
						if (game.getAIWinStatus() || game.checkWin() == 2) {
							net.sendPacket(C4Msg.GAME_WON_AI, aiMove);
							out.println(portString + "AI won the game.");
						} else if (game.findTie()) {
							net.sendPacket(C4Msg.GAME_ENDED_TIE, aiMove);
							out.println(portString + "Game ended in a tie.");
						} else {
							net.sendPacket(C4Msg.SERVER_MOVE, aiMove);
						}
						break;
					case PLAY_AGAIN:
						playAgain = true;
						break GameLoop;
					case CLOSE_CONNECTION:
						out.println(portString + "Client closed Connection.");
						return;
					default:
						out.println(portString + "Unexpected packet received.");
					}
				}
			}
		} catch (IOException e) {
			out.println(portString + "A communication problem has occured.");
		}
	}
}
