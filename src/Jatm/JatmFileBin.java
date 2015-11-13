/*
 * JatmFileBin - Raw binary file format for Jatm
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * .BIN Binary file format for JAtm
 * @author Ricardo
 */
public class JatmFileBin extends JatmFile {

    public JatmFileBin() {
        extension = "bin";
        description = "Binary files (*.bin)";        
    }
    
    protected JaTape bytTape(int start, byte[] bin, String filename) {
        // Build Data Block
        int length = (bin.length < 0x10000) ? bin.length : 0xFFFF;
        byte[] dataBlock = new byte[length+2];           // create data block
        dataBlock[0] = JaTapeBlock.DATA_BLOCK;                // set Block Type
        System.arraycopy(bin, 0, dataBlock, 1, length);  // fill data

        // Create a proper tape file name from disk file name
        filename = filename.substring(0,filename.lastIndexOf("."));    // Remove extendion
        filename = (filename.length() > 10) ? filename.substring(0, 10) : filename;    // Trim if necessary
 
        // Create a tape file
        JaTape tape = new JaTape(new byte[JaTape.HEADER_LENGTH], dataBlock);
        tape.makeByt();
        tape.setFilename(filename);
        tape.setParameter(JaTape.ADDRESS, start);
        tape.setParameter(JaTape.LENGTH, bin.length);
        tape.fixCrc();

        return tape;
    } 
    
    @Override
    public int load(Path filePath, List<JaTape> list) {
        byte[] buf; 
        try {
            buf = Files.readAllBytes(filePath);
        } catch (IOException e) {
            return -1;
        }

        list.add( bytTape(0, buf, filePath.getFileName().toString()) );
        return 1;
    }

    @Override
    public int save(Path filePath, List<JaTape> list, int[] selection) {
        if(selection.length <= 0) { // No selection
            return -1; // Error
        }

        JaTape tapeFile = list.get(selection[0]); // Only the first selected
        byte[] data = tapeFile.getDataBlock();
        if(data.length <= 2) { // Avoid empty data block
            return -1;  // Error
        }

        // Trim off Block Type and CRC from data block and save it        
        byte[] buf = new byte[data.length-2];
        System.arraycopy(data, 1, buf, 0, buf.length);
        try {
            Files.write(filePath, buf);
        } catch (IOException ex) {
            return -1;  // Error
        }
        return 1;   // One file saved
    }  
}
