/*
 * JatmFileTap - TAP file format for Jatm
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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JatmFileTap extends JatmFile {
    public JatmFileTap() {
        extension = "tap";
        description = "TAP files (*.tap)";
    }
    
    @Override
    public int load(Path filePath, List<JaTape> list) {
        byte[] buffer; 
        try {       // Load whole file to a buffer
            buffer = Files.readAllBytes(filePath);
        } catch (IOException ex) {
            return -1;
        }

        if(buffer == null) {   // Proceed only if data exist
            return -1;
        }

        int tapeCount = 0; // Count number of Jupiter Ace tape files found            
        int index = 0;     // buffer index
        int blockLength;   // current block length
        do {
            // Load the Header Block
            blockLength = getWord(buffer, index); // Header TAP block length
            byte[] header = new byte[blockLength+1];
            header[0] = JaTape.HEADER_BLOCK; // Fix Header block type            
            System.arraycopy(buffer, index+2, header, 1, blockLength);            

            index += blockLength + 2; // advance buffer pointer to next block
            
            // Load the Data Block
            blockLength = getWord(buffer, index); // Data TAP block length
            byte[] data = new byte[blockLength+1];
            data[0] = JaTape.DATA_BLOCK; // Fix Data block type           
            System.arraycopy(buffer, index+2, data, 1, blockLength);
            
            index += blockLength + 2; // advance buffer pointer to next block 

            JaTape ja = new JaTape(header, data); // Build a Tape File
            list.add( ja );    // Add it to the list of Jpupiter Ace Files
            tapeCount++;
        } while(index < buffer.length);

        return tapeCount;
    }

    @Override
    public int save(Path filePath, List<JaTape> list, int[] selection) {
        if(selection.length <= 0) {
            return -1;  // Error: No selection
        }

        DataOutputStream out;
        File file = new File(filePath.toString());
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        } catch  (IOException ex) {
            return -1; // Error writing to file
        }
        
        JaTape tapeFile;
        byte[] header;
        byte[] data;        
        for(int i=0; i < selection.length; i++) {
            tapeFile = list.get(selection[i]);
            header = tapeFile.getHeaderBlock();
            data = tapeFile.getDataBlock();
            try {
                out.writeByte(loByte(header.length-1));
                out.writeByte(hiByte(header.length-1));
                out.write(header, 1, header.length-1);

                out.writeByte(loByte(data.length-1));
                out.writeByte(hiByte(data.length-1));
                out.write(data, 1, data.length-1);
            } catch  (IOException ex) {
                return -1; // Error writing to file
            }
        }
        // close file
        try { out.close(); } catch (IOException ex) { return -1; }   
        return 1;   // One file saved
    } 
}
