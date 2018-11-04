package com.mfarssac.temperature.util;

public enum SensorScan {

    INIT {
        private boolean busy;
        private long mTime;
        public void setTime() {mTime = System.currentTimeMillis();}
        public long getTime(){return mTime;}
        public void init(){ busy = false;}
        public boolean isBusy() {return busy;}
        public void setBusy(boolean isBusy) {busy = isBusy;}
        public SensorScan getNextState(){
            return START;
        }
        public void setRescanSensors(){}
        public void setNumSensors(int listSize){}
        public int getSensorToRead(){return 0;}
    },
    START {
        private boolean busy;
        private long mTime;
        public void setTime() {mTime = System.currentTimeMillis();}
        public long getTime(){return mTime;}
        public void init(){  busy = false; }
        public boolean isBusy() { return busy;}
        public void setBusy(boolean isBusy) {
            busy = isBusy;
        }
        public SensorScan getNextState(){
            SensorScan result;
            if (!busy)
                result = SCAN_SENSORS;
            else
                result = START;
            return result;
        }
        public void setRescanSensors(){}
        public void setNumSensors(int listSize){}
        public int getSensorToRead(){return 0;}
    },
    SCAN_SENSORS{
        private boolean busy;
        private long mTime;
        public void setTime() {mTime = System.currentTimeMillis();}
        public long getTime(){return mTime;}
        private int numSensors;
        public void init(){  busy = false; }
        public boolean isBusy() { return busy;}
        public void setNumSensors(int numSensors) {this.numSensors = numSensors;}
        public void setBusy(boolean isBusy) {
            busy = isBusy;
        }
        public SensorScan getNextState(){
            SensorScan result = SCAN_SENSORS;
            if (!busy) {
                if (numSensors > 0)
                    result = UPDATE_LCDS;
                else
                    result = START;
            }
        return result;
        }
        public void setRescanSensors(){}
        public int getSensorToRead(){return 0;}
    },
    UPDATE_LCDS {
        private boolean busy;
        private long mTime;
        public void setTime() {mTime = System.currentTimeMillis();}
        public long getTime(){return mTime;}
        public void init(){
            busy = false;
        }
        public boolean isBusy() {
            return busy;
        }
        public void setBusy(boolean isBusy) {
            busy = isBusy;
        }
        public SensorScan getNextState(){
            SensorScan result;
            if (!busy)
                result= READ_SENSOR;
            else
                result = UPDATE_LCDS;
            return result;
        }
        public void setRescanSensors(){}
        public void setNumSensors(int listSize){}
        public int getSensorToRead(){return 0;}
    },
    READ_SENSOR {
        private boolean busy;
        private long mTime;
        public void setTime() {mTime = System.currentTimeMillis();}
        public long getTime(){return mTime;}
        private int numSensors;
        private int sensorToRead;
        private boolean rescanSensors;

        public void init(){
            busy = false;
            sensorToRead =0;
            numSensors =0;
            rescanSensors = false;
        }

        public void setBusy(boolean isBusy) {
            this.busy = isBusy;
        }
        public boolean isBusy() {  return busy;}
        public SensorScan getNextState(){
            SensorScan result = READ_SENSOR;

            if(!busy)
                if (rescanSensors) {
                    rescanSensors = false;
                    result = START;
                } else {
                    pollNextSensor();
                    result = UPDATE_LCDS;
                }
            return result;
        }
        public void setRescanSensors(){rescanSensors=true;}
        public void setNumSensors(int listSize){this.numSensors = listSize;}
        public int getSensorToRead(){return sensorToRead;}
        public int pollNextSensor(){

            sensorToRead++;
            if (sensorToRead>=numSensors)
                sensorToRead =0;

            return sensorToRead; }
    },
    PERSIST_RESULTS {
        private boolean busy;
        private long mTime;
        public void setTime() {mTime = System.currentTimeMillis();}
        public long getTime(){return mTime;}
        public void init(){ busy = false;}
        public boolean isBusy() {return busy;}
        public void setBusy(boolean isBusy) {busy = isBusy;}
        public SensorScan getNextState(){
            return UPDATE_LCDS;
        }
        public void setRescanSensors(){}
        public void setNumSensors(int listSize){}
        public int getSensorToRead(){return 0;}
    };

    public abstract void setBusy(boolean isBusy);
    public abstract boolean isBusy();
    public abstract SensorScan getNextState();
    public abstract void init();

    public abstract void setRescanSensors();
    public abstract void setNumSensors(int listSize);
    public abstract int getSensorToRead();
    public abstract void setTime();
    public abstract long getTime();

}
