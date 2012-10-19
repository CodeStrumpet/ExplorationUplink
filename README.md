ExplorationUplink
=================

*Android, Arduino, and HTML5 based Robot OS*

Overview
---------
We use WebSockets to stream video and control commands between an Android phone and a browser.  Commands received by the phone are relayed to a microcontroller where they get translated into motor output.

Currently we are using the Android ADK for the bridge between the phone and MC, with an Arduino ADK Mega as the MC.  Different phone/MC transport mechanisms such as IOIO, PropBrige or MicroBridge could easily be swapped in to replace the ADK if desired.

Thanks go to TooTallNate for his java_websocket library (https://github.com/TooTallNate/Java-WebSocket/).



Steps to get the code up and running
------------------------------------

Run the ExplorationUplink Android app on a phone with OS level supporting the ADK (2.3.6+ I believe).

Open Android/html/explorationuplink.html in a browser.  The Android app will display the IP address of the phone in a textfield on the screen--input that into the field in the webpage and press 'Connect'.

(If you have an ADK board, the first step should be plugging the phone USB into the board and that will launch the android app.  All else should be the same)


