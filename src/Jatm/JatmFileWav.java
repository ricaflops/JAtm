/*
 * JatmFileWav - Wav file format for Jatm
 *
 * This file is part of JAtm - The Jupiter Ace tape manager.
 *
 * JAtm is a tool to manage Jupiter Ace tape files in several formats.
 * Copyright (C) 2015  Ricardo Fernandes Lopes
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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * .WAV Wave file format for JAtm
 * @author Ricardo
 */
public final class JatmFileWav extends JatmFile {
    
    private static final int Z80_CLOCK = 3250000; // CPU Clock Frequency   
    private static final int PILOT_CYCLES = 512; // Pilot Tone Cycles    
     
    // Pulse Length in Z80 cycles
    private static final int PILOT_PULSE_T    = 2011; // Pilot pulse width
    private static final int SYNC_PULSE_HI_T  = 601;  // Sync hi pulse width
    private static final int SYNC_PULSE_LO_T  = 791;  // Sync lo pulse width 
    private static final int BIT0_PULSE_HI_T  = 795;  // Bit 0 hi pulse width
    private static final int BIT0_PULSE_LO_T  = 801;  // Bit 0 lo pulse width
    private static final int BIT1_PULSE_HI_T  = 1585; // Bit 1 hi pulse width
    private static final int BIT1_PULSE_LO_T  = 1591; // Bit 1 lo pulse width
    private static final int MARK_PULSE_HI_T = 903;  // End Mark hi pulse width
    private static final int MARK_PULSE_LO_T = 4187; // End Mark lo pulse width
    private static final int PAUSE_T         = 6500000; // Leading silence
    private static final int ERROR_T         = 200; // Error tolerance
    // Complete cycle length in Z80 cycles
    private static final int PILOT_T = 2 * PILOT_PULSE_T;
    private static final int SYNC_T  = SYNC_PULSE_HI_T + SYNC_PULSE_LO_T; // Sync
    private static final int BIT0_T  = BIT0_PULSE_HI_T + BIT0_PULSE_LO_T; // Bit 0
    private static final int BIT1_T  = BIT1_PULSE_HI_T + BIT1_PULSE_LO_T; // Bit 1    

    
    // Save Parameters
    private RandomAccessFile file;
    
    private static int saveBits;  // WAV save fixed 16 bits/sample
    private static int saveSampleRate; // WAV file Save Sample Rate
    private static int saveChannels;   // WAV file Save Stereo
    private static int saveVolume;     // WAV file Save Volume: 0-100

    private int hiLevel;    // calculated from saveVolume
    private int loLevel;    // calculated from saveVolume
    private byte[] silence; // Leading silence
    private byte[] pilotCycle;   // pilotCycle tone   
    private byte[] syncCycle;    // Sync Pulses
    private byte[] bit0Cycle;    // Bit 0 wave
    private byte[] bit1Cycle;    // Bit 1 wave
    private byte[] endMark; // End Mark pulses    
  

    public JatmFileWav() {
        extension = "wav";
        description = "Wav files (*.wav)";
        setSaveDefault(); // Default Save Parameters
    }
    
    /**
     * convert Z80 cycles to WAV samples
     * @param wavSampleRate
     * @param z80cycles
     * @return 
     */
    private int cycles2Samples(long wavSampleRate, int z80cycles) {
        long samples;
        samples = ((long)z80cycles * wavSampleRate) / Z80_CLOCK;
        return (int)samples;
    }

//===== LOAD ==================================================================  
   
