/*
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
package JatmUI;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Ricardo
 */
public final class CharacterSet {
    // ------------------ STATIC --------------------    
    private static final byte[] romCharacters; // Default character set
    static {
        romCharacters = new byte[1024];
        try (InputStream in = CharacterSet.class.getResourceAsStream("resources/romchars.bin")) {
            in.read(romCharacters);
            in.close();
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }
    // ----------------------------------------------
    
    private byte[] charSet;            // Character set as binary
    private BufferedImage[] charImage; // Character Set as image
    
    public CharacterSet() { // Simple Constructor
        init();
        drawCharacters();
    }
    
    public CharacterSet(byte[] cs, int offset) { // Constructor with a character set
        init();
        load(cs, offset);
    }

    private void init() { // Initialize Object variables
        // Start with default ROM char set
        charSet = new byte[romCharacters.length];
        charSet = romCharacters.clone();
        // Create empty Image 
        charImage = new BufferedImage[256];
        for(int i=0; i<256; i++) {
            charImage[i] = new BufferedImage(8,8,BufferedImage.TYPE_BYTE_BINARY);
        } 
    }
    
    public void load(byte[] cs, int offset) { // load a character set
        // initialize Char Set to default ROM char set
        charSet = romCharacters.clone();
        // overwrite new data into default Char Set
        for(int i = 0; i < cs.length; i++) {
            if (i+offset*8 < charSet.length) {
                charSet[i+offset*8] = cs[i];
            }
        }
        drawCharacters();  
    }
    
    public void drawCharacters() {
        byte pattern;
        int chrRow, chrIndex;
        for(int i = 0; i < charSet.length; i++) { // Draw char set image
            chrIndex = i / 8;    // Character Index
            chrRow = i % 8;      // Character row

            pattern = charSet[i]; // get char row pattern
            
            for(int chrCol=0; chrCol<8; chrCol++) { // Draw a Character row pattern
                if((0x80&(pattern << chrCol)) == 0) {
                    // Clear Pixel ** may improve clearing image first then draw only the set pixels **
                    charImage[chrIndex].getRaster().setSample(chrCol,chrRow,0,0);     // Normal set
                    charImage[chrIndex+128].getRaster().setSample(chrCol,chrRow,0,1); // Inverted Set
                } else {
                    // Set Pixel
                    charImage[chrIndex].getRaster().setSample(chrCol,chrRow,0,1);     // Normal set
                    charImage[chrIndex+128].getRaster().setSample(chrCol,chrRow,0,0); // Inverted Set
                }
            }        
        }
    }
    
    public BufferedImage getImage(int index) {
        if(index<0 || index>255) {
            index = 0;
        }
        return charImage[index];
    }
    
}
