#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include <SoftwareServo.h>
#include <Servo.h>


typedef enum ADKMessageType {
	ADKMessageTypeNone = 0,
	ADKMessageTypeString = 1,
	ADKMessageTypeSensor = 2,
	ADKMessageTypeControl = 3
} ADKMessageType;


typedef enum ADKControlMessageType {
	ADKControlMessageTypeNone = 0,
	ADKControlMessageTypeDirection = 1,
	ADKControlMessageTypeDrive = 2
} ADKControlMessageType;




//Motor controller constants
const int motorPin = 4;
const int fullReverse = 1200;//in microseconds
const int stopState = 1444;// really 1500
const int fullForward = 1600;
const int deltaV = 4;

int pinState = LOW;
int smoothOut = 5; //in milliseconds
int currentSpeed = stopState;

bool didReadCommand = false;  // used to decide whether to send Ack message


#define  LED_PIN  13
#define SERVO_PIN 7

AndroidAccessory acc("ExplorationUplink",
	"Model",
	"Description",
	"1.0",
	"http://yoursite.com",
"0000000012345678");

SoftwareServo steeringServo;

int steeringServoAngle = 90; // initialize dead center
long lastWriteTime = 0;
int writeDelay = 1000;

int throttleLevel = 0;
int throttleLevelLimit = 4;

// pin declarations 
const int analogPinCount = 6; 

bool movingForward = false;
long forwardStartTime = 0;
int forwardMovementInterval = 2000;


void setup()
{
	// set communiation speed
	Serial.begin(115200); 
	
	// Initialize pins
	pinMode(LED_PIN, OUTPUT);
	pinMode(SERVO_PIN, OUTPUT);
	pinMode(motorPin, OUTPUT);
	
	// Initialize steering servo
	steeringServo.attach(SERVO_PIN);
	
	// Initialize drive motor controller
	Serial.println("Starting 10 second delay");
	delay(10000);
	initializeMotor();
	Serial.println("Initialized MotorControl");
  	delay(300);

	// run through test drive code
	speedUp();
	Serial.println("Max speed.");
	delay(2000);
	slowDown();
	completeStop();

	// start ADKConnection
	acc.powerOn();
}

void loop()
{   
	
	if (acc.isConnected()) {
		
		// turn on LED to indicate we have an Accessory connection
		digitalWrite(LED_PIN, HIGH); 

		// only reading 3 byte ADKControlMessages right now
		byte buffer[3]; // type, control_type, value
		
		int len = acc.read(buffer, 3, 1); // read data into buffer variable

		if (len > 0) {
			didReadCommand = true;

			int type = (int)buffer[0];
			Serial.println("got message of type:  " + type);
			
			if (type == ADKMessageTypeControl) {

				int control_type = (int)buffer[1];
				int control_value = (int)buffer[2];

				if (control_type = ADKControlMessageTypeDirection) {
					Serial.print("got directional control message with type:  ");
					Serial.print(control_type);
					Serial.print("  value:  ");
					Serial.println(control_value);

					if (control_value == 4 && steeringServoAngle > 50) { // left turn

						steeringServoAngle -= 10;
						
					} else if (control_value == 6 && steeringServoAngle < 130) { // right turn
					  
						steeringServoAngle += 10;
						
					} else if (control_value == 8) { // forward
						
						if (throttleLevel < throttleLevelLimit) {
							throttleLevel++;
							speedUp();
							forwardStartTime = millis();
							movingForward = true;
						}
						
					} else if (control_value == 5) { // slow down
						
						if (throttleLevel == 1) {
							throttleLevel = 0;
							completeStop();
						} else {
							throttleLevel--;
							slowDown();
						}
						
					} else if (control_value == 1) { // stop
						
						throttleLevel = 0;
						completeStop();
						
					}

				} else {
					Serial.println("Not currently supporting other ADKControlMessageType: " + control_type);
				}
			} else {
				Serial.println("Not currently supporting ADKMessageType: " + type);
			}
		}


		int sensorValues[analogPinCount];
																																																																	// Read Sensors 
		for (int i = 0; i < analogPinCount; i++) {
			sensorValues[i] = analogRead(i);
		}

		//if (millis() - lastWriteTime > writeDelay) {
		if (didReadCommand) {
			didReadCommand = false;
			int writeLength = 1; // simple Ack
			byte write_msg[writeLength];
			write_msg[0] = (byte) 1;

			acc.write(write_msg, writeLength);

			Serial.println("wrote acknowledgment...");			

			lastWriteTime = millis();			
		}

	} else {
		digitalWrite(LED_PIN , LOW); // turn led off to indicate lack of Accessory connection
	}
	
	
	// kill forward momentum if rover has been moving longer than forwardMovementInterval
	if (movingForward) {
		if (millis() - forwardStartTime > forwardMovementInterval) {
			completeStop();
			movingForward = false;
		}
	}
	
	// Update the servos to current angle
	steeringServo.write(steeringServoAngle);  
	SoftwareServo::refresh();
	
}

void writeIntToByteArray(int paramInt, byte *byteArray)
{

	byteArray[0] = (paramInt >> 24) & 0xFF;
	byteArray[1] = (paramInt >> 16) & 0xFF;
	byteArray[2] = (paramInt >> 8) & 0xFF;
	byteArray[3] = paramInt & 0xFF;
}

void pulseOut(int waveLength){
	Serial.print("waveLength: ");
	Serial.println(waveLength);
	pinState = HIGH;
	digitalWrite(motorPin, pinState);
	delayMicroseconds(waveLength);
	pinState = LOW;
	digitalWrite(motorPin, pinState);
	return;
}

void initializeMotor(){
	pulseOut(stopState);
	delay(6);
	pulseOut(stopState + 21);
	return;
}

void speedUp(){
	for(int i=stopState;i<fullForward;i=i+deltaV){
		pulseOut(i);
		delay(smoothOut);
	}
	return;
}
void slowDown(){
	for(int i=fullForward;i>stopState;i=i-deltaV){
		pulseOut(i);
		delay(smoothOut);
	}
	return;
}
void completeStop(){
	pulseOut(stopState);
	return;
}
