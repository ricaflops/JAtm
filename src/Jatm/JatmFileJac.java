/*
 * JatmFileJac - Jupiter Ace file format for Jatm
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
 * .JAC Jupiter Ace file format for JAtm
 * @author Ricardo F. Lopes
 */
public class JatmFileJac extends JatmFile {
    public JatmFileJac() {
        extension = "jac";
        description = "Jupiter Ace files (*.jac)";
    }
    
    @Override
    public int load(Path filePath, List<JaTape> list) {
        int fileCount = 0;
        byte[] buf;
        try {    // Load whole file to a buffer
            buf = Files.readAllBytes(filePath);
        } catch (IOException ex) {
            return -1;
        }

        if(buf == null) {   // Proceed only if data exist
            return -1;
        }
        
        if(buf.length > JaTape.HEADER_LENGTH) {
            byte[] header = new byte[JaTape.HEADER_LENGTH]; // get Header
            byte[] data = new byte[buf.length - JaTape.HEADER_LENGTH];
            System.arraycopy(buf, 0, header, 0, JaTape.HEADER_LENGTH);
            System.arraycopy(buf, JaTape.HEADER_LENGTH, data, 0, data.length);
            JaTape tapeFile = new JaTape(header, data);
            list.add( tapeFile );    // Add it to the list of tape files
            fileCount++;
        }
        
        return fileCount;
    }

    @Override
    public int save(Path filePath, List<JaTape> list, int[] selection) {
        if(selection.length <= 0) {
            return -1;  // Error
        }            

        JaTape tapeFile = list.get(selection[0]); // Only the first selected
        byte[] header = tapeFile.getHeaderBlock();
        byte[] data = tapeFile.getDataBlock();

        if(data.length <= 2) {
            return -1;  // Error
        }
        
        byte[] buf = new byte[header.length + data.length];
        System.arraycopy(header, 0, buf, 0, header.length);
        System.arraycopy(data, 0, buf, header.length, data.length);
        try {
            Files.write(filePath, buf);
        } catch (IOException ex) {
            return -1;
        }
        
        return 1;   // One file Saved
    }
}
