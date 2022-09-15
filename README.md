# simple-IOT-app

This is an entry-level Internet of Things (IoT) application that demonstrates how IoT system works in general using a simple example.

## Preresiquite

An IoT system consists of these following components:
- Data receiver end: micro:bit should be suffice for sensor-related data. This application receives temperature and humidity data from micro:bits, however any sensor data should be usable with configurations.
- Gateway: an Android tablet is used, an USB OTG converter is also required.
- Internet server: Any cloud server provider is sufficient. This application uses ThingSpeak server as its cloud service provider.
- End-user end: Another end-user application handles this. Its main objective is to show end-user the data which has been transfered from Data receivers. Also it can perform several related tasks.

## Installation

This application is for Gateway. Clone the project then install to Gateway tablet using Android Studio.

## Usage

- Boot up the micro:bits
- Open the application on Gateway
- Plug in the micro:bits via USB OTG converter
- Make configurations as necessary
- Observe data changes on Server.
