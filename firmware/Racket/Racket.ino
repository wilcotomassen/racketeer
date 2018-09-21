#include <ESP8266WiFi.h>

const char* ssid = "some-ssid";
const char* password = "some-password";

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
 
}

// the loop function runs over and over again forever
void loop() {
  Serial.println("HELLO");
  digitalWrite(LED_BUILTIN, LOW);   // Turn the LED on (Note that LOW is the voltage level
  // but actually the LED is on; this is because
  // it is active low on the ESP-01)
  delay(500);                      // Wait for a second
  digitalWrite(LED_BUILTIN, HIGH);  // Turn the LED off by making the voltage HIGH
  delay(1000);                      // Wait for two seconds (to demonstrate the active low LED)
}
