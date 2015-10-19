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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.List;

public final class JatmFileWav extends JatmFile {

    /** Jupiter Ace Tape File Parameters **/
    private static final int PILOT_PULSES = 8192; // Number of Pilot Pulses
    private static final int PILOT_PULSE_LEN = 2011; // One Pilot Pulse width in Z80 cycles
    private static final int SYNC1_PULSE_LEN = 600; // First Sync Pulse Length in Z80 cycles
    private static final int SYNC2_PULSE_LEN = 790; // Second Sync Pulse Length in Z80 cycles 
    private static final int BIT0_LEN = 801; // Bit 0 Pulse Length in Z80 cycles
    private static final int BIT1_LEN = 1591; // Bit 1 Pulse Length in Z80 cycles
    private static final int PAUSE_LEN = 3000; // Pause Length in Z80 cycles
    private static final int Z80_CLOCK = 3250000;
    
    /** Save WAV file parameters **/
    public static int saveSampleRate;    // 22050, 44100, 48000 Hz
    public static int saveBitsPerSample; // 8, 16, 24 bits
    public static  Boolean saveLeftChannel; // Left Channel Usage
    public static  Boolean saveRightChannel; // Right Channel Usage
    public static  int saveVolume;  // 0-100%
    
    // Calculated Save Parameters
    private int saveChannels; // 1=mono, 2=stereo
    private long saveHiLevel; // calculated from saveVolume
    private long saveLoLevel; // calculated from saveVolume
    
    private RandomAccessFile file;    
    
    public JatmFileWav() {
        extension = "wav";
        description = "Wav files (*.wav)";
        setSaveDefault(); // Default Save Parameters
    }
    
    // Set Save Parameters to Default values
    public static void setSaveDefault() {
        saveSampleRate = 44100; // Sample Rate
        saveBitsPerSample = 16; // bits
        saveLeftChannel = true; // Both Channels
        saveRightChannel = true;
        saveVolume = 90;
    }

    // Calculate Min & Max levels from Volume & sample size values
    private void calcSaveLevels() {
        long v = Math.round(Math.scalb(saveVolume/100.0, (saveBitsPerSample-1)));
        if(saveBitsPerSample <= 8 ) { // 8 bits unsigned
            saveHiLevel = Math.max(v+127,255);
            saveLoLevel = Math.min(v+127,0);
        } else { // 16 or 24 bits signed
            saveHiLevel = v;
            saveLoLevel = -saveHiLevel+1;
        }
    }
    
    @Override
    public int save(Path filePath, List<JaTape> list, int[] selection) {
        if(selection.length <= 0) {
            return -1;  // Error: No selection
        }
        
        int blockAlign = (saveChannels * saveBitsPerSample)/8;
        int byteRate = saveSampleRate * blockAlign;

        try {
            file = new RandomAccessFile(new File(filePath.toString()),"rw");
        } catch (FileNotFoundException ex) {
            return -1;  // Error: Cannot create file
        }
        
        // Write WAV header
        try {
            file.writeBytes("RIFF");                            // 00: 4 bytes, Chunk ID
            file.write(toLittleEndian((long)0));                // 04: 4 bytes, Chunk Size = 36 + SubChunk2Size
            file.writeBytes("WAVE");                            // 08: 4 bytes, File Format
            file.writeBytes("fmt ");                            // 12: 4 bytes, SubChunk1 ID
            file.write(toLittleEndian((long)16));               // 16: 4 bytes, SubChunk1 Size
            file.write(toLittleEndian(1));                      // 20: 2 bytes, Audio Format (PCM)
            file.write(toLittleEndian(saveChannels));           // 22: 2 bytes, Num Channels
            file.write(toLittleEndian((long)saveSampleRate));   // 24: 4 bytes, Sample Rate
            file.write(toLittleEndian((long)byteRate));         // 28: 4 bytes, Byte Rate
            file.write(toLittleEndian(blockAlign));             // 32: 2 bytes, Block Align
            file.write(toLittleEndian(saveBitsPerSample));      // 34: 2 bytes, Bits per Sample
            file.writeBytes("data");                            // 36: 4 bytes, SubChunk2 ID
            file.write(toLittleEndian((long)0), 0, 4);          // 40: 4 bytes, SubChunk2 Size
        } catch  (IOException ex) {
            return -1; // Error writing to file
        }
        
        long subChunk2Size = 0; // count data written 
        calcSaveLevels();
        
        JaTape tapeFile;
        byte[] header;
        byte[] data;        
        for(int i=0; i < selection.length; i++) {
            tapeFile = list.get(selection[i]);
            header = tapeFile.getHeaderBlock();
            data = tapeFile.getDataBlock();
            
//            try {
//                saveSilence(PAUSE_LEN);  // Leading Gap
//                savePilot(PILOT_PULSES); // Pilot Tone
                // *** Save Header block
//                saveSilence(PAUSE_LEN); // Header/Data separation Gap
                // *** Save Data block
//                saveSilence(PAUSE_LEN); // Trailling Silence
                
//                out.writeByte(loByte(header.length-1));
//                out.writeByte(hiByte(header.length-1));
//                out.write(header, 1, header.length-1);
//                out.writeByte(loByte(data.length-1));
//                out.writeByte(hiByte(data.length-1));
//                out.write(data, 1, data.length-1);
//            } catch  (IOException ex) {
//                return -1; // Error writing to file
//            }
            
        }
        
        // update & close file
        try {
                    
            file.seek(4);       // update Chunk Size
            file.write(toLittleEndian(subChunk2Size+36));
            file.seek(40);      // update subChunk2 Size
            file.write(toLittleEndian(subChunk2Size));
            file.close();       // Close File
        } catch (IOException ex) {
            return -1;
        }   
        
        return 1;   // One file saved
    }

    /** Convert int to 2 byte array */
    private byte[] toLittleEndian(int x)
    {
        byte[] result = new byte[2];
        result[0] = (byte)(x & 0xFF);
        x >>>= 8;
        result[1] = (byte)(x & 0xFF);
        return result;
    }
    
    /** Convert long to 4 byte array */
    private byte[] toLittleEndian(long x)
    {
        byte[] result = new byte[4];
        result[0] = (byte)(x & 0xFF);
        x >>>= 8;
        result[1] = (byte)(x & 0xFF);
        x >>>= 8;
        result[2] = (byte)(x & 0xFF);
        x >>>= 8;
        result[3] = (byte)(x & 0xFF);
        return result;
    }


    private void saveSilence(int cycles) {
        
    }
    
    private void savePilot(int pulses) {
        
    }
    
    private void saveData(byte[] b) {
        // file.write
    }
    
    //**** LOAD ***************************************************************  
    
    @Override
    public int load(Path filePath, List<JaTape> list) {
        return 0;
    }


}
