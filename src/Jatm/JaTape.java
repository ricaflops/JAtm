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

/**
 * Implements a Jupiter Ace Tape File structure
 * @author Ricardo F. Lopes
 */
public class JaTape {

    /**
     * File and Block Type constants
     */
    public static final byte DICT_FILE    = (byte) 0x00;
    public static final byte BYT_FILE     = (byte) 0x20;

    /**
     * Header Parameter Index
     * to be used with getParameter and setParameter methods
     */
    public static final int BLOCK_TYPE =  0; // 1 byte
    public static final int FILE_TYPE  =  1; // 1 byte
    public static final int FILE_NAME  =  2; // 10 byte string
    public static final int LENGTH     = 12; // 2 bytes
    public static final int ADDRESS    = 14; // 2 bytes
    public static final int CURR_WRD   = 16; // 2 bytes
    public static final int CURRENT    = 18; // 2 bytes
    public static final int CONTEXT    = 20; // 2 bytes
    public static final int VOCLNK     = 22; // 2 bytes
    public static final int STKBOT     = 24; // 2 bytes
    public static final int CRC        = 26; // 1 byte

    /**
     * Header Block Size in Bytes
     */
    public static final int HEADER_LENGTH = 27; // Header Block Size

    private final JaTapeBlock header;
    private final JaTapeBlock data;
    private int baseAddress;

    /**
     * Constructor: Empty Tape File
     */
    public JaTape() {
        this(new byte[HEADER_LENGTH], new byte[2]);
    }

    /**
     * Copy Constructor
     * @param tape
     */
     public JaTape(JaTape tape) {
        this(tape.getHeaderBlock(), tape.getDataBlock());
    }

    /**
     * Constructor: Tape File from header and data blocks
     * @param hdr header block byte array
     * @param dat data block byte array
     */
    public JaTape(byte[] hdr, byte[] dat) {
        // Make shure Header block is of correct size
        byte[] buf = new byte[HEADER_LENGTH];
        int length = (buf.length<hdr.length) ? buf.length : hdr.length;
        System.arraycopy(hdr, 0, buf, 0, length);

        header = new JaTapeBlock(buf);
        data = new JaTapeBlock(dat);
        baseAddress = getParameter(JaTape.ADDRESS); // file base address
    }

    /**
     * Set a whole data block array, including block type and CRC bytes
     * @param block data block
     */
    public void setDataBlock(byte[] block) {
        data.set(block);

    }

    /**
     * Get the whole data block array, including block type and CRC bytes
     * @return data block
     */
    public byte[] getDataBlock() {
        return data.get();
    }

    /**
     * Get the core data from data block array, excluding block type and CRC bytes
     * @return core data block
     */
    public byte[] getData() {
        return data.getPart(1,data.length()-2);
    }

    /**
     * Set byte value to a Data block position
     * @param index position in Data block array (excluding block type byte)
     * @param b byte value to be set in Data block position
     */
    public void setDataByte(int index, byte b) {
        data.setByte(index+1,b); // skip block type byte
    }

    /**
     * Get byte value from Data block position
     * @param index position in Data block array (excluding block type byte)
     * @return  byte value in Data block position
     */
    public byte getDataByte(int index) {
        return data.getByte(index+1); // skip block type byte
    }

    /**
     * Set a 2 byte value to a Data block position
     * @param index position in Data block array (excluding block type byte)
     * @param w 2 byte value to be set in Data block position
     */
    public void setDataWord(int index, int w) {
        data.setWord(index+1,w); // skip block type byte
    }

    /**
     * Get 2 byte value from Data block position
     * @param index position in Data block array (excluding block type byte)
     * @return 2 byte value in Data block position
     */
    public int getDataWord(int index) {
        return data.getWord(index+1); // skip block type byte
    }

    /**
     * Set the Header block from a byte array
     * @param b byte array to be set as the Header block
     */
    public void setHeaderBlock(byte[] b) {
        // Limit Header block length to HEADER_LENGTH bytes exactly
        byte[] h = new byte[HEADER_LENGTH];
        int len = (h.length > b.length)?b.length:h.length;
        System.arraycopy(h, 0, b, 0, len);

        header.set(h);
        baseAddress = getParameter(JaTape.ADDRESS); // file base address
    }

    /**
     * Get the Header block as a byte array
     * @return header block array
     */
    public byte[] getHeaderBlock() {
        return header.get();
    }

