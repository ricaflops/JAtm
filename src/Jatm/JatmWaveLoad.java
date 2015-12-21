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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Ricardo
 */
public class JatmWaveLoad {
    // ==================== Constants ==============================
    private static final int Z80_CLOCK = 3250000; // CPU Clock Frequency

    // Pulse Lengths in Z80 cycles
    private static final int PILOT_PULSE_T    = 2011; // Pilot pulse width
    private static final int SYNC_HI_PULSE_T  = 601;  // Sync hi pulse width
    private static final int SYNC_LO_PULSE_T  = 791;  // Sync lo pulse width
    private static final int BIT0_HI_PULSE_T  = 795;  // Bit 0 hi pulse width
    private static final int BIT0_LO_PULSE_T  = 801;  // Bit 0 lo pulse width
    private static final int BIT1_HI_PULSE_T  = 1585; // Bit 1 hi pulse width
    private static final int BIT1_LO_PULSE_T  = 1591; // Bit 1 lo pulse width
    private static final int ERROR_T          = SYNC_HI_PULSE_T/2;  // Pulse Length Error tolerance

    // Cycle lengths in Z80 cycles
    private static final int SYNC_T  = (SYNC_HI_PULSE_T + SYNC_LO_PULSE_T)/2; // Sync
    private static final int BIT0_T  = (BIT0_HI_PULSE_T + BIT0_LO_PULSE_T)/2; // Bit 0
    private static final int BIT1_T  = (BIT1_HI_PULSE_T + BIT1_LO_PULSE_T)/2; // Bit 1

    // ================================================================
    private JatmFirFilter firFilter;  // FIR Filter

    // Scaled Time width
    private int pulseTolerance; // cycle width error limit in audio samples
    private int pilotPulse;
    private int syncPulse;
    private int bit0Pulse;
    private int bit1Pulse;
    private static enum LoadState {
        SEARCHING,
        PILOTING,
        LOADING,
        DONE
    }

    // ====================== User Options ==============================
    private static int channelOption;
    private static float levelOption;
    private static float histeresysOption;
    private static boolean inverseOption;
    private static boolean filterOption;
    // Initialize User options
    static {
        channelOption = 0;     // Mix Left and Right Channels
        levelOption = 0.05F;      // Detection Level
        histeresysOption = 0.01F; // Detection Level Histeresys
        inverseOption = false; // no inverted wave form
        filterOption = false;  // Low pass filter off
    }

    /**
     * Set Channel Option
     * @param option 0=Left+Right, 1=Left, 2=Right
     */
    public static void setChannelOption(int option) {
        if (option < 0 || option > 2) {
            option = 0;
        }
        channelOption = option;
    }

    /**
     * Get Channel Option
     * @return  0=Left+Right, 1=Left, 2=Right
     */
    public static int getChannelOption() {
        return channelOption;
    }

    /**
     * Set Detection Level % Option
     * @param option Level %: -100 to +100
     */
    public static void setLevelOption(int option) {
        levelOption = (float)option / 100F;
        // limit level to -1.0 to +1.0
        levelOption = (levelOption <= +1F) ? levelOption : +1F;
        levelOption = (levelOption >= -1F) ? levelOption : -1F;
    }

    /**
     * Get Detection % Level Option
     * @return Level: 0 to +100%
     */
    public static int getLevelOption() {
        return (int)(100F * levelOption);
    }

    /**
     * Set Histeresys % to Detection Level
     * @param option Histeresys %: 0 to +100
     */
    public static void setHisteresysOption(int option) {
        histeresysOption = (float)option / 100F;
        // Limit level to 0.0 to 1.0
        histeresysOption = (histeresysOption <= 1F) ? histeresysOption : 1F;
        histeresysOption = (histeresysOption >= 0F) ? histeresysOption : 0F;
    }

    /**
     * Get Histeresys % Level Option
     * @return Histeresys: -100% to +100%
     */
    public static int getHisteresysOption() {
        return (int)(100F*histeresysOption);
    }

    public static void setInverseOption(boolean option) {
        inverseOption = option;
    }

    public static boolean getInverseOption() {
        return inverseOption;
    }

    public static void setFilterOption(boolean option) {
        filterOption = option;
    }

    public static boolean getFilterOption() {
        return filterOption;
    }

    // ====================== Audio File data ==============================
    private AudioInputStream audioIn; // Audio inpurt stream
    private boolean eof;              // End of File Flag
    private byte[] dataFrame;         // Buffer to read a single audio frame

    private float sampleRate;    // Sample Rate in Hz
    private int channels;        // 1 (mono) or 2 (stereo)
    private int bitsPerSample;   // 8, 16, 24 or 32 bits
    private int bytesPerSample;  // 1 (8 bits), 2 (16 bits), 3 (24 bits) or 4 (32 bits)
    private int dataFrameSize;   // bytes per sample * number of channels
    private float levelScale;    // Sample scaler to 1.0

    /**
     * Empty Constructor
     */
    public JatmWaveLoad() {

    }