    @Override
    public int load(Path filePath, List<JaTape> list) {
        JatmWaveLoad audioIn = new JatmWaveLoad(); // Load Audio
        int tapeCount = 0; // Count number of Jupiter Ace tape files found
        JaTape tape;
        
        // Open Audio input stream
        try {
            audioIn.open(filePath.toString());
        } catch (IOException | UnsupportedAudioFileException ex) {
            tapeCount = 0; // Abort if file error
        }
        
        // Loop loading tape files until EOF
        do {
            tape = audioIn.load();
            if (tape != null) {
               list.add( tape );    // Add tape to tape list
                tapeCount++; 
            }
        } while(tape != null);
        
        // Close Audio input stream
        try {
            audioIn.close();
        } catch (IOException ex) {
            Logger.getLogger(JatmFileWav.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tapeCount;        
    }
    
//===== SAVE ==================================================================
     
    public static void setSaveBits(int bits) {
        saveBits = bits;
    }
    public static int getSaveBits() {
        return saveBits;
    }
    
    public static void setSaveSampleRate(int sr) {
        saveSampleRate = sr;
    }
    public static int getSaveSampleRate() {
        return saveSampleRate;
    }
    
    public static void setSaveStereo(boolean stereo) {
        if(stereo) {
        saveChannels = 2;
        } else {
            saveChannels = 1;
        }    
    }
    public static boolean isSaveStereo() {
        return (saveChannels == 2);
    }
    
    public static void setSaveVolume(int vol) {
        saveVolume = vol;
    }
    public static int getSaveVolume() {
        return saveVolume;
    }
    
    /** Convert int to 2 byte array, little endian */
    private byte[] int2byteArray(int x) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte)(x & 0xFF); // Low Byte
        x >>>= 8;
        byteArray[1] = (byte)(x & 0xFF); // Hight Byte
        return byteArray;
    }
    
