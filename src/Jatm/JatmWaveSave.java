/*
 * Copyright (C) 2015 Ricardo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Jatm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Ricardo
 */
public class JatmWaveSave {
    // ==================== Constants ==============================
    private static final int Z80_CLOCK = 3250000; // CPU Clock Frequency
    private static final int PILOT_CYCLES = 512; // Pilot Tone Cycles

    // Pulse Lengths in Z80 cycles
    private static final int PILOT_PULSE_T    = 2011; // Pilot pulse width
    private static final int SYNC_PULSE_HI_T  = 601;  // Sync hi pulse width
    private static final int SYNC_PULSE_LO_T  = 791;  // Sync lo pulse width
    private static final int BIT0_PULSE_HI_T  = 795;  // Bit 0 hi pulse width
    private static final int BIT0_PULSE_LO_T  = 801;  // Bit 0 lo pulse width
    private static final int BIT1_PULSE_HI_T  = 1585; // Bit 1 hi pulse width
    private static final int BIT1_PULSE_LO_T  = 1591; // Bit 1 lo pulse width
    private static final int END_MARK_PULSE_HI_T = 903;  // End Mark hi pulse width
    private static final int END_MARK_PULSE_LO_T = 4187; // End Mark lo pulse width
    private static final int PAUSE_T         = 6500000; // Leading silence

    // ========================================================
    // WAV file parameters
    private RandomAccessFile audioFile;
    private long subChunk2Size;  // count data bytes written

    // Wave Tables
    private byte[] waveSilence; // Leading silence
    private byte[] wavePilot;   // pilotCycle tone
    private byte[] waveSync;    // Sync Pulses
    private byte[] waveBit0;    // Bit 0 wave
    private byte[] waveBit1;    // Bit 1 wave
    private byte[] waveEndMark; // End Mark pulses

    private int sampleFrameSize, bytesPerSample;

    // =================== User Options ================================
    private static int channelOption;
    private static int levelOption;
    private static int bitsOption;       // Sample Size in bits
    private static int sampleRateOption; // Sample Rate
    static { // Initial Values
        sampleRateOption = 44100; // CD quality Sample Rate
        bitsOption = 16;          // CD quality Sample Size
        channelOption = 1;        // Mono
        levelOption = 90;         // 90% Volume
    }

    public static void setBits(int bits) {
        bitsOption = 8;
        if(bits > 8) bitsOption = 16;
        if(bits > 16) bitsOption = 24;
        if(bits > 24) bitsOption = 32;
    }

    public static int getBits() {
        return bitsOption;
    }

    public static void setSampleRate(int sr) {
        sampleRateOption = 22050;
        if(sr > 22050) sampleRateOption = 44100;
        if(sr > 44100) sampleRateOption = 48000;
    }

    public static int getSampleRate() {
        return sampleRateOption;
    }

    public static void setStereo(boolean stereo) {
        if(stereo) channelOption = 2; // Stereo
        else       channelOption = 1; // Mono
    }

    public static boolean isStereo() {
        return (channelOption == 2);
    }

    public static void setLevel(int level) {
        levelOption = (level <   0) ?   0 : level;
        levelOption = (level > 100) ? 100 : level;
    }

    public static int getLevel() {
        return levelOption;
    }

    // ================== CONSTRUCTOR =======================
    public JatmWaveSave() {

    }