    /**
     * Open Audio input Stream
     * @param filename
     * @return true if an error occur
     */
    public boolean open(String filename) {
        eof = true; // File not Openned. Flag EOF
        try {  // Open Audio input stream
            audioIn = AudioSystem.getAudioInputStream(new File(filename));
        } catch (UnsupportedAudioFileException | IOException ex) {
            System.out.println("ERROR: Load WAV file - open failed");
            return true; // Flag a problem
        }

        if(audioIn == null) {
            System.out.println("ERROR: Load WAV file - null stream");
            return true; // Flag a problem
        }

        // Get Audio File Format
        channels = audioIn.getFormat().getChannels();
        bitsPerSample = audioIn.getFormat().getSampleSizeInBits();
        sampleRate = audioIn.getFormat().getSampleRate();
        bytesPerSample = bitsPerSample / 8;
        dataFrameSize = audioIn.getFormat().getFrameSize();

        // Set Time scale based on Audio Sample Rate
        float timeScale  = (float)Z80_CLOCK / sampleRate;
        pulseTolerance  = (int)(0.5F+(float)ERROR_T / timeScale);
        pilotPulse = (int)(0.5F+(float)PILOT_PULSE_T / timeScale);
        syncPulse  = (int)(0.5F+(float)SYNC_T  / timeScale);
        bit0Pulse  = (int)(0.5F+(float)BIT0_T  / timeScale);
        bit1Pulse  = (int)(0.5F+(float)BIT1_T  / timeScale);

        // Set Level Scale based on sample size in bits;
        switch(bitsPerSample) {
            case 8:  levelScale = 127F; break;
            case 16: levelScale = 32767F; break;
            case 24: levelScale = 8388.607e3F; break;
            case 32: levelScale = 2147.483647e6F; break;
            default: levelScale = 32767F; break;
        }

        if (dataFrameSize < 0) { // Check for some weird error that may occur
            dataFrameSize = channels * bytesPerSample;
        }

        dataFrame = new byte[dataFrameSize]; // Allocate Buffer for a single sample frame
        if(dataFrame == null) {
            System.out.println("ERROR: Load WAV file - null frame buffer");
            return true; // error allocationg frame buffer
        }

        // Create FIR filter
        int filterLength=3;
        if(sampleRate > 20000F) {
            filterLength = 5;
        }
        if(sampleRate > 40000F) {
            filterLength = 7;
        }
        firFilter = new JatmFirFilter(filterLength);

        // File open successfull
        eof = false;
        return false;
    }

    /**
     * Close Audio input Stream
     */
    public void close() {
        eof = true; // End of file
        if(audioIn == null) { // no stream to close!
            return;
        }
        try {
            audioIn.close();
        } catch (IOException ex) {
            System.out.println("ERROR: Load WAV file - close error");
        }
    }

    /**
     * Load one Tape file from Audio stream current position
     * @return a tape file or null if no file found
     */
    public JaTape load() {
        JaTape tape;
        byte[] headerBuffer;
        byte[] dataBuffer;
        JaTapeBlock headerBlock;

        if(eof || (audioIn == null)) { // EOF or No stream to load from
            return null;
        }

        // Load Header Block
        headerBuffer = new byte[27];
        if (loadBlock(headerBuffer) == 27) { // header block load

            headerBlock = new JaTapeBlock(headerBuffer); // convert byte

            // Load Data Block
            int dataSize = headerBlock.getWord(JaTape.LENGTH)+2; // get expected data block size
            if( dataSize > 2) {// +2 due to additional BlockType and CRC bytes
                dataBuffer = new byte[dataSize];
                if(loadBlock(dataBuffer) > 0) { // data block loaded correctly
                    tape = new JaTape(headerBuffer, dataBuffer); // create a new tape
                } else {
                    tape = null; // invalid data block
                }
            } else {
                tape = null; // invalid data bloco size
            }
        } else {
            tape = null; // invalid header block
        }

        return tape;
    }

    //====================== Private Methods ============================

    /**
     * search and load a tape file block
     * @param block byte array to hold the loaded block bytes
     * @return number of bytes loaded
     */
    private int loadBlock(byte[] block) {
        int bitCount = 0;
        int byteCount = 0; // count bytes loaded
        int data = 0;
        int pulse;

        LoadState state = LoadState.SEARCHING;
        do {
            pulse = getPulseWidth();
            if (eof) { // EOF check
                state = LoadState.DONE;
            }
            switch (state) {
                case SEARCHING:
                    if (isPulse(pulse, pilotPulse)) { // Pilot Found!
                        state = LoadState.PILOTING;
                    } // else keep "Searching"
                    break;
                case PILOTING:
                    if (!isPulse(pulse, pilotPulse)) {   // Not a pilot tone..
                        if (isPulse(pulse, syncPulse)) { // May be a sync?!
                            state = LoadState.LOADING;   // Yes, Start Loading!
                        } else { // ... No, back searching
                            state = LoadState.SEARCHING;
                            byteCount = 0; // Reset data index
                        }
                    } // else keep "Piloting"
                    break;
                case LOADING:
                    if (bitCount == 0) { // first bit in a new byte
                        data = 0;
                    }
                    data <<= 1; // rotate bits left to add new bit
                    if (isPulse(pulse, bit0Pulse)) { // is it a 0 bit ?
                        bitCount++;
                    } else {
                        if(isPulse(pulse, bit1Pulse)) { // is it a 1 bit ?
                            data++; // set bit
                            bitCount++;
                        } else { // unespected or sync pulses: end
                            state = LoadState.DONE;
                            break;
                        }
                    }
                    if(bitCount >= 8) { // single Byte Load complete
                        bitCount = 0; // prepare for next byte
                        if(byteCount < block.length) { // Check block room
                            block[byteCount] = (byte)(data & 0xFF); // Save loaded byte
                            byteCount++; // count loaded bytes
                        } else { // Buffer full: End
                            state = LoadState.DONE;
                        }
                    }
                    break;
                default:
                    state = LoadState.DONE; // Just in case....
            } // Switch statement
        } while( state != LoadState.DONE );
        return byteCount;
    }

