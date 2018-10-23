# Android App with data of a Raspberry Pi 3B board over WiFi runing Android Things linked to three Texas Instruments sensor tags over Bluetooth Low Energy.

Note: This prototype consists of Software (Sw) and Hardware (Hw) and is currently under development (last updated October 22nd 2018). See below for a short summary of the project evolution.

## Project description

Using a Mobile phone, a Raspberry Pi 3B runing the Android Things OS and three Texas Instruments Sensors Tags connected with the board over Bluetooth Low Energy, the project shows how to monitor the heating temperature of a flat with sensors located at different rooms.

### Youtube: working prototype

[![Watch the video](https://user-images.githubusercontent.com/18221570/47273220-4cc8c380-d591-11e8-910c-9c92676e7f75.PNG)](https://www.youtube.com/watch?v=RkMu2JuVxSU&t=67s)

YouTube video link: [https://www.youtube.com/watch?v=RkMu2JuVxSU&t=67s](https://www.youtube.com/watch?v=RkMu2JuVxSU&t=67s)

## Design considerations

Even it is obvious that one display could do the job to display the readings of three sensors it was decided for clarity purposes to associate one LCD to each of the BLE sensors. The extra cost of this seems at the design phase paid off by a better user experience. It is for this reason that the design uses three displays instead of one, making clear to the user which display reflects information of which sensor.

### Project phases

At present stage, the project counts only with the IoT part, beeing a hardware with LCDs that connects to the sensors. 

Moving on, the project will let set the on/off swiching of the heating system based on the temperature at different sensors. For example at night the relevant temperature will be at the bed room and during the day at the studio or living room. Using pushbuttons the user will define at what point of the day her/his preferences change and these will be taken into account when creating a custom machine learning model. On following days similar temperature changes will lead to related confort temperatures in the house leting the system adjust the temperature switch trigger in a smart way, not just by defining threshold levels at different points of time, but leting the user define behavious over time.

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

## License
Copyright 2018 Marc Farssac

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. 
