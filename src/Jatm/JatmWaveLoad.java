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
    private static final int SYNC_PULSE_HI_T  = 601;  // Sync hi pulse width
    private static final int SYNC_PULSE_LO_T  = 791;  // Sync lo pulse width 
    private static final int BIT0_PULSE_HI_T  = 795;  // Bit 0 hi pulse width
    private static final int BIT0_PULSE_LO_T  = 801;  // Bit 0 lo pulse width
    private static final int BIT1_PULSE_HI_T  = 1585; // Bit 1 hi pulse width
    private static final int BIT1_PULSE_LO_T  = 1591; // Bit 1 lo pulse width
    private static final int ERROR_T         = 200; // Error tolerance
    
    // Cycle lengths in Z80 cycles
    private static final int PILOT_T = 2 * PILOT_PULSE_T;
    private static final int SYNC_T  = SYNC_PULSE_HI_T + SYNC_PULSE_LO_T; // Sync
    private static final int BIT0_T  = BIT0_PULSE_HI_T + BIT0_PULSE_LO_T; // Bit 0
    private static final int BIT1_T  = BIT1_PULSE_HI_T + BIT1_PULSE_LO_T; // Bit 1
    
    // Scaled Time width
    private float timeFactor;  
    private int errorLimit; // cycle width error limit in audio samples 
    private int pilot_width;
    private int sync_width;
    private int bit0_width;
    private int bit1_width;
      
    private static enum LoadState {
        SEARCHING,
        PILOTING,
        LOADING,
        DONE
    }
    
    // ====================== User Options ==============================
    private static int channelOption;
    private static float thresholdOption;
    static { // Initialize Default option
        channelOption = 0; // Mix Channels
        thresholdOption = 0F;
    }
    // ====================== Audio Format ==============================
    private AudioInputStream inStream;
    private byte[] inBuffer;    // Buffer to read a single audio sample
    private int inChannels;     // 1 (mono) or 2 (stereo)
    private int inSampleBytes;  // 8, 16, 24 or 32 bits
    private float inSampleRate; // Sample Rate
    private long inSamples;    // Total number of samples in file
    private float sampleScale; // Sample scale to 1.0
    private int inBytesPerFrame;

    private int inSampleSize;    


    /**
     * Constructor
     */
    public JatmWaveLoad() {

    }
    
    /**
     * Open Audio input Stream
     * @param filename
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public void open(String filename) throws IOException, UnsupportedAudioFileException {
        // Open Audio input stream
        inStream = AudioSystem.getAudioInputStream(new File(filename));
        // Get Audio Format Parameters
        inChannels = inStream.getFormat().getChannels();  // 1=mono or 2=stereo
        inSampleSize = inStream.getFormat().getSampleSizeInBits(); // Sample Size in Bits
        inSampleRate = inStream.getFormat().getSampleRate(); // Sample Rate in Hz
        inSamples =  inStream.getFrameLength();
        inSampleBytes = inSampleSize/8; // Sample size in Bytes
        sampleScale = (float) (1 << (inSampleSize-1)); // Sample Scale to 1.0
        inBytesPerFrame = inStream.getFormat().getFrameSize();
        if (inBytesPerFrame == AudioSystem.NOT_SPECIFIED) {
            inBytesPerFrame = 1;
        }

        inBuffer = new byte[inBytesPerFrame]; // Allocate Buffer for a single sample
        
        // Scale Cycles width
        timeFactor = (float)Z80_CLOCK / inSampleRate;        
        errorLimit = (int)((float)ERROR_T / timeFactor); // Cycle width error allowed        
        pilot_width =(int)((float)PILOT_T / timeFactor);
        sync_width =(int)((float)SYNC_T / timeFactor);
        bit0_width =(int)((float)BIT0_T / timeFactor);
        bit1_width =(int)((float)BIT1_T / timeFactor);
    }
    
    /**
     * Close Audio input Stream
     * @throws IOException 
     */
    public void close() throws IOException {
        inStream.close();
    }
    
    /**
     * Load one Tape file from Audio stream current position
     * @return a tape file or null if no file found
     */
    public JaTape load() {
        JaTape tape;
        JaTapeBlock headerBlock;
        // Load Header Block
        byte[] headerBuffer = new byte[27]; // Headers shall be 27 bytes exactly !
        if (loadBlock(headerBuffer) == 27) { // header block load
            headerBlock = new JaTapeBlock(headerBuffer);
            
            // Load Data Block
            int dataSize = headerBlock.getWord(JaTape.LENGTH)+2; // get expected data block size
            if( dataSize > 2) {// +2 due to additional BlockType and CRC bytes
                byte[] dataBlock = new byte[dataSize]; 
                if(loadBlock(dataBlock) > 0) { // data block loaded correctly
                    tape = new JaTape(headerBuffer, dataBlock); // create a new tape
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
    
    /**
     * Set Channel to use, Left, Right, Left+Right
     * @param option 0=Mix, 1=Left, 2=Right
     */
    public void setChannelOption(int option) {
        if (option < 0 || option > 2) {
            option = 0;
        }
        channelOption = option;
    }
    
    /**
     * Get Channel Use Option
     * @return  0=Mix, 1=Left, 2=Right
     */
    public int getChannelOption() {
        return channelOption;
    } 

    /**
     * Set Threshold Level from -100% to 100%
     * @param option -100 to 100
     */
    public void setThresholdOption(int option) {
        thresholdOption = (float)option / 100F;
        
        if(thresholdOption > 1F) {
            thresholdOption = 1F;
        }
        
        if(thresholdOption < -1F) {
            thresholdOption = -1F;
        }        
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
        int cycle;
        
        LoadState state = LoadState.SEARCHING;        
        do {
            cycle = getCycle();
            if (cycle < 0) { // EOF check
                state = LoadState.DONE;
                byteCount = -1; // <-- flag EOF
            }
            switch (state) {
                case SEARCHING:
                    if (isCycle(cycle,pilot_width)) { // Pilot Found!
                        state = LoadState.PILOTING;
                    }
                    break;
                case PILOTING:   
                    if (!isCycle(cycle,pilot_width)) {   // Not a pilot tone..
                        if (isCycle(cycle,sync_width)) { // May be a sync?!
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
                    if (isCycle(cycle, bit0_width)) { // is it a 0 bit ?
                        bitCount++;
                    } else {
                        if(isCycle(cycle,bit1_width)) { // is it a 1 bit ?
                            data++; // set bit
                            bitCount++;
                        } else { // unespected or sync pulses, end
                            state = LoadState.DONE;
                        }
                    }
                    if(bitCount >= 8) { // single Byte Load complete
                        bitCount = 0; // prepare for next byte
                        if(byteCount < block.length) { // Check block room
                            block[byteCount] = (byte)data; // Save loaded byte
                            byteCount++; // count loaded bytes
                        } else {
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
     * get a Sample from stream
     * @return sample value in range -1.0 to 1.0 , return -100.0 if file error or EOF
     * @throws IOException 
     */
    private float getSample() {
        float result, leftSample, rightSample;
        
        // Read sample block
        int nBytes; 
        try {
            nBytes = inStream.read(inBuffer); // return -1 if EOF
        } catch (IOException ex) {
            return -1000F; // File error
        }
        if(nBytes < inBytesPerFrame) { // check for EOF
            return -100F;
        }
        // Convert Left Channel Sample
	leftSample = convertSample(0);
        // Convert Right Channel Sample
	if(inChannels == 1) { 
            rightSample = leftSample; // Mono: Right = Left 
	} else {
            rightSample = convertSample(inSampleBytes); // Stereo: Convert Right Channel          
        }
        
        switch(channelOption) {
            case 1: // Left Channel Only
                result = leftSample;
                break;
            case 2: // Right Channel Only
                result = rightSample;
                break;
            default: // Mix Left and Right Channels
                result = (leftSample + rightSample) /2F;
        }
        return result;
    }
    
    /**
     * Convert a Sample in buffer to a normalized Float Number from -1.0 to 1.0
     * @param index Sample buffer index
     * @return sample value as -1.0 to 1.0
     */
    private float convertSample(int index) {
	int result = 0;
        for(int i=index; i<inSampleBytes+index; i++) {
            result <<= 8; // move byte higher
            result |= (int)inBuffer[i];
        }
        if(inSampleBytes==1) { // 8-bit sample offset
            result ^= 0x80;
        }
	return (float)result / sampleScale; // result scaleed to -1.0 to 1.0
    }
    
    /**
     * Get next Wave Cycle Width
     * @return pulse width in samples
     */
    private int getCycle() {
        int width = 0; // Cyle Width in audio sample counts
        float level; // Current sample level
        
        // Measure Hight Level semi-cycle
        do {
            width++;
            level = getSample();
            if(level<-10.0) { // EOF or file read error
                return -1; // Flag File problem or EOF
            }
        } while(level >= thresholdOption); //***** change to check against user treshold level
        
        // Measure Low Level semi-cycle
        do {
            width++;
            level = getSample();
            if(level<-10.0) { // EOF or file read error
                return -1; // Flag File problem or EOF
            }
        } while(level < thresholdOption); //***** change to check against user treshold level
        
        return width;
    }
    
    /**
     * Identify a Wave Cycle
     * @param ref Reference Cycle Width
     * @param width Cycle Width to identify
     * @return true if matched, false otherwise
     */
    private boolean isCycle(int ref, int width) {
        if (width < ref - errorLimit) {
            return false;
        }
        if ( width > ref + errorLimit ) {
            return false;
        }
        return true;
    }

}
