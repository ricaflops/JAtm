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

/**
 * Represents a Jupiter Ace Tape File block structure
 * A Jupiter Ace file has 2 blocks: a header block and a block block
 * The general block structure is:
 *  - [1 byte]  Starts with a Block Type code..
 *  - [n bytes] ..followed by the block block section..
 *  - [1 byte]  ..and finish with a CRC.
 * @author Ricardo F. Lopes
 */
public class JaTapeBlock {
    /** Header Block Type Tag */
    public static final byte HEADER_BLOCK = (byte) 0x00;
    /** Data Block Type Tag   */   
    public static final byte DATA_BLOCK   = (byte) 0xFF;
    
    private byte[] block; // the block contents

    /**
     * Cosntructor
     * @param data Byte array to initialize the Block contents 
     */
    public JaTapeBlock(byte[] data) {  
        this.block = data;
    }   
 
    /**
     * Get Block Size, including Block type and CRC bytes
     * @return block size in bytes
     */
    public int length() {
        return this.block.length;
    }
    
    /**
     * Check if index is inside Block array boundaries
     * @param index Index to verify
     * @return True if index is under Block size
     */
    private Boolean validIndex(int index) {
        return (index >= 0 && index < block.length);
    }

    /**
     * Set the entire Block contents
     * @param data byte array to became the Block contents
     */
    public void set(byte[] data) {
        this.block = data;
    }

    /**
     * Get the entire Block contents as a byte array
     * @return byte array with whole Block contents
     */
    public byte[] get() {
        return this.block;
    }

    /**
     * Set a byte in Block position
     * @param index Block position to write the byte
     * @param data byte to be writen
     */
    public void setByte(int index, byte data) {
        if(validIndex(index)) {
            block[index] = data;
        }
    }    

    /**
     * Get a byte value from Block position
     * @param index Block position to fetch the byte
     * @return the byte value
     */
    public byte getByte(int index) {
        byte dat = 0;
        if(validIndex(index)) {
            dat = block[index];
        }
        return dat;
    }

    /**
     * Set a two byte value in a Block position
     * @param index Block position to write the value
     * @param data 2 byte value to be writen
     */
    public void setWord(int index, int data) {
        setByte(index,(byte)(data & 0xFF));          // Lo Byte
        setByte(index+1,(byte)((data >> 8) & 0xFF)); // Hi Byte
    }   
 
    /**
     * Get a 2 byte value from Block position
     * @param index Block position
     * @return 2 byte value integer
     */
    public int getWord(int index) {
        if(index < 0 || index > block.length-2) { // check index range
        return 0; // return zero if invalid range
        } else {
            return ((block[index+1] & 0xFF) << 8) | (block[index] & 0xFF);
        }
    }

    /**
     * Write a byte array into the Block
     * @param index position to start writing the array
     * @param data the array to be writen
     */
    public void setPart(int index, byte[] data) {
        int len = data.length;
        if( index + len > block.length ) {
            len = block.length - index;
        }
        System.arraycopy(data, 0, block, index, len);    
    }

    /**
     * Get Block sub-array
     * @param index start of the sub-array
     * @param length size of the sub-array
     * @return the byte sub-array
     */
    public byte[] getPart(int index, int length) {
        int len = length;
        if( index + len > block.length ) {
            len = block.length - index;
        }
        
        byte[] b = new byte[len];
        System.arraycopy(block, index, b, 0, len);
        return b;
    }

    /**
     * Write a String tothe Block position
     * @param index Block position to write the string
     * @param str the String to write
     */
    public void setString(int index, String str) {
        setPart(index, str.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Get a String from Block index
     * @param index String position in block
     * @param length Size of the requested String
     * @return the constructed String
     */
    public String getString(int index, int length) {
        return  new String(getPart(index,length), StandardCharsets.UTF_8);
    }
    
    /**
     * Set Block Type
     * @param type
     */
    public void setType(byte type) {
        setByte(0, type);
    }

    /**
     * Get block type
     * @return 
     */
    public byte getType() {
        return getByte(0);
    }
    
    /**
     * set Block CRC byte value
     * @param crc 
     */
    public void setCrc(byte crc) {
        setByte(block.length-1, crc);
    }
    
    /**
     * Get Block CRC value
     * @return 
     */
    public byte getCrc() {
        return getByte(block.length-1);
    }

    /**
     * Set Block CRC to a correctly calculated value
     */
    public void fixCrc() { // Calculate and Fix CRC
        setCrc( crc() );
    }
    
    /**
     * Check Block CRC correctness
     * @return True if CRC is correct
     */
    public Boolean crcOk() { // Check CRC
        return ( getCrc() == crc() );
    }
    
    /**
     * Calculate a correct Block CRC value. Do not change current CRC value.
     * @return calculated CRC value
     */
    public byte crc() { // Calculate CRC
        byte c = 0;
        // Callculation does not include block type and CRC bytes
        for(int i = 1; i < block.length-1; i++) {
            c ^= getByte(i);
        }
        return c;
    }

}
