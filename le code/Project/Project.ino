#include <SoftwareSerial.h>

// Global constants
// Step motor:
const int IN1 = 12;
const int IN2 = 11;
const int IN3 = 10;
const int IN4 = 9;
// ESP8266-01:
const int rxESP = 7;
const int txESP = 8;
const int rst = 5;
// HC-05
const int rxBT = 3;
const int txBT = 4;
const int vccBT = 6;
// Led pin
const int isConnected = 2;
// Factory parameters
const String clientID = "SomeID";
const String btPswd = "SomePswd";
// Server parameters
const String server = "tapcontroller.000webhostapp.com";
const String getURI = "/getValue.php";
const String putNewURI = "/putNewID.php";
const String dataPG = "clientID="+clientID;
const String putValueURI = "/putValue.php";
const String dataPV = "clientID="+clientID+"&flow=0&duration=0";
// State variables
String ssid = "";
String pswd = ""; 
double flow = 0;
long duration = 0;
boolean paired = false;
int failures = 0;

SoftwareSerial ESP(rxESP, txESP);
SoftwareSerial BTSerial(rxBT, txBT);

void setup() {
  ESP.begin(9600);
  BTSerial.begin(9600);
  // disconnect from all access-points
  ESP.println("AT+CWJAP=\"\",\"\"");
  pinMode(IN1, OUTPUT); 
  pinMode(IN2, OUTPUT); 
  pinMode(IN3, OUTPUT); 
  pinMode(IN4, OUTPUT);
  pinMode(isConnected, OUTPUT);
  pinMode(vccBT, OUTPUT);
  digitalWrite(vccBT, HIGH);
  pinMode(rst, OUTPUT);
  digitalWrite(rst, HIGH);
}

void loop() {
  if(paired){
     ESP.listen();
     excuteInstruction();
  }
  else{
    BTSerial.listen();
    if(BTSerial.available()){
      String apData = BTSerial.readString();
      int diez = apData.indexOf("#");
      if(diez>0){
        int i = 0;
        while(i<diez){
          ssid += apData.charAt(i);
          i += 1;
        }
        i += 1;
        while(i<apData.length()){
          pswd += apData.charAt(i);
          i += 1;
        }
        paired = true;
        digitalWrite(vccBT, LOW);
        while(!connectToAP()){
          delay(1);
        }
        httppost(putNewURI,dataPG);
      }
    }
  }
}

void openTheTap(double dflow){
  if(flow+dflow > 0 and flow+dflow <= 100){
    flow = flow + dflow;
    double nbStep = 2048*dflow/100;
    stepper(nbStep);
  }
  return;
}

void closeTheTap(){
    double nbStep = -2048*flow/100;
    stepper(nbStep);
    flow = 0;
    duration = 0;
}

// Taken from https://www.aranacorp.com/fr/pilotez-un-moteur-pas-a-pas-avec-arduino/

void stepper(double nbStep){
   int Direction;
   int Steps = 0;
   if(nbStep>=0){
       Direction=1;
   }
   else{
       Direction=0;
       nbStep=-nbStep;
   }
   for (int x=0;x<nbStep*8;x++){
     switch(Steps){
        case 0:
          digitalWrite(IN1, LOW); 
          digitalWrite(IN2, LOW);
          digitalWrite(IN3, LOW);
          digitalWrite(IN4, HIGH);
        break; 
        case 1:
          digitalWrite(IN1, LOW); 
          digitalWrite(IN2, LOW);
          digitalWrite(IN3, HIGH);
          digitalWrite(IN4, HIGH);
        break; 
        case 2:
          digitalWrite(IN1, LOW); 
          digitalWrite(IN2, LOW);
          digitalWrite(IN3, HIGH);
          digitalWrite(IN4, LOW);
        break; 
        case 3:
          digitalWrite(IN1, LOW); 
          digitalWrite(IN2, HIGH);
          digitalWrite(IN3, HIGH);
          digitalWrite(IN4, LOW);
        break; 
        case 4:
          digitalWrite(IN1, LOW); 
          digitalWrite(IN2, HIGH);
          digitalWrite(IN3, LOW);
          digitalWrite(IN4, LOW);
        break; 
        case 5:
          digitalWrite(IN1, HIGH); 
          digitalWrite(IN2, HIGH);
          digitalWrite(IN3, LOW);
          digitalWrite(IN4, LOW);
        break; 
          case 6:
          digitalWrite(IN1, HIGH); 
          digitalWrite(IN2, LOW);
          digitalWrite(IN3, LOW);
          digitalWrite(IN4, LOW);
        break; 
        case 7:
          digitalWrite(IN1, HIGH); 
          digitalWrite(IN2, LOW);
          digitalWrite(IN3, LOW);
          digitalWrite(IN4, HIGH);
        break; 
        default:
          digitalWrite(IN1, LOW); 
          digitalWrite(IN2, LOW);
          digitalWrite(IN3, LOW);
          digitalWrite(IN4, LOW);
        break; 
     }
     delayMicroseconds(1000);
     if(Direction==1){ 
        Steps++;
     }
     if(Direction==0){ 
        Steps--; 
     }
     if(Steps>7){
        Steps=0;
     }
     if(Steps<0){
        Steps=7; 
     }
    } 
}