    /** Convert long to 4 byte array, little endian */
    private byte[] long2byteArray(long x) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(x & 0xFF); // Lowest significant byte
        x >>>= 8;
        byteArray[1] = (byte)(x & 0xFF);
        x >>>= 8;
        byteArray[2] = (byte)(x & 0xFF);
        x >>>= 8;
        byteArray[3] = (byte)(x & 0xFF); // Highest significant byte
        return byteArray;
    }
   
    /**
     * Set Save Parameters to Default values
     */
    public static void setSaveDefault() {
        saveSampleRate = 44100;  // CD quality Sample Rate
        saveBits = 16;
        saveChannels = 2;
        saveVolume = 90;         // 90% Volume Level
    }

    /**
     * Initialize parameters to save WAV file
     */
    private void initSave() {
        // Calculate Levels
        saveVolume = (saveVolume>100)?100:saveVolume;
        saveVolume = (saveVolume<0)?0:saveVolume;
        long v = Math.round(Math.scalb(saveVolume/100.0, saveBits-1));
        hiLevel = (int) v;
        loLevel = (int) (1-v);

        // create Silence sample block
        silence = createSilence(PAUSE_T);
        // Create Pilot tone cycle
        pilotCycle = createCycle(PILOT_PULSE_T, PILOT_PULSE_T);
        // Create Sync hi>lo Pulses
        syncCycle = createCycle(SYNC_PULSE_HI_T, SYNC_PULSE_LO_T);
        // Create Bit 0 hi>lo pulses
        bit0Cycle = createCycle(BIT0_PULSE_HI_T, BIT0_PULSE_LO_T);
        // Create Bit 1 hi>lo pulses
        bit1Cycle = createCycle(BIT1_PULSE_HI_T, BIT1_PULSE_LO_T);
        // Create End Mark Pulse
        endMark = createCycle(MARK_PULSE_HI_T, MARK_PULSE_LO_T);
    }
    
    /**
     * Create a wav silence array (level=0)
     * @param cycles silence length in Z80 cycles
     * @return WAV silence array
     */
    private byte[] createSilence(int cycles) {
        int samples = cycles2Samples(saveSampleRate, cycles);
        int len = samples*saveChannels*(saveBits/8);
        byte[] array = new byte[len];
        Arrays.fill( array, (byte) 0 );
        return array;
    }
    
    /**
     * create a wave cycle array: hi>lo
     * @param hiWidth cycle hi level
     * @param loWidth cycle low level
     * @return WAV cycle array
     */
    private byte[] createCycle(int hiWidth, int loWidth ) {
        int hiSamples = cycles2Samples(saveSampleRate, hiWidth);
        int loSamples = cycles2Samples(saveSampleRate, loWidth);
        int len = (hiSamples+loSamples)*saveChannels*(saveBits/8);    
        byte[] array = new byte[len];
        int index = 0;
        for(int i = 0; i < hiSamples; i++) {
            index = setSample(array, index, hiLevel);                
        }
        for(int i = 0; i < loSamples; i++) {
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
    private int setSample(byte[] array, int index, int level) {
        byte loByte  = (byte)(level & 0xFF);      // Low byte
        byte hiByte = (byte)((level>>8) & 0xFF); // High byte
        array[index++] = loByte; array[index++] = hiByte;     // Mono or Left Channel
        if(saveChannels == 1) {
            array[index++] = loByte; array[index++] = hiByte; // Right Channel when stereo
        }
        return index;
    }

    private long saveByte(byte b) throws IOException {
        long total = 0;
        for(int i=0; i<8; i++) { // write 8 bits, hi bit first
            if((b & 0x80) != 0) {
                total += writeArray(bit1Cycle); // bit 1
            } else {
                total += writeArray(bit0Cycle); // bit 0
            }
            b <<= 1; // rotate next bit into position
        }
        return total;
    }
    
    /**
     * save tape file block
     * @param block block array to save
     * @return number of bytes saved
     * @throws IOException 
     */
    private long saveBlock(byte[] block)  throws IOException {
        long byteCount = 0; // byte counter
        byteCount += writeArray(syncCycle);     // Write Sync pulses
        for(int i = 0; i < block.length; i++) { // Write block bytes
            byteCount += saveByte(block[i]);
        }
        byteCount += writeArray(endMark);       // Write End Mark
        return byteCount; // return WAV file bytes writen
    }
    
    /**
     * write a byte array in WAV file
     * @param byte array to write
     * @return bytes writen
     * @throws IOException 
     */
    private long writeArray(byte[] byteArray) throws IOException {
        file.write(byteArray);
        return byteArray.length;
    }

    @Override
    public int save(Path filePath, List<JaTape> list, int[] selection) {
        if(selection.length <= 0) {
            return -1;  // Error: No selection
        }
         // Adjust WAV save parameters
        initSave();
        long chunkSize = 32;
        long subChunk1Size = 16;
        long subChunk2Size = 0;  // count data written
        long blockAlign = (saveChannels * saveBits)/8;
        long byteRate = saveSampleRate * blockAlign;
        try {
            file = new RandomAccessFile(new File(filePath.toString()),"rw");
            // Write WAV file header
            file.writeBytes("RIFF");                         // 00: 4 bytes, Chunk ID
            file.write(long2byteArray(chunkSize));           // 04: 4 bytes, Chunk Size = SubChunk2Size + 36
            file.writeBytes("WAVE");                         // 08: 4 bytes, File Format
            file.writeBytes("fmt ");                         // 12: 4 bytes, SubChunk1 ID
            file.write(long2byteArray(subChunk1Size));       // 16: 4 bytes, SubChunk1 Size
            file.write(int2byteArray(1));                   // 20: 2 bytes, Audio Format (PCM)
            file.write(int2byteArray(saveChannels));            // 22: 2 bytes, Num Channels
            file.write(long2byteArray(saveSampleRate));      // 24: 4 bytes, Sample Rate
            file.write(long2byteArray(byteRate));            // 28: 4 bytes, Byte Rate
            file.write(int2byteArray((int)blockAlign));          // 32: 2 bytes, Block Align
            file.write(int2byteArray(saveBits));           // 34: 2 bytes, Bits per Sample
            file.writeBytes("data");                         // 36: 4 bytes, SubChunk2 ID
            file.write(long2byteArray(subChunk2Size), 0, 4); // 40: 4 bytes, SubChunk2 Size
            
            // Save each selected file in sequence
            JaTape tapeFile;       
            for(int i=0; i < selection.length; i++) {
                tapeFile = list.get(selection[i]);  // Get next file to save
                subChunk2Size += writeArray(silence);      // Write Leading Silence
                for(int j = 0; j < 8*PILOT_CYCLES; j++ ) { // Write Header Pilot
                    subChunk2Size += writeArray(pilotCycle);             
                }
                subChunk2Size += saveBlock(tapeFile.getHeaderBlock()); // Write Header Block
                for(int j = 0; j < PILOT_CYCLES; j++ ) {  // Write Data Pilot
                    subChunk2Size += writeArray(pilotCycle);                
                }
                subChunk2Size += saveBlock(tapeFile.getDataBlock()); // Write Data Block
            } 
            subChunk2Size += writeArray(silence); // Write Trailing Silence         
            // update WAV file chunk size parameters and close it   
            file.seek(4);  file.write(long2byteArray(subChunk2Size+36)); // update Chunk Size
            file.seek(40); file.write(long2byteArray(subChunk2Size)); // update subChunk2 Size
            file.close();       // Close File
        } catch (IOException ex) {
            Logger.getLogger(JatmFileWav.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1;   // One WAV file saved
    }

}
