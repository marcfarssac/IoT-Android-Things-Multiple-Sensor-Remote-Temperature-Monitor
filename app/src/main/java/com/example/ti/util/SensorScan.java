package com.example.ti.util;

public enum SensorScan {

    INIT {
        public boolean isBusy() {return false;}
        public void setBusy(boolean isBusy) {}
        public SensorScan getNextState(){
            return START;
        }
    },
    START {
        private boolean isWorking;
        public boolean isBusy() {
            return isWorking;}
        public void setBusy(boolean isBusy) {
            isWorking = isBusy;
        }
        public SensorScan getNextState(){
            SensorScan result;
            if (!isWorking)
                result = SCAN_SENSORS;
            else
                result = START;
            return result;
        }
    },
    SCAN_SENSORS{
        private boolean isWorking;
        public boolean isBusy() {
            return isWorking;}
        public void setBusy(boolean isBusy) {
            isWorking = isBusy;
        }
        public SensorScan getNextState(){
            SensorScan result = SCAN_SENSORS;
            if (!isWorking) {
                if (getNumSensors() != 0)
                    result = UPDATE_LCDS;
                else
                    result = START;
            }
        return result;
        }
    },
    UPDATE_LCDS {
        private boolean isWorking;
        public boolean isBusy() {
            return isWorking;
        }
        public void setBusy(boolean isBusy) {
            isWorking = isBusy;
        }
        public SensorScan getNextState(){
            SensorScan result = READ_SENSOR;
            if (!isWorking)
                result= READ_SENSOR;
            else
                result = UPDATE_LCDS;
            return result;
        }
    },
    READ_SENSOR {
        private boolean isBusy;

        private int numSensors;
        private int sensorBeingRead;

        public void setBusy(boolean isBusy) {
            this.isBusy = isBusy;
        }
        public boolean isBusy() {  return isBusy;}
        public SensorScan getNextState(){
            SensorScan result = READ_SENSOR;
            if(!isBusy) {
                if (pollNextSensor() != 0)
                    result = READ_SENSOR;
                else
                    result = START;
            }
            return result;
        }
        public void setNumSensors(int listSize){this.numSensors = listSize;}
        public int getSensorToRead(){return sensorBeingRead;}
        public int pollNextSensor(){

            sensorBeingRead++;
            if (sensorBeingRead>=numSensors)
                sensorBeingRead =0;

            return sensorBeingRead; }
    };

    private int numSensors;
    private int sensorToRead;

    public abstract void setBusy(boolean isBusy);
    public abstract boolean isBusy();
    public abstract SensorScan getNextState();

    public int getSensorToRead(){return sensorToRead;}
    public void initSensorsReading(){
        READ_SENSOR.setBusy(false);
        UPDATE_LCDS.setBusy(false);
        SCAN_SENSORS.setBusy(false);
        sensorToRead =0;
    }
    public void setNumSensors(int sensors) { numSensors = sensors; }
    public int  getNumSensors(){ return numSensors; }
}
