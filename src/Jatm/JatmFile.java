/*
 * Base class for Jatm file formats
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

import java.nio.file.Path;
import java.util.List;

/**
 * Base class for different file formats supported by JAtm
 * @author Ricardo F. Lopes
 */
public abstract class JatmFile {
    protected String extension;
    protected String description;

    public String getExtension() {
        return extension;
    }

    public String getDescription() {
        return description;
    }

    public abstract int load(Path filePath, List<JaTape> list);
    public abstract int save(Path filePath, List<JaTape> list, int[] selection);
    public int getWord(byte[] b, int index) {
        int word = 0;
        word += b[index] & 0xFF;         // get low byte
        word += (b[index+1] &0xFF) <<8;  // get high byte
        return word;        
    }
    
    public int loByte(int word) {
        return word & 0x00FF;
    }
    
    public int hiByte(int word) {
        return loByte(word >> 8);
    } 
}