// Partially taken from https://www.les-electroniciens.com/videos/arduino-ep16-installation-du-module-wifi-esp8266

boolean connectToAP(){
  
  boolean flag = false;

  ESP.listen();
  
  ESP.println("AT+CWJAP=\""+ ssid + "\",\"" + pswd +"\"");
  delay(3000);
  flag = ESP.find("OK");
  
  if(flag){
    digitalWrite(isConnected,HIGH);
  }
  else{
    digitalWrite(isConnected,LOW);
  }

  return flag;
}

// Partially taken from https://www.instructables.com/id/Arduino-Esp8266-Post-Data-to-Website/

String httppost (String uri,String data) {

    String response = "";
    
    String postRequest =
    
    "POST " + uri + " HTTP/1.0\r\n" +
    
    "Host: " + server + "\r\n" +
    
    "Accept: *" + "/" + "*\r\n" +
    
    "Content-Length: " + data.length() + "\r\n" +
    
    "Content-Type: application/x-www-form-urlencoded\r\n" +
    
    "\r\n" + data;
    
    String sendCmd = "AT+CIPSEND=";//determine the number of caracters to be sent.

    do{

      // TCP connexion

      do {

        connectToAP();
  
        ESP.println("AT+CIPSTART=\"TCP\",\"" + server + "\",80");//start a TCP connection.

        failures += 1;

        if(failures>10){
          wakeUp();
        }
        
      } 
      while(!ESP.find("OK"));

      // Sending the length

      do{

        ESP.print(sendCmd);
    
        ESP.println(postRequest.length() );
        
        delay(500);

        failures += 1;

        if(failures>10){
          wakeUp();
        }
      }
      while(!ESP.find(">"));

      // Sending the POST query

      ESP.println(postRequest);

      failures += 1;

      if(failures>10){
        wakeUp();
      }
      
    }
    while(!ESP.find("SEND OK"));

    failures = 0;
    
    long int timeout = 3000;

    long int t0 = millis();
    
    while ( (t0 + timeout) > millis())
    {
        while (ESP.available())
        {
            char c = ESP.read();
            response += c;
        }
    }
    
    // close the connection
    
    ESP.println("AT+CIPCLOSE");
    delay(100);

    return response;
}

double getData(){
  
  String response = httppost(getURI,dataPG);
  int startingStar = response.indexOf("*");
  int endingStar = response.indexOf("*",startingStar+1);
  int diez = response.indexOf("#");
  String printedFlow = "";
  String printedDuration = "";
  
  if(startingStar<0 or endingStar<0 or diez<0){
    return flow;
  }
  else{
    int buff = startingStar+1;
    while(buff<diez){
      printedFlow += response.charAt(buff);
      buff += 1;
    }
      buff += 1;
    while(buff<endingStar){
      printedDuration += response.charAt(buff);
      buff += 1;
    }
  }
  
  if(printedFlow == "F" or printedDuration == "F"){
    // Serial.println("This object isn't recorded in the database");
  }
  else{
    char *stopstringDuration;
    long durationTmp = strtol(printedDuration.c_str(), &stopstringDuration,0);
    if(!(stopstringDuration == printedDuration.c_str())){
      duration = durationTmp;
    }
    char *stopstringFlow;
    double flowTmp = strtod(printedFlow.c_str(), &stopstringFlow);
    if(!(stopstringFlow == printedFlow.c_str())){
      return flowTmp;
    }
  }

  return flow;
}

void excuteInstruction(){
    double newFlow = getData();
    // init value in server:
    httppost(putValueURI,dataPV);
    double dflow = newFlow-flow;
    openTheTap(dflow);
    flow = newFlow;
    delay(duration*1000*60);
    closeTheTap();
}

void wakeUp(){
   digitalWrite(rst,LOW);
   delay(100);
   digitalWrite(rst,HIGH);
   failures = 0;
}
