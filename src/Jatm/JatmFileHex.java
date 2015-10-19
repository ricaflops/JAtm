/*
 * JatmFileHex - Intel Hex file format for Jatm
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;

public class JatmFileHex extends JatmFileBin {
    public JatmFileHex() {
        extension = "hex";
        description = "Intel Hex files (*.hex)";        
    }

    @Override
    public int load(Path filePath, List<JaTape> list) {
        String line;
        int recordLength;
        int address;
        int startAddress = -1;
        int dataIndex = 0;
        byte[] buf = new byte[64*1024]; // max 64k bytes
        BufferedReader file;
        try {
            file = new BufferedReader(new FileReader(filePath.toString()));
        } catch (FileNotFoundException ex) {
            return -1;
        }
        Boolean end = false;
        try {
            while(!end && (line = file.readLine()) != null) {
                if(line.length() > 10) {
                    end = line.equals(":00000001FF"); // end record
                    if((line.charAt(0) == ':') && line.substring(7, 9).equals("00")) { // is it a data record?
                        recordLength = getHexByte(1,line);
                        address = getHexByte(3,line)<<8 + getHexByte(5,line);
                        if(startAddress < 0) { // save the first address as start address
                            startAddress = address;
                        }
                        for(int i=0; i<recordLength; i++) { // read data bytes
                            buf[dataIndex] = (byte) getHexByte(9+2*i,line);
                            dataIndex++; // count bytes read
                        }
                    }
                }
            }
            file.close();
        } catch (IOException ex) {
            return -1;
        }
        byte[] data = new byte[dataIndex];
        System.arraycopy(buf, 0, data, 0, dataIndex);
        list.add( bytTape(startAddress, data, filePath.getFileName().toString()) );
        return 1;
    }

    @Override
    public int save(Path filePath, List<JaTape> list, int[] selection) {
        if(selection.length <= 0) { // No selection Error
            return -1;
        }
        
        JaTape tapeFile = list.get(selection[0]); // Get only the first selection

        int dataLength = tapeFile.getParameter(JaTape.LENGTH);
        int recordAddress = tapeFile.getParameter(JaTape.ADDRESS);
        int dataIndex = 0;
        byte recordLength = 16; // 16 bytes per line record
        byte recordCrc;
        
        FileOutputStream fos;
        OutputStreamWriter out;
        
        try {
            fos = new FileOutputStream(filePath.toString());
            out = new OutputStreamWriter(fos, "UTF-8");
            
            // write records
            while(dataLength > 0) {
                if(dataLength < recordLength) { // fix last record length
                    recordLength = (byte)(dataLength);
                }
                // write record prefix :rraaaatt ": rec.length address rec.type"
                out.write(String.format(":%02X%04X00", recordLength, recordAddress));
                // Initialize CRC calculation
                recordCrc = (byte)(recordAddress + (recordAddress>>8));
                recordCrc += recordLength;
                // write data bytes
                for(int i = 0; i < recordLength; i++) {
                    out.write(String.format("%02X", tapeFile.getDataByte(dataIndex)));
                    recordCrc += tapeFile.getDataByte(dataIndex);
                    dataIndex++;
                }
                // write record CRC
                recordCrc = (byte)(0-recordCrc); // CRC two's complement
                out.write(String.format("%02X\n", recordCrc));
                
                recordAddress += recordLength; // update address
                dataLength -= recordLength;    // count down saved data
            }
            out.write(":00000001FF\n"); // write Hex file End string
            out.close();
        } catch (FileNotFoundException ex) {
            return -1;
        } catch (UnsupportedEncodingException ex) {
            return -1;
        } catch (IOException ex) {
            return -1;
        }
 
        return 1;   // One file saved
    }

    private int getHexByte(int index, String line) {
        return ((translate(line.charAt(index))<<4) + translate(line.charAt(index+1)));
    }
    
    private int translate(char c) {
        switch(c) {
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            case 'A': return 10;
            case 'B': return 11;
            case 'C': return 12;
            case 'D': return 13;
            case 'E': return 14;
            case 'F': return 15;
        }
        return 0;
    }
    
}

