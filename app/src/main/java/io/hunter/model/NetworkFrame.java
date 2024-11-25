package io.hunter.model;

import java.nio.charset.StandardCharsets;

import com.google.common.base.Charsets;

public class NetworkFrame {

    //The source of the message
    private byte src;

    //The network Source of the message
    private byte networkSrc;

    //The destination of the message
    private byte dest;

    //The network destination of the message
    private byte networkDest;

    //If the message is a control message
    private byte control;

    //The CRC of the message
    private byte crc;

    //The size of the message
    private byte size;

    //The raw bytes that are the message.
    private byte[] meat;

    //Used to generate a frame with all the parameters but requires you to generate the crc.
    public NetworkFrame(byte src,byte networkSrc, byte dest, byte networkDest, byte control, byte crc, byte size, byte message[]) {
        this.src = src;
        this.networkSrc = networkSrc;
        this.dest = dest;
        this.networkDest = networkDest;
        this.control = control;
        this.crc = crc;
        this.size = size;

        meat = new byte[size];

        for(int i = 0; i < message.length ; i++) {
            meat[i] = message[i];
        }

    }

    //Used to generate a new frame from theb eginning and generates the crc at creation and uses a byte.
    public NetworkFrame(byte src,byte networkSrc, byte dest, byte networkDest, byte control, byte size, byte message[]) {
        this.src = src;
        this.networkSrc = networkSrc;
        this.dest = dest;
        this.networkDest = networkDest;
        this.control = control;
        this.size = size;

        meat = new byte[size];

        for(int i = 0; i < message.length ; i++) {
            meat[i] = message[i];
        }

        this.crc = genCRC();
    }

    //Generates the crc and converts the string message to a byte array.
    public NetworkFrame(byte src, byte networkSrc, byte dest, byte networkDest, byte control, String message) {
        this(src, networkSrc, dest, networkDest, control, (byte) message.length(), message.getBytes(StandardCharsets.US_ASCII));
    }

    public byte getSrc() {
        return src;
    }

    public byte getDest() {
        return dest;
    }

    public byte getNetworkDest() {
        return networkDest;
    }

    public byte getNetworkSource() {
        return networkSrc;
    }

    public byte getControl() {
        return control;
    }

    public byte getSize() {
        return size;
    }

    public byte[] getMeat() {
        return meat;
    }

    /**
     * Gives you the meat of the message as a string.
     * @return the decoded string from the meat.
     */
    public String getMessage() {
        return new String(meat, Charsets.US_ASCII);
    }

    /**
     * Generates the frame into one unfified byte array to be transmitted.
     * @return The byte array of the entire message.
     */
    public byte[] generateFrame() {
        byte[] totalFrame = new byte[7+size];
        totalFrame[0] = src;
        totalFrame[1] = networkSrc;
        totalFrame[2] = dest;
        totalFrame[3] = networkDest;
        totalFrame[4] = control;
        totalFrame[5] = crc;
        totalFrame[6] = size;

        for (int i = 0; i < size; i++) {
            totalFrame[7+i] = meat[i];
        }

        return totalFrame;
    }

    /**
     * Generates the CRC
     * 
     * @return The CRC of the program.
     */
    private byte genCRC() {
        byte sum = 0;
        for(int i = 0; i < size; i++) {
            sum += meat[i];
        }

        sum += src;
        sum += networkSrc;
        sum += dest;
        sum += networkDest;
        sum += control;
        sum += size;

        return sum;
    }

    /**
     * Corrupts the first byte in the message by setting it to n
     */
    public void corruptData() {
        meat[0] = 'n';
    }

    /**
     * Checks the CRC of the frame
     * 
     * @return Will return false if message is corrupted, else it returns true.
     */
    public boolean checkCRC() {
        byte calculated = genCRC();
        if (calculated != crc) {
            return false;
        }
        return true;
    }

    /**
     * Prints frame to the console, used only for debugging the program.
     * @param program A string in which can be usedto indentify which thread its being called form.
     */
    public void debugFrame(String program) {
        System.out.println("["+program+"][Frame Debug] Source:" + src + ", Network Source:" + networkSrc + ", Destination:" + dest + ", Network Destination:" + networkDest + ", CRC:" + crc + ", Size:" + size + "\n\tMessage:" + getMessage());
    }
}
