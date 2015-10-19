/*
 * JaTapeBlock - Class to represent a Jupiter Ace Tape File Block
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

import java.nio.charset.StandardCharsets;

/*
 * A Jupiter Ace file has 2 blocks: a header block and a data block
 * The general block structure is:
 *   - [1 byte]  Starts with a Block Type code..
 *   - [n bytes] ..followed by the block data section..
 *   - [1 byte]  ..and finish with a CRC.
 */

public class JaTapeBlock {

    private byte[] data;

    public JaTapeBlock(byte[] data) {  
        this.data = data;
    }   
    
    private Boolean validIndex(int index) {
        return (index>=0 && index<data.length);
    }

    public void set(byte[] b) {
        this.data = b;
    }

    public byte[] get() {
        return this.data;
    }
    
    public int length() {
        return this.data.length;
    }
    
    public void setByte(int index, byte b) {
        if(validIndex(index)) {
            data[index] = b;
        }
    }    

    public byte getByte(int index) {
        byte dat = 0;
        if(validIndex(index)) {
            dat = data[index];
        }
        return dat;
    }
    
    public void setWord(int index, int w) {
        setByte(index,(byte)(w & 0xFF));
        setByte(index+1,(byte)((w>>8) & 0xFF));
    }   
    
    public int getWord(int index) {
        return ((data[index+1] & 0xFF) << 8) | (data[index] & 0xFF);
    }

    public void setPart(int index, byte[] b) {
        int len = b.length;
        if( index + len > data.length ) {
            len = data.length - index;
        }
        System.arraycopy(b, 0, data, index, len);    
    }

    public byte[] getPart(int index, int length) {
        int len = length;
        if( index + len > data.length ) {
            len = data.length - index;
        }
        
        byte[] b = new byte[len];
        System.arraycopy(data, index, b, 0, len);
        return b;
    }

    public void setString(int index, String s) {
        setPart(index, s.getBytes(StandardCharsets.UTF_8));
    }
    
    public String getString(int index, int length) {
        return  new String(getPart(index,length), StandardCharsets.UTF_8);
    }
    
    public void setType(byte blockType) {
        setByte(0,blockType);
    }

    public byte getType() {
        return getByte(0);
    }
    
    public void setCrc(byte crc) {
        setByte(data.length-1, crc);
    }
    
    public byte getCrc() {
        return getByte(data.length-1);
    }

    public void fixCrc() { // Calculate and Fix CRC
        setCrc( crc() );
    }
    
    public Boolean crcOk() { // Check CRC
        return ( getCrc() == crc() );
    }
    
    public byte crc() { // Calculate CRC
        byte c = 0;
        // not including block type and CRC bytes
        for(int i = 1; i < data.length-1; i++) {
            c ^= getByte(i);
        }
        return c;
    }

}
