package com.example.ti.util;

public enum SensorScan {

    START {
        private boolean isWorking;
        public boolean isBusy() {
            return isWorking;}
        public void setBusy(boolean isBusy) {
            isWorking = isBusy;
        }
        public SensorScan getNextState(){return SCAN_SENSORS;}
    },
    SCAN_SENSORS{
        private boolean isWorking;
        public boolean isBusy() {
            return isWorking;}
        public void setBusy(boolean isBusy) {
            isWorking = isBusy;
        }
        public SensorScan getNextState(){
            if (getNumSensors()!=0)
                return UPDATE_LCDS;
            else
                return START;
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
        public SensorScan getNextState(){return READ_SENSOR;}
    },
    READ_SENSOR {
        private boolean isBusy;
        private int numSensors;
        private int sensorToRead;

        public void setBusy(boolean isBusy) {
            this.isBusy = isBusy;
        }
        public boolean isBusy() {  return isBusy;}
        public SensorScan getNextState(){
            if (pollNextSensor()!=0)
                return READ_SENSOR;
            else
                return START;
        }
        public void setNumSensors(int listSize){this.numSensors = listSize;}
        public int pollNextSensor(){

            sensorToRead++;
            if (sensorToRead>=numSensors)
                sensorToRead =0;

            return sensorToRead; }
    };

    private int numSensors;
    private int sensorToRead;

    public abstract void setBusy(boolean isBusy);
    public abstract boolean isBusy();
    public abstract SensorScan getNextState();

    public int getSensorToRead(){return sensorToRead;}
    public void initSensorsReading(){ sensorToRead =0;}
    public void setNumSensors(int sensors) { numSensors = sensors; }
    public int  getNumSensors(){ return numSensors; }
}
