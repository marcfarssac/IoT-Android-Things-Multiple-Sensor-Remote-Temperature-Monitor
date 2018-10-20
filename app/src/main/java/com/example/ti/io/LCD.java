package com.example.ti.io;

import android.support.annotation.IntRange;
import android.util.Log;

import com.google.android.things.pio.Gpio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Liquid crystal driver.
 */
public class LCD implements Runnable, AutoCloseable {

    private static final String TAG = LCD.class.getSimpleName();

    /**
     * Display Data Ram Address.
     * Has to be int, no unsigned byte unfortunately...
     */
    private static final int LCD_SET_DDRAM_ADDR = 0x80;
    private static final byte LCD_DDRAM_ADDR_COL1_ROW0 = 0x40;
    private static final byte LCD_DDRAM_ADDR_COL9_ROW1 = 0x40;

    // Commands
    private static final byte LCD_CLEAR_DISPLAY = 0x01;
    private static final byte LCD_RETURN_HOME = 0x02;

    private static final byte LCD_ENTRY_MODE_SET = 0x07; // Shift entire display
    private static final byte LCD_DISPLAY_ON = 0x0F; // Display on, cursor on, blinking

    // can be used any time to shift display info
    public static final byte LCD_CURSOR_OR_DISPLAY_SHIFT = 0x10; // 0x14 shift right Shift to the right (without moving content)

    private static final byte LCD_FUNCTION_SET = 0x20; // 4 BIT MODE, 1 LINE DISPLAY, 5X8 Characters
    private static final byte LCD_8_BIT_FUNCTION_2_LINE = 0x28; // 4 bit but mode, 2 line display, 5x8 dots format display mode

    private static final byte ROWS = 2;
    private static final byte COLUMNS = 8;

    private Gpio resetPin;

    private Gpio enablePin;

    private Gpio backLight;

    private List<Gpio> dataBus;

    private boolean isEnabled;

    /**
     *
     * d4..d7 Four high order bidirectional tristate data bus pins.
     * Used for data transfer and receive between the MPU and the HD44780U.
     * DB7 can be used as a busy flag.
     *
     * @param rs Selects registers.
     *           0: Instruction register (for write) Busy flag:address counter (for read)
     *           1: Data register (for write and read)
     * @param e  Starts data read/write
     * @throws IOException
     */
    public LCD(Gpio rs,
               Gpio e,
               Gpio d4,
               Gpio d5,
               Gpio d6,
               Gpio d7,
               Gpio bl) throws IOException {
        this.resetPin = rs;
        this.enablePin = e;
        this.backLight = bl;

        rs.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        e.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        bl.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        dataBus = new ArrayList<Gpio>();
        dataBus.add(d4);
        dataBus.add(d5);
        dataBus.add(d6);
        dataBus.add(d7);

        for (Gpio bit : dataBus) {
            bit.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        }

        isEnabled = false;
        delay(10);

        sendCommand(LCD_CLEAR_DISPLAY, true); // 0x01
//        sendCommand(LCD_RETURN_HOME); // It turns display on ok !!
        sendCommand(LCD_ENTRY_MODE_SET, /*four bit mode*/ true); // 0x07
//        sendCommand(LCD_DISPLAY_ON);
        sendCommand(LCD_CURSOR_OR_DISPLAY_SHIFT); // 0x10
        sendCommand(LCD_FUNCTION_SET); // Works 0x20
    }

    public void init() throws IOException {
        setBackLight(false);
        clearDisplay();
        goHome();
        for (int i = 0; i < 80; i++) {
            write(" ");
        }
    }

    public boolean isEnabled(){
        return isEnabled;
    }

    public void setIsEnabled(boolean enable){
        isEnabled = enable;
    }

    public void setBackLight(boolean enable) throws IOException {
        backLight.setValue(enable);
    }

    public void write(String val) throws IOException {
        resetPin.setValue(true);
        for (char b : val.toCharArray()) {
            write(b);
        }
        resetPin.setValue(true);
    }

    public void goHome() throws IOException {
        sendCommand(LCD_RETURN_HOME);
    }

    public void shift() throws IOException {
        sendCommand(LCD_CURSOR_OR_DISPLAY_SHIFT); // Works
    }

    private void write(char c) throws IOException {
        resetPin.setValue(true);
        write8((byte) c);
        resetPin.setValue(true);
    }

    public void setCursor(@IntRange(from = 1, to = ROWS) int row,
                          @IntRange(from = 1, to = COLUMNS) int column) throws IOException {
            sendCommand((byte) (LCD_SET_DDRAM_ADDR | ((LCD_DDRAM_ADDR_COL1_ROW0 * (row - 1)) + (column - 1))), true);

    }

    public void clearDisplay() throws IOException {
        sendCommand(LCD_CLEAR_DISPLAY); // It clears ok !
    }

    /**
     * @param command
     * @param fourBitMode
     * @throws IOException
     */
    private void sendCommand(byte command, boolean fourBitMode) throws IOException {

        resetPin.setValue(false);
        if (fourBitMode) {
            write4(command);
        } else {
            write8(command);
        }
    }

    /**
     * Sends a command to the display controller.
     *
     * @param command
     * @throws IOException
     */
    private void sendCommand(byte command) throws IOException {
        sendCommand(command, true);

    }

    /**
     * Write 8 bits to the data bus using GPIO.
     *
     * @param value Value to write. Assumes <b>unsigned.</b>
     * @throws IOException
     */
    private void write8(byte value) throws IOException {
        write4((byte) (value >> 4));
        write4(value);
    }

    /**
     * Write 4 bits to the data bus using GPIO.
     *
     * @param value Value to write. Assumes <b>unsigned.</b>
     * @throws IOException
     */
    private void write4(byte value) throws IOException {
        for (int i = 0; i < dataBus.size(); i++) {
            Gpio pin = dataBus.get(i);
            boolean bit = ((value >> i & 0x01) != 0);
            pin.setValue(bit);
            delay(1);
        }
        pulseEnable();
        delay(1);
    }

    private void pulseEnable() throws IOException {
        enablePin.setValue(false);
        delay(1);
        enablePin.setValue(true);
        delay(1);
        enablePin.setValue(false);
        delay(5);
    }

    @Override
    public void run() {

    }

    /**
     * Convenience method, just in case my threading strategy changes.
     *
     * @param ms time in ms to wait.
     */
    public void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        resetPin.close();
        enablePin.close();

        for (Gpio pin : dataBus) {
            pin.close();
        }
    }

    public void writeText(String text1, String text2) throws IOException {


            for (int i = 0; i < text1.length(); i++) {
                write(text1.substring(i, i + 1));
            }

            for (int i = 0; i < 40 - text1.length(); i++) {
                write(" ");
            }
            for (int i = 0; i < text2.length(); i++) {
                write(text2.substring(i, i + 1));
            }

            for (int i = 0; i < 40 - text2.length(); i++) {
                write(" ");
            }
    }

}
