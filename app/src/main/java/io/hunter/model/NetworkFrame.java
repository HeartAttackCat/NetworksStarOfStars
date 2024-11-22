package io.hunter.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.common.base.Charsets;

public class NetworkFrame {

    private byte src;

    private byte networkSrc;

    private byte dest;

    private byte networkDest;

    private byte control;

    private byte crc;

    private byte size;

    private byte[] meat;

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

    public NetworkFrame(byte src, byte networkSrc, byte dest, byte networkDest, byte control, String message) {
        this(src, networkSrc, dest, networkDest, control, (byte) message.length(), message.getBytes(StandardCharsets.US_ASCII));
    }

    public NetworkFrame(byte[] totalFrame) {
        this.src = totalFrame[0];
        this.networkSrc = totalFrame[1];
        this.dest = totalFrame[2];
        this.networkDest = totalFrame[3];
        this.control = totalFrame[4];
        this.crc = totalFrame[5];
        this.size = totalFrame[6];
        
        meat = new byte[size];

        for (int i = 0; i < size; i++) {
            this.meat[i] = totalFrame[7+i];
        }
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

    public String getMessage() {
        return new String(meat, Charsets.US_ASCII);
    }

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

    public boolean checkCRC() {
        byte calculated = genCRC();
        if (calculated != crc) {
            return false;
        }
        return true;
    }

    public void debugFrame(String program) {
        System.out.println("["+program+"][Frame Debug] Source:" + src + ", Network Source:" + networkSrc + ", Destination:" + dest + ", Network Destination:" + networkDest + ", CRC:" + crc + ", Size:" + size + "\n\tMessage:" + getMessage());
    }
}
