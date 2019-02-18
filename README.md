# Android App with data of a Raspberry Pi 3B board over WiFi runing Android Things linked to three Texas Instruments sensor tags over Bluetooth Low Energy.

Note: This prototype consists of Sofware (Sw) and Hardware (Hw). Both run on an Android mobile phone and an Android Things Hardware. The project consists of two modules, one that runs on each of the devices. BLE devices have their own firmware developed by Texas Instruments. 

It under development (last updated November 11th 2018). See below for a short summary of the project evolution. Wathc the [Youtube chanel here](https://www.youtube.com/playlist?list=PLk7A4gvo5SIdjq2N2y8oxlR_yW7zcPpmn) to learn about the project progress and status.

## Project description

Using a Mobile phone, a Raspberry Pi 3B runing the Android Things OS and three Texas Instruments Sensors Tags connected with the board over Bluetooth Low Energy, the project shows how to monitor in Realtime the heating temperature of a flat with sensors located at different rooms using a mobile phone with an internet connection.

### Youtube: working prototype v 0.5

This version adds Realtime readings from the Android App (Mobile client). Compared to the previous version, where readings in the LCD and the Mobile phone where not sincronized (due to the Firebase Cloud Messaging latency), in this video and product version we can see how they are showing the same values all the time. (The only delay is due the fact that we are sending readings to the cloud every five seconds, thus the Mobile App updates the readings every five seconds also. This is not a problem since actually reading a Bluetooth Low Energy sensor in a loop takes seven seconds for each).

[![iot-real-time](https://user-images.githubusercontent.com/18221570/48338394-d8e87b00-e665-11e8-856b-1996e0023a09.PNG)](https://www.youtube.com/watch?v=9z9KXS_KiRE&t=304s)

YouTube video link: [https://www.youtube.com/watch?v=9z9KXS_KiRE&t=304s](https://www.youtube.com/watch?v=9z9KXS_KiRE&t=304s)

### Youtube: working prototype v 0.21

Sensor readings are sent to the Andorid App (Mobile phone) through FCM (Firebase Cloud Messaging). This introduces some delays and differences on the temperature readings on the LCDs and the mobile devices.

[![iot-project-screenshoot-v021](https://user-images.githubusercontent.com/18221570/48338569-45fc1080-e666-11e8-99f9-23720b5cd5d5.PNG)](https://www.youtube.com/watch?v=6op35qc54do&t=2s)

YouTube video link: [https://www.youtube.com/watch?v=6op35qc54do&t=2s](https://www.youtube.com/watch?v=6op35qc54do&t=2s)


### Youtube: working prototype v 0.1

This is the first prototype. In this one there is still no connection with the cloud. It just binds a service with each of the Bluetooth Low Energy sensors, reads the temperature and displays it on the screen. Later project revisions improve this and send the temperature / sensor readings to the cloud).

[![Watch the video](https://user-images.githubusercontent.com/18221570/47273220-4cc8c380-d591-11e8-910c-9c92676e7f75.PNG)](https://www.youtube.com/watch?v=RkMu2JuVxSU&t=67s)

YouTube video link: [https://www.youtube.com/watch?v=RkMu2JuVxSU&t=67s](https://www.youtube.com/watch?v=RkMu2JuVxSU&t=67s)

At present stage, the project counts only with the IoT part, beeing a hardware with LCDs that connects to the sensors. 

Moving on, the project will let set the on/off swiching of the heating system based on the temperature at different sensors. For example at night the relevant temperature will be at the bed room and during the day at the studio or living room. Using pushbuttons the user will define at what point of the day her/his preferences change and these will be taken into account when creating a custom machine learning model. On following days similar temperature changes will lead to related confort temperatures in the house leting the system adjust the temperature switch trigger in a smart way, not just by defining threshold levels at different points of time, but leting the user define behavious over time.

## Design considerations

Even it is obvious that one display could do the job to display the readings of three sensors it was decided for clarity purposes to associate one LCD to each of the BLE sensors. The extra cost of this seems at the design phase paid off by a better user experience. It is for this reason that the design uses three displays instead of one, making clear to the user which display reflects information of which sensor.

#### Preliminary hardware

An Android Things app driving General Purpose Input Outputs (Gpio) to display data on three external 16x1 LEDs and connecting to an undetermined number of Bluetooth Low Energy (BLE) sensors to read their sensors.

![20181014_201614](https://user-images.githubusercontent.com/18221570/46932739-4b5b4080-d051-11e8-8667-92d46162b39f.jpg)

To reduce the number of used IOs a demultiplexer was used to drive three "work in progress" leds, one for each display.

![iot-project-display-5](https://user-images.githubusercontent.com/18221570/47273348-489da580-d593-11e8-807f-244f8ee50a03.PNG)

![iot-project-display-4](https://user-images.githubusercontent.com/18221570/47273354-53f0d100-d593-11e8-8a27-cd37baaaf2d6.PNG)

![iot-project-display-3](https://user-images.githubusercontent.com/18221570/47273356-5c490c00-d593-11e8-9dc4-38c068bd7dfb.PNG)

### Preliminary software

On top of the standard Android Things architecture it was needed a driver for the LCD, seting the Register Selector (RS) signal and the Read / Write (RW) one to select the operation to perform by the LCD.

![operations-lcd](https://user-images.githubusercontent.com/18221570/47273489-30c72100-d595-11e8-984d-7bd31aa9a346.PNG)

The LCD display is built in a LSI controller with two 8-bit registers, an instructions register (IR) and a data register (DR). The IR stores instruction codes, such as display clear and cursor shift. The IR can only be written from the MPU. The DR temporarily stores data to be written or read from DDRAM. When address information is written itno the IR, the data is stored into the DR from DDRAM. By the register selector (RS) signal, these two registers can be selected.

![lcd-timing-diagram](https://user-images.githubusercontent.com/18221570/47273615-b055ef80-d597-11e8-9f7b-aacdec20c635.PNG)

## Issues solved

### LCD 16x1

This is an old type of LCD superseded by 2, 3, 4 and more lines LCDs that lacks some clarity in the documentation available. The instrucutions available were not matching the LCD behaviour and required to model the behaviour of the hardware with some trial / errors.

### Bluetooth Low Energy

BLE sensors use a client-server model where each sensor gets linked to the Android App with a bound service. Having three sensors requires to bind this service for each of them every time that we want to make a reading. Different alternatives have been considered, like having three services ruuning and just binding a service to each of the sensors. 

This approach has been rejected since the mobile phone has only one Bluetooth Low Energy driver and use it to connect to multiple servers would have required an apparently too complicated architecture and software programming.

The alternative has been to connect the service to each sensor every time we wanted to make the reading. This is from a time perspective not an issue, since we can have multiple readings per minute for each of the sensors. Nevertheless it is considered that other approaches could save battery and they shall be considered at latter stages of the project.

### Sensor Tag with old firmware version

The sensor tags used were purchased in year 2014 and can not be reflashed with an updated firmware. Two of the sensors count with one Firmware verson and the other one with an older one. After some headaches it has been found out that some Bluetooth connection issues were not related to the Software running on the client, but running on one of the sensor tags with the oldest firmware revision. The work around has been to restart the sensor tag every time the connection was lost and this worked well with the rest of the solution.

## Android App screen

![screenshot_20181112-093839_iot temperature sensors_framed](https://user-images.githubusercontent.com/18221570/48338890-20233b80-e667-11e8-882b-67dc7865bff3.png)

## Notes

This public repository doesn't contain the latest and greatest code which is kept private. Please let me know if you would like to see the final one. Write me at [marc.farssac@gmail.com](mailto:marc.farssac@gmail.com)

## License
Copyright 2018 Marc Farssac

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