    /**
     * Set Header Parameters to a canonical Byt file type
     */
    public void makeByt() {
        header.setType(JaTapeBlock.HEADER_BLOCK);

        header.setByte(FILE_TYPE, BYT_FILE);
        header.setWord(CURR_WRD, 0x2020);
        header.setWord(CURRENT, 0x2020);
        header.setWord(CONTEXT, 0x2020);
        header.setWord(VOCLNK, 0x2020);
        header.setWord(STKBOT, 0x2020);
    }

    /**
     * Set Header Parameters to a canonical Dict file type
     */
    public void makeDict() {
        header.setType(JaTapeBlock.HEADER_BLOCK);

        header.setByte(FILE_TYPE, DICT_FILE);
        header.setWord(ADDRESS, 0x3C51);
        header.setWord(CURRENT, 0x3C4C);
        header.setWord(CONTEXT, 0x3C4C);
        header.setWord(VOCLNK, 0x3C4F);
        header.setWord(STKBOT, 0x3C5D);
    }

    /**
     * Check if file is of type Dict
     * @return True if a Dict type file
     */
    public Boolean isDict() {
        return (header.getByte(FILE_TYPE)==0x00);
    }

    /**
     * get file type string
     * @return file type as a String "dict" or " byt"
     */
    public String getFileType() {
        if(isDict()) {
            return "dict";
        } else {
            return " byt";
        }
    }

    /**
     * Set file type: Dict or Byt
     * @param type File type byte
     */
    public void setType(byte type) {
        header.setByte(FILE_TYPE, type);
    }

    /**
     * Get File name
     * @return file name string
     */
    public String getFilename() {
        return header.getString(FILE_NAME, 10);
    }

    /**
     * Set the File name (max 10 characters)
     * @param filename
     */
    public void setFilename(String filename) {
        filename = filename + "          "; // Pad with Spaces
        header.setString(FILE_NAME, filename.substring(0, 10));
    }

    /**
     * Get a Header Parameter
     * @param index Header block position
     * @return 2 byte parameter value
     */
    // get word size Parameters. For use with constants LENGTH to STKBOT
    public int getParameter(int index) {
        return header.getWord(index);
    }

    /**
     * Set a Header Parameter
     * @param index Header block position
     * @param parameter 2 byte value to be set
     */
    public void setParameter(int index, int parameter) {
        header.setWord(index, parameter);
    }

    /**
     * Check if Header and Data blocks CRC's are correct
     * @return True if Header and Data blocks CRC's are ok
     */
    public Boolean crcOk() {
        return (header.crcOk() && data.crcOk());
    }

    /**
     * Check if Header block CRC is correct
     * @return True if Header block CRC is ok
     */
    public Boolean headerCrcOk() {
        return header.crcOk();
    }

    /**
     * Check if Data block CRC is correct
     * @return True if Data block CRC is ok
     */
    public Boolean dataCrcOk() {
        return data.crcOk();
    }

    /**
     * Calculate and set correct Header and Data blocks CRC bytes
     */
    public void fixCrc() {
        header.fixCrc();
        data.fixCrc();
    }

    /**
     * Calculate and set a correct Header block CRC byte
     */
    public void fixHeaderCrc() {
        header.fixCrc();
    }

    /**
     * Calculate and set a correct Data block CRC byte
     */
    public void fixDataCrc() {
        data.fixCrc();
    }

    /**
     * Get Header block CRC byte
     * @return Header Block CRC byte
     */
    public byte getHeaderCrc() {
        return header.getCrc();
    }

    /**
     * Get Data block CRC byte
     * @return Data block CRC byte
     */
    public byte getDataCrc() {
        return data.getCrc();
    }

    /**
     * Set Header block CRC byte
     * @param crc
     */
    public void setHeaderCrc(byte crc) {
        header.setCrc(crc);
    }

    /**
     * Set Data block CRC byte
     * @param crc
     */
    public void setDataCrc(byte crc) {
        data.setCrc(crc);
    }

    /**
     * Check for a valid file address
     * @param address Address to be checked
     * @return false if address outside file boundaries
     */
    public boolean validAddress(int address) {
        return (address >= baseAddress)
                && (address < baseAddress + getParameter(JaTape.LENGTH));
    }

    /**
     * Get byte from memory address. Returns Zero if invalid address
     * @param address byte location
     * @return byte at address (0-FF)
     */
    public byte getMemByte(int address) {
        if (validAddress(address)) {
            return getDataByte(address - baseAddress);
        }
        return 0;
    }

    /**
     * Get two byte value at address. Returns Zero if invalid address
     * @param address bytes location: lo-byte high-byte order
     * @return value at address (0-FFFF)
     */
    public int getMemWord(int address) {
        if (validAddress(address)) {
            return getDataWord(address - baseAddress);
        }
        return 0;
    }
}
