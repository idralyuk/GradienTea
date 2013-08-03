// include the neo pixel library
#include <Adafruit_NeoPixel.h>

// how many leds in our string?
static const int NUM_LEDS = 256;

// Parameter 1 = number of pixels in strip
// Parameter 2 = pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_RGB     Pixels are wired for RGB bitstream
//   NEO_GRB     Pixels are wired for GRB bitstream
//   NEO_KHZ400  400 KHz bitstream (e.g. FLORA pixels)
//   NEO_KHZ800  800 KHz bitstream (e.g. High Density LED strip)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUM_LEDS, 12, NEO_GRB + NEO_KHZ800);

// buffer to hold colors of our LEDs
char colorValues[NUM_LEDS*3];

void setup() {
  strip.begin();
  strip.show();
  
  // Do a quick test/demo to show that things are working
  for (int i=0; i<60; i++) { flashAll(strip.Color((i%20)*2,i%30,i%60)); delay(10); }

  // initialize to black (off)
  for (int i=0; i < NUM_LEDS*3; i++) {
    colorValues[i] = 0;
  }

  // initialize the strip to the current values
  for(int i=0; i<NUM_LEDS; i++) {
    int d = i*3;
    uint32_t c = strip.Color(colorValues[d], colorValues[d+1], colorValues[d+2]);
    strip.setPixelColor(i, c);
  }
  // update the strip
  strip.show();

   //Initialize serial and wait for port to open:
  Serial.begin(115200);
  while (!Serial) {
    ; // wait for port
  }
  Serial.println("READY");
}


void loop() {
  while (true) {
    int bufferPos = 0;
    
    while (bufferPos < NUM_LEDS*3) {
      int color = Serial.read();
      if (color >= 0) {
        colorValues[bufferPos++] = color;
      }
    }
    
    // feed the data to the leds
    for(int i=0; i<NUM_LEDS; i++) {
      int d = i*3;
      uint32_t c = strip.Color(colorValues[d], colorValues[d+1], colorValues[d+2]);
      strip.setPixelColor(i, c);
    }
    
    // update the strip
    strip.show();
    
    // Clear up the serial buffer
    while (Serial.available() > 0) Serial.read();
    
    // Let the sender know we're ready for more data
    Serial.println("READY");
  }
  
  
  
//  
//  // wait for bytes on serial port
//  if (Serial.available() > 0) {
//    // read 3 bytes per LED from serial port
//    int bytesRead = Serial.readBytes(colorValues, NUM_LEDS*3);
//    // check we got a full complement of bytes
//    if (bytesRead < NUM_LEDS*3) {
//      Serial.println("ERROR");
//      // something went wrong, abandon this loop
//      return;
//    }
//    // feed the data to the leds
//    for(int i=0; i<NUM_LEDS; i++) {
//      int d = i*3;
//      uint32_t c = strip.Color(colorValues[d], colorValues[d+1], colorValues[d+2]);
//      strip.setPixelColor(i, c);
//    }
//    // update the strip
//    strip.show();
//    Serial.println("READY");
//  }
}

void flashAll(uint32_t color) {
  // Do a quick test/demo to show that things are working
  for (int i=0; i < NUM_LEDS; i++) {
    strip.setPixelColor(i, color);
  }
  strip.show();
}