    /**
     * Create a WAV file
     * @param filename
     * @return  True if an error occur
     */
    public boolean open(String filename) {
        // Initialize WAV File Parameters
        subChunk2Size = 0;   // count data written
        bytesPerSample = bitsOption / 8;
        sampleFrameSize = channelOption * bytesPerSample; // Bytes in a data frame
        long chunkSize = 32;
        long subChunk1Size = 16;
        long byteRate = sampleRateOption * sampleFrameSize;

        // Create WAV File
        try {
            audioFile = new RandomAccessFile(new File(filename),"rw");
            // Write WAV file header
            audioFile.writeBytes("RIFF");                    // 00: 4 bytes, Chunk ID
            audioFile.write(to4bArray(chunkSize));           // 04: 4 bytes, Chunk Size = SubChunk2Size + 36
            audioFile.writeBytes("WAVE");                    // 08: 4 bytes, File Format
            audioFile.writeBytes("fmt ");                    // 12: 4 bytes, SubChunk1 ID
            audioFile.write(to4bArray(subChunk1Size));       // 16: 4 bytes, SubChunk1 Size
            audioFile.write(to2bArray(1));                   // 20: 2 bytes, Audio Format (PCM)
            audioFile.write(to2bArray(channelOption));       // 22: 2 bytes, Num Channels
            audioFile.write(to4bArray(sampleRateOption));    // 24: 4 bytes, Sample Rate
            audioFile.write(to4bArray(byteRate));            // 28: 4 bytes, Byte Rate
            audioFile.write(to2bArray(sampleFrameSize));     // 32: 2 bytes, Block Align
            audioFile.write(to2bArray(bitsOption));          // 34: 2 bytes, Bits per Sample
            audioFile.writeBytes("data");                    // 36: 4 bytes, SubChunk2 ID
            audioFile.write(to4bArray(subChunk2Size), 0, 4); // 40: 4 bytes, SubChunk2 Size
        } catch (IOException ex) {
            System.out.println("ERROR: Cannot save WAV file");
            return true; // Flag an error
        }

        // Calculte hi and lo levels according to sample size option
        double max;
        long hi, lo, offset = 0;
        switch(bitsOption) {
            case 8:  max = 127.0; offset = 128; break;
            case 16: max = 32767.0; break;
            case 24: max = 8388607.0; break;
            case 32: max = 2147483645.0; break;
            default:  max = 32767.0; break;
        }
        max *= (double)levelOption / 100.0; // scaled level
        hi = offset + (long)max; // Offset due to 8 bit option
        lo = offset - (long)max; // Offset due to 8 bit option

        // Construct Wave Tables
        waveSilence = createWaveTable(PAUSE_T, offset, PAUSE_T, offset);
        wavePilot = createWaveTable(PILOT_PULSE_T, hi, PILOT_PULSE_T, lo);
        waveSync = createWaveTable(SYNC_PULSE_HI_T, hi, SYNC_PULSE_LO_T, lo);
        waveBit0 = createWaveTable(BIT0_PULSE_HI_T, hi, BIT0_PULSE_LO_T, lo);
        waveBit1 = createWaveTable(BIT1_PULSE_HI_T, hi, BIT1_PULSE_LO_T, lo);
        waveEndMark = createWaveTable(END_MARK_PULSE_HI_T, hi, END_MARK_PULSE_LO_T, lo);

        return false; // Flag success
    }

    /**
     * Close the WAV file
     */
    /**
     * Close the WAV file
     * @return true if an error occur
     */
    public boolean close() {
        subChunk2Size += writeArray(waveSilence); // Write Trailing Silence
        try {
            // update Chunk Size
            audioFile.seek(4);  audioFile.write(to4bArray(subChunk2Size+36));
            // update subChunk2 Size
            audioFile.seek(40); audioFile.write(to4bArray(subChunk2Size));
            // Close file
            audioFile.close();
        } catch (IOException ex) {
            System.out.println("ERROR: Closing WAV file");
            return true;
        }
        return false; // Ok
    }

    /**
     * Save a Tape in currently oppened WAV file
     * @param tape JA tape file to save
     */
    public void save(JaTape tape) {
        // Write Tape Header
        subChunk2Size += writeArray(waveSilence);  // Write Leading Silence
        for(int j = 0; j < 8*PILOT_CYCLES; j++ ) { // Write Header Pilot
            subChunk2Size += writeArray(wavePilot);
        }
        subChunk2Size += saveBlock(tape.getHeaderBlock()); // Write Header Block

        // Write Tape Data
        for(int j = 0; j < PILOT_CYCLES; j++ ) {  // Write Data Pilot
            subChunk2Size += writeArray(wavePilot);
        }
        subChunk2Size += saveBlock(tape.getDataBlock()); // Write Data Block
    }

