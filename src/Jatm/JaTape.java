/*
 * JaTape - A class to represent Jupiter Ace Tape File contents
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

public class JaTape {

    public static final byte DICT_FILE    = (byte) 0x00;
    public static final byte BYT_FILE     = (byte) 0x20;
    public static final byte HEADER_BLOCK = (byte) 0xFF;
    public static final byte DATA_BLOCK   = (byte) 0x00;
    // Header data offsets
    public static final int BLOCK_TYPE =  0; // Byte
    public static final int FILE_TYPE  =  1; // Byte
    public static final int FILE_NAME  =  2; // 10 byte string
    public static final int LENGTH     = 12; // Word
    public static final int ADDRESS    = 14; // Word
    public static final int CURR_WRD   = 16; // Word
    public static final int CURRENT    = 18; // Word
    public static final int CONTEXT    = 20; // Word
    public static final int VOCLNK     = 22; // Word
    public static final int STKBOT     = 24; // Word
    public static final int CRC        = 26; // Byte
    
    public static final int HEADER_LENGTH = 27; // Header Block Size
    
    private final JaTapeBlock header;
    private final JaTapeBlock data;

    public JaTape() {
        this(new byte[HEADER_LENGTH], new byte[2]);
    }
    
    public JaTape(byte[] hdr, byte[] dat) {
        // Make shure Header block is of correct size
        byte[] buf = new byte[HEADER_LENGTH];
        int length = (buf.length<hdr.length) ? buf.length : hdr.length;
        System.arraycopy(hdr, 0, buf, 0, length);

        header = new JaTapeBlock(buf);
        data = new JaTapeBlock(dat);
    }
    
    public void setDataBlock(byte[] b) {
        data.set(b);
    }
    
    // Get all data array, including block type and CRC bytes
    public byte[] getDataBlock() {
        return data.get();
    }

    // Get only the core data array, excluding block type and CRC bytes 
    public byte[] getData() {
        return data.getPart(1,data.length()-2);
    }
    
    public void setDataByte(int index, byte b) {
        data.setByte(index+1,b); // skip block type byte
    }
    
    public byte getDataByte(int index) {
        return data.getByte(index+1); // skip block type byte
    }

    public void setDataWord(int index, int w) {
        data.setWord(index+1,w); // skip block type byte
    }
    
    public int getDataWord(int index) {
        return data.getWord(index+1); // skip block type byte
    }    

    public void setHeaderBlock(byte[] b) {
        // Limit Header block length to HEADER_LENGTH bytes exactly
        byte[] h = new byte[HEADER_LENGTH];
        int len = (h.length > b.length)?b.length:h.length;
        System.arraycopy(h, 0, b, 0, len);
        
        header.set(h);
    }
    
    public byte[] getHeaderBlock() {
        return header.get();
    }
    
    public void makeByt() {
        header.setByte(BLOCK_TYPE, HEADER_BLOCK);
        header.setByte(FILE_TYPE, BYT_FILE);
        header.setWord(CURR_WRD, 0x2020);
        header.setWord(CURRENT, 0x2020);
        header.setWord(CONTEXT, 0x2020);
        header.setWord(VOCLNK, 0x2020);
        header.setWord(STKBOT, 0x2020);
    }
 
    public void makeDict() {
        header.setByte(BLOCK_TYPE, HEADER_BLOCK);
        header.setByte(FILE_TYPE, DICT_FILE);
        header.setWord(ADDRESS, 0x3C51);
        header.setWord(CURRENT, 0x3C4C);
        header.setWord(CONTEXT, 0x3C4C);
        header.setWord(VOCLNK, 0x3C4F);
        header.setWord(STKBOT, 0x3C5D);
    }

    public Boolean isDict() {
        return (header.getByte(FILE_TYPE)==0x00);
    }

    public void setType(byte type) {
        header.setByte(FILE_TYPE, type);
    }    
    
    public String getFilename() {
        return header.getString(FILE_NAME, 10);
    }
 
    public void setFilename(String s) {
        s = s + "          "; // Pad with Spaces
        header.setString(FILE_NAME, s.substring(0, 10));
    }

    // get word size Parameters. For use with constants LENGTH to STKBOT
    public int getParameter(int index) {
        return header.getWord(index);
    }
    
    // get word size Parameters. For use with constants LENGTH to STKBOT
    public void setParameter(int index, int parameter) {
        header.setWord(index, parameter);
    }  
    
    public Boolean crcOk() {
        return (header.crcOk() && data.crcOk());
    }
    
    public Boolean headerCrcOk() {
        return header.crcOk();
    }
    
    public Boolean dataCrcOk() {
        return data.crcOk();
    }
    
    public void fixCrc() {
        header.fixCrc();
        data.fixCrc();
    }
    
    public void fixHeaderCrc() {
        header.fixCrc();
    }
    
    public void fixDataCrc() {
        data.fixCrc();
    }
    
    public byte getHeaderCrc() {
        return header.getCrc();
    }
    
    public byte getDataCrc() {
        return data.getCrc();
    }
    
    public void setHeaderCrc(byte crc) {
        header.setCrc(crc);
    }
    
    public void setDataCrc(byte crc) {
        data.setCrc(crc);
    }
    
}
