#include <ESP8266WiFi.h>
#include <WiFiUdp.h>       

// Networking settings
const char* ssid = "*****";
const char* password = "****";
const unsigned int localPort = 8888;

char packetBuffer[UDP_TX_PACKET_MAX_SIZE]; //buffer to hold incoming packet,

WiFiUDP udp;

void setup() {
  
  // Start serial for debugging
  Serial.begin(9600);
  delay(10);

  Serial.print("Connecting to: ");
  Serial.println(ssid);

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

  // Start listening for UDP packets
  udp.begin(localPort);
 
}

// the loop function runs over and over again forever
void loop() {

  int packetSize = udp.parsePacket();
  if (packetSize) {

    Serial.println("PACKET!");

     // Read the packet into packetBufffer
    udp.read(packetBuffer, UDP_TX_PACKET_MAX_SIZE);
    Serial.println("Contents:");
    Serial.println(packetBuffer);

    // Blink led
    digitalWrite(LED_BUILTIN, LOW);
    delay(100);
    digitalWrite(LED_BUILTIN, HIGH);  
  }
  
}
