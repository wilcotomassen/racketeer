import hypermedia.net.*;

UDP udp;  // define the UDP object

final String ip = "192.168.1.86";
final int port = 8888;
final int pixelCount = 16;

void setup() {
  size(800, 800);
  udp = new UDP(this);
}

void draw() {
    
  int playerPos = Math.round((float) mouseX / (float) width * (float) (pixelCount - 1));
  int ballPos = Math.round((float) mouseY / (float) height * (float) (pixelCount - 1));
  
  println(playerPos + " " + ballPos);
    
  byte[] buffer = {
    0x53, // S
    0x51, // Q
    (byte) playerPos,   // Player pos
    (byte) ballPos,     // Ball pos
    0x00 // Null message terminator
  };
  
  udp.send(buffer, ip, port );   // the message to send  
   
  
}