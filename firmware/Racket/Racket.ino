#include <ESP8266WiFi.h>
#include <WiFiUdp.h>       
#include <Adafruit_NeoPixel.h>

// Networking settings
const char* ssid = "*****";
const char* password = "****";
const unsigned int localPort = 8888;

// LED Settings
#define LED_PIN   2
#define NUM_PIXELS 16
#define OFF_COLOR 10


char packetBuffer[UDP_TX_PACKET_MAX_SIZE]; //buffer to hold incoming packet,

WiFiUDP udp;
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUM_PIXELS, LED_PIN, NEO_GRB + NEO_KHZ800);

void setup() {
  
  // Start serial for debugging
  Serial.begin(9600);
  delay(10);

  Serial.print("Connecting to: ");
  Serial.println(ssid);

  // Initialize pixels to black
  pixels.begin();
  for (int i = 0; i < NUM_PIXELS; i++){
    pixels.setPixelColor(i, pixels.Color(OFF_COLOR, OFF_COLOR, OFF_COLOR));
  }
  pixels.show();

  // Connecto to wifi
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  // Print connection details
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.flush();
  Serial.end();

  for (int i = 0; i < NUM_PIXELS; i++){
    pixels.setPixelColor(i, pixels.Color(OFF_COLOR,160,OFF_COLOR));
    pixels.show();
    delay(100);
  }
  for (int i = NUM_PIXELS-1; i > -1; i--){
    pixels.setPixelColor(i, pixels.Color(OFF_COLOR,OFF_COLOR,OFF_COLOR));
    pixels.show();
    delay(100);
  }

  // Start listening for UDP packets
  udp.begin(localPort);
  
}

void loop() {

  int packetSize = udp.parsePacket();
  if (packetSize) {

    // Read the packet into packetBufffer
    udp.read(packetBuffer, UDP_TX_PACKET_MAX_SIZE);

    // Validate packet
    if (
      packetBuffer[0] != 0x53       // S
      || packetBuffer[1] != 0x51    // Q
      || packetBuffer[4] != 0x00    // NULL
      ) {
       return;
    }

    digitalWrite(LED_BUILTIN, LOW);

    int playerIndex = (int) packetBuffer[2];
    int ballIndex = (int) packetBuffer[3];
        
    // Update pixels
    for (int i = 0; i < NUM_PIXELS; i++) {
      if (i == ballIndex) {
        // Ball
        pixels.setPixelColor(i, pixels.Color(OFF_COLOR, 160, OFF_COLOR));
      } else if (
        (i >= playerIndex - 1 && i <= playerIndex + 1) // Normal
        || (playerIndex == NUM_PIXELS-1 && i == 0) // Edge A
        || (playerIndex == 0 && i == NUM_PIXELS-1) // Edge B
        ) {
        // Player
        pixels.setPixelColor(i, pixels.Color(255, OFF_COLOR,OFF_COLOR));
      } else {
        pixels.setPixelColor(i, pixels.Color(OFF_COLOR, OFF_COLOR, OFF_COLOR));
      }
    }
    pixels.show();        

    digitalWrite(LED_BUILTIN, HIGH);  
  }

}