    /**
     * Identify Pulse
     * @param ref Reference Pulse Width
     * @param width Pulse Width to identify
     * @return true if matched, false otherwise
     */
    private boolean isPulse(int width, int ref) {
        if(width == 0) {
            return false;
        }
        if (width < (ref - pulseTolerance)) { // Too short pulse!
            return false;
        }
        return width <= (ref + pulseTolerance);
    }


    /**
     * Get next Pulse Width
     * @return pulse width in audio sample count. zero if no pulse found
     *   _____________               _
     *  |             |             |
     *  |             |             |
     * _|             |_____________|
     *  |<---width--->|
     */
    private int getPulseWidth() {
        int width = 0; // Pulse Width in audio sample counts

        while(detectLevel(getSample()) < 1) { // Seek for a Pulse Rising Edge
            if (eof) { // check for EOF
                return 0;
            }
        }
        width++; // Pulse Rising Edge found! Start Counting pulse width

        while(detectLevel(getSample()) > -1) { // Count pulse width until a falling edge
            if (eof) { // check for EOF
                return 0;
            }
            width++;
        }

        return width; // return measured pulse width
    }

    /**
     * Detect Sample Level region
     * @param sampleLevel loaded audio level (-1.0 to 1.0)
     * @return 1: high, -1: low, 0: histeresys region
     *    1 = HIGH level region
     * ..................................... (level + histeresys)
     *    0 = upper histeresys region
     * ------------------------------------- (level)
     *    0 = lower histeresys region
     * ..................................... (level - histeresys)
     *   -1 = LOW level region
     */
    private int detectLevel(float sampleLevel) {
        int region = 0;   // assume level at histeresys region at first
        if (sampleLevel > levelOption + histeresysOption) { // check if at High level region
            region = 1;   // level at HIGH level region
        }
        if (sampleLevel < levelOption - histeresysOption) { // check if at the low level region
            region = -1;  // level at LOW level region
        }
        if (inverseOption) {                                // Check user option "Inverse"
            region *= -1; // invert result signal
        }
        return region;
    }

    /**
     * Get a Sample from Audio stream. when EOF Sets global variable eof=true
     * @return sample value in range -1.0 to 1.0
     * @throws IOException
     */
    private float getSample() {
        float result, leftSample, rightSample;

        // Read sample Frame
        int nBytes;
        try {
            nBytes = audioIn.read(dataFrame); // return -1 if EOF
            if(nBytes < dataFrameSize) { // Detect EOF (end of file)
                eof = true; // Flag EOF
                return 0F;
            }
        } catch (IOException ex) {
            eof = true; // Flag EOF
            System.out.println("Read Sample Frame error");
            return 0F;
        }

        // Convert Left Channel Sample
	leftSample = extractSample(0);
        // Convert Right Channel Sample
	if(channels == 1) {
            rightSample = leftSample; // Mono: Right = Left
	} else {
            rightSample = extractSample(bytesPerSample); // Stereo: Convert Right Channel
        }

        switch(channelOption) {
            case 1: // Left Channel Only
                result = leftSample;
                break;
            case 2: // Right Channel Only
                result = rightSample;
                break;
            default: // Average Left and Right Channels
                result = (leftSample + rightSample) / 2F;
        }
        if(filterOption) {
            result = firFilter.filter(result);
        }
        return result;
    }

    /**
     * Convert a Sample in dataFrame buffer to a normalized Float Number from -1.0 to +1.0
     * @param index Sample buffer index
     * @return sample value as -1.0 to 1.0
     */
    private float extractSample(int index) {
	long result = 0;

        if(bytesPerSample==1) { // 8 bit unsigned
            result = 0xFF & (long)dataFrame[index]; // circunvent java sign extension on convertion
            result -= 128; // apply 8-bit data offset
        } else {
            for(int i = bytesPerSample-1; i >= 0 ; i--) {
                result <<= 8;                   // make room for next byte
                result |= dataFrame[index+i];   // insert byte
            }
        }

	return (float)result / levelScale; // result scaled to -1.0 to 1.0
    }

}
