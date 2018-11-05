/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mfarssac.temperature.sensorhub.collector;

import android.util.Log;

import com.mfarssac.temperature.sensorhub.SensorData;

import java.util.ArrayList;
import java.util.List;

public class CC2541Collector implements SensorCollector {

    private static final String TAG = CC2541Collector.class.getSimpleName();

    private static final String SENSOR_TEMPERATURE = "temperature";
    private static final String SENSOR_HUMIDITY = "humidity";
    private static final String SENSOR_PRESSURE = "ambient_pressure";

    private boolean isTemperatureEnabled;
    private boolean isPressureEnabled;
    private boolean isHumidityEnabled;
    private float tempReading;

    private boolean isHumidityAvailable;

    public CC2541Collector() {
        // By default, enable all available sensors. Different initial state can be set by calling
        // setEnabled before activate.
        this.isTemperatureEnabled = true;
        this.isPressureEnabled = false;
        this.isHumidityEnabled = false;
        this.tempReading = 0;
    }

    @Override
    public boolean activate() {
        return true;
    }

    @Override
    public void setEnabled(String sensor, boolean enabled) {
    }

    @Override
    public boolean isEnabled(String sensor) {
        switch (sensor) {
            case SENSOR_TEMPERATURE:
                return isTemperatureEnabled;
            case SENSOR_PRESSURE:
                return isPressureEnabled;
            case SENSOR_HUMIDITY:
                return isHumidityAvailable && isHumidityEnabled;
            default:
                Log.w(TAG, "Unknown sensor " + sensor + ". Ignoring request");
        }
        return false;
    }

    @Override
    public List<String> getAvailableSensors() {
        List<String> sensors = new ArrayList<>();
        sensors.add(SENSOR_TEMPERATURE);
        return sensors;
    }

    @Override
    public List<String> getEnabledSensors() {
        List<String> sensors = new ArrayList<>();
        if (isEnabled(SENSOR_TEMPERATURE)) {
            sensors.add(SENSOR_TEMPERATURE);
        }
        if (isEnabled(SENSOR_PRESSURE)) {
            sensors.add(SENSOR_PRESSURE);
        }
        if (isEnabled(SENSOR_HUMIDITY)) {
            sensors.add(SENSOR_HUMIDITY);
        }
        return sensors;
    }

    @Override
    public void collectRecentReadings(List<SensorData> output) {
                // If both temperature and pressure are enabled, we can read both with a single
                // I2C read, so we will report both values with the same timestamp
                long now = System.currentTimeMillis();
                output.add(new SensorData(now, SENSOR_TEMPERATURE, tempReading));
    }

    @Override
    public void closeQuietly() {
    }

    public void setTempReading(float tempReading) {
        this.tempReading = tempReading;
    }

}
