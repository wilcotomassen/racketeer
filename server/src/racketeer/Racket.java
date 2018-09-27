package racketeer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import processing.core.PApplet;

public class Racket {
	
	private final int PIXEL_COUNT = 16;
	
	private InetAddress targetAddress;
	private int targetPort;
	
	private byte[] udpBuffer;
	private DatagramSocket socket;
	
	// Current player pos
	private int playerPos = 0;
	private int ballPos = 0;
	
	public Racket(String host, int port) {
		try {
			targetAddress = InetAddress.getByName(host);
			targetPort = port;
			
			socket = new DatagramSocket();
			
			// Initialize packet buffer
			udpBuffer = new byte[] {
				0x53, 	// Header: S
				0x51, 	// Header: Q
				0x00,   // Payload: Player pos
				0x00,   // Payload: Ball pos
				0x00    // Terminator
			};
			
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void updatePositions(float playerPos, float ballPos) {
		boolean update = false;
		
		// Update player position
		int playerPixelPos = mapFloatToPixel(playerPos);
		if (playerPixelPos != playerPos) {
			this.playerPos = playerPixelPos;
			update = true;
		}
		
		// Update ball position
		int ballPixelPos = mapFloatToPixel(ballPos);			
		if (ballPixelPos != ballPos) {
			this.ballPos = ballPixelPos;
			update = true;
		}
		
		// Send update if anything changed
		if (update) {
			sendUdpMessage();
		}
		
	}
	
	private int mapFloatToPixel(float pos) {
		return (int) PApplet.map(pos, 0f, 1f, 0f, (float) PIXEL_COUNT - 1f);
	}
		
	public void sendUdpMessage() {
		
		try {
			
			// Update buffer
			udpBuffer[2] = (byte) playerPos;
			udpBuffer[3] = (byte) ballPos;
			
			// Send packet
			DatagramPacket packet = new DatagramPacket(udpBuffer, udpBuffer.length, targetAddress, targetPort);
			socket.send(packet);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