    // ================= PRIVATE =======================
    /**
     * save tape file block
     * @param block block array to save
     * @return number of bytes saved
     * @throws IOException
     */
    private long saveBlock(byte[] block) {
        long byteCount = 0; // byte counter
        byteCount += writeArray(waveSync);     // Write Sync pulse
        for(int i = 0; i < block.length; i++) { // Write block byte
            byteCount += saveByte(block[i]);
        }
        byteCount += writeArray(waveEndMark);       // Write End Mark
        return byteCount; // return WAV file bytes writen
    }

    /**
     * write a byte array in WAV file
     * @param byte array to write
     * @return bytes writen
     * @throws IOException
     */
    private long writeArray(byte[] array) {
        try {
            audioFile.write(array);
        } catch (IOException ex) {
            System.out.println("ERROR: writing to WAV file.");
            return 0;
        }
        return array.length;
    }

    /**
     * create a wave cycle array: hi>lo
     * @param hiLength cycle hi level
     * @param loLength cycle low level
     * @return WAV cycle array
     */
    private byte[] createWaveTable(int hiLength, long hiLevel, int loLength, long loLevel ) {
        int hiSamples = cycles2Samples(hiLength);
        int loSamples = cycles2Samples(loLength);
        int arraySize = (hiSamples + loSamples)*sampleFrameSize;
        byte[] array = new byte[arraySize];

        int index = 0;
        for(int i = 0; i < hiSamples; i++) { // fill high level pulse
            index = setSample(array, index, hiLevel);
        }
        for(int i = 0; i < loSamples; i++) { // fill low level pulse
            index = setSample(array, index, loLevel);
        }
        return array;
    }

    /**
     * build a WAV file sample in a byte array
     * @param array WAV array to set the sample in
     * @param index array index to set the sample
     * @param level sample level
     * @return next position array index
     */
    private int setSample(byte[] array, int index, long value) {
        for (int i=0; i<bytesPerSample; i++) {
            array[i+index] = (byte)(value & 0xFF); // set left channel
            if(channelOption==2) { // Stereo
                array[i+index+bytesPerSample] = array[i+index]; // set Right channel
            }
            value >>= 8; // rotate discarding LSB and positioning next byte
        }
        return index + sampleFrameSize; // return next array position
    }

    private long saveByte(byte b) {
        long total = 0;
        for(int i=0; i<8; i++) { // write 8 bits, hi bit first
            if((b & 0x80) != 0) {
                total += writeArray(waveBit1); // bit 1
            } else {
                total += writeArray(waveBit0); // bit 0
            }
            b <<= 1; // rotate next bit into position
        }
        return total;
    }

    // ================= Miscelaneous ====================
        /**
     * convert Z80 cycles count to WAV sample count
     * @param wavSampleRate
     * @param z80cycles
     * @return
     */
    private int cycles2Samples(int z80cycles) {
        long samples;
        samples = ((long)z80cycles * (long)sampleRateOption) / Z80_CLOCK;
        return (int)samples;
    }

        /** Convert int to 2 byte array, little endian */
    private byte[] to2bArray(int x) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte)(x & 0xFF); // Low byte first
        x >>>= 8;
        byteArray[1] = (byte)(x & 0xFF); // Hight byte last
        return byteArray;
    }

    /** Convert long to 4 byte array, little endian */
    private byte[] to4bArray(long x) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(x & 0xFF); // Least significant byte first
        x >>>= 8;
        byteArray[1] = (byte)(x & 0xFF);
        x >>>= 8;
        byteArray[2] = (byte)(x & 0xFF);
        x >>>= 8;
        byteArray[3] = (byte)(x & 0xFF); // Most significant byte last
        return byteArray;
    }

}
