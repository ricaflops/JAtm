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
package Jatm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Integer.parseInt;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 RAM Vocabulary Header Fields:
 0 to 63 bytes   Name    Word name   last char with bit-7 set
 2 bytes         Length  Lebgth of Parameter Field + 7
 2 bytes         Link    Points to previous word Name Length Field
 1 byte          Name Length (bits 0-5): Name Length   bit 6: IMMEDIATE word flag
 2 bytes         Code        strBuf address to execute this word
 n bytes         Parameter   (n = Length Field - 7)

 Offset from CFA:
 -n-5: Name (0-63 characters)
 -5: Length (x)
 -3: Link (point to previous word Name Length)
 -1: Name Length (n) (bit-6 = IMMEDIATE flag)
 0: Code
 +2: Parameter (x-7 bytes)
 */

/**
 * List Jupiter Ace file FORTH vocabulary contents and disassemble words
 * @author Ricardo F. Lopes
 */
public class JaVocabulary {

    private final HashMap<Integer, String> vocabularyMap; // Tape file vocabulary
    List<Integer> cfaList;
    private final JaTape tapeFile;

    // Max Number of Words per line in code listing
    private static final int MAX_WORDS_PER_LINE = 5;

    // Word Types based on CFA (Code Field Address)
    private static final int DOCOLON  = 0x0EC3;
    private static final int CREATE   = 0x0FEC;
    private static final int VARIABLE = 0x0FF0;
    private static final int CONSTANT = 0x0FF5;
    private static final int DEFINER  = 0x1085;
    // private static final int COMPILER = 0x10F5;
    private static final int COMPILER  = 0x1108;

    private static final int ROM_DOES  = 0x10E8;
    private static final int ROM_RUNS  = 0x1140;

    // Immediate Words in ROM needs special decoding
    private static final int ROM_STK_BYTE     = 0x104B; // ASCII
    private static final int ROM_STK_FP       = 0x1064; // floating point number
    private static final int ROM_STK_INT      = 0x1011; // integer number
    private static final int ROM_IF           = 0x1283; // IF
    private static final int ROM_ELSE         = 0x1271; // ELSE
    private static final int ROM_LOOP         = 0x1332; // LOOP
    private static final int ROM_PLOOP        = 0x133C; // +LOOP
    private static final int ROM_UNTIL        = 0x128D; // UNTIL
    private static final int ROM_PRINT_STRING = 0x1396; // ."
    private static final int ROM_COMMENT      = 0x1379; // (
    private static final int ROM_THEN         = 0x12A4; // THEN
    private static final int ROM_DO           = 0x1323; // DO
    private static final int ROM_BEGIN        = 0x129F; // BEGIN
    private static final int ROM_WHILE        = 0x1288; // WHILE
    private static final int ROM_REPEAT       = 0x1276; // REPEAT
    private static final int ROM_SEMICOLON    = 0x04B6; // ;

    //------------------ Static ROM Vocabulary read from a txt file ----------------
    private static final HashMap<Integer, String> ROM_VOC;
    static {
        ROM_VOC = new HashMap<>();
        try (InputStream in = JaVocabulary.class.getResourceAsStream("resources/ROM_words.txt")) {
            BufferedReader buf = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = buf.readLine()) != null) {
                String parts[] = line.split(" "); // Space delimited values
                ROM_VOC.put(parseInt(parts[0], 16), parts[1]); // Build Hash Map
            }
            in.close();
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }
    // -----------------------------------------------------------------------------

    /**
     * Constructor
     * @param tape Jupiter Ace tape object
     */
    public JaVocabulary(JaTape tape) {
        vocabularyMap = new HashMap<>(); // Tape File vocabulary
        cfaList = new ArrayList<>();   // CFA list
        tapeFile = tape;
        buildVocabularyList();
    }

    /**
     * Scan file and build a vocabulary list populating <code>cfaList</code>
     */
    private void buildVocabularyList() {
        if (tapeFile.isDict()) { // scan Words only if it is a Dictionary file
            int link = tapeFile.getParameter(JaTape.CURR_WRD); // get first word link
            // scan until link to outside file boudaries
            while (tapeFile.validAddress(link)) {
                // Get Word Name Length
                byte nameLength = (byte) (tapeFile.getMemByte(link) & 0x3F); // clear bits 6,7
                // Get Word Name Characters
                byte[] nameChars = new byte[nameLength];
                for (int i = 0; i < nameLength; i++) { // clear bit-7 of all chars
                    nameChars[i] = (byte) (tapeFile.getMemByte(link - 4 - nameLength + i) & 0x7F);
                }
                // Create Word Name String
                String wordName = new String(nameChars, StandardCharsets.UTF_8);
                // Build CFA to Word name map
                vocabularyMap.put(link + 1, wordName); // CFA = link+1
                cfaList.add(link + 1);                 // Build CFA List

                link = tapeFile.getMemWord(link - 2);  // get next word link
            }
        }
    }

    /**
     * @return Number of words in vocabulary
     */
    public int vocabularySize() {
        return vocabularyMap.size();
    }

    /**
     * Lists words in vocabulary (similar to VLIST). One word per line
     * @return vocabulary list as a String
     */
    public String vlist() {
        StringBuilder wordList = new StringBuilder();
        cfaList.stream().forEach((cfa) -> {
            wordList.append(vocabularyMap.get(cfa)).append(" ");
        });
        return wordList.toString();
    }

    /**
     * Lists words in vocabulary (similar to VLIST). One word per line
     * @return vocabulary list as a String
     */
    public String listCfa() {
        StringBuilder wordList = new StringBuilder();
        cfaList.stream().forEach((cfa) -> {
            wordList.append(String.format("%04Xh: ", cfa));
            wordList.append(vocabularyMap.get(cfa)).append("\n");
        });
        return wordList.toString();
    }    
    
    /**
     * Disassemble FORTH vocabulary
     * @return Decoded vocabulary as a String
     */
    public String listAllWords() {
        StringBuilder strBuf = new StringBuilder(); // resulting String buffer
        // Reverse word list order to disassemble older words first
        int[] reverseCfaList = new int[cfaList.size()];
        for (int i = 0; i < cfaList.size(); i++) {
            reverseCfaList[reverseCfaList.length - i - 1] = cfaList.get(i);
        }
        // Disassemble each Forth word in list
        for (int cfa : reverseCfaList) {
            listWord(strBuf, cfa);
        }
        return strBuf.toString();
    }

    /**
     * List contents of a FORTH word
     * @param strBuf listing
     * @param cfa Word Code Field Address
     */
    private void listWord(StringBuilder strBuf, int cfa) {
        String wordName = vocabularyMap.get(cfa); // get word Name
        int codeField = tapeFile.getMemWord(cfa); // get word Code Field
        // Append Word prefix according to code field
        switch (codeField) {
            case CREATE:   // CREATE name (size)
                strBuf.append("CREATE");
                break;
            case VARIABLE: // VARIABLE name
                strBuf.append("VARIABLE");
                break;
            case CONSTANT: // xx CONSTANT name
                strBuf.append(getInt(cfa + 2));
                strBuf.append(" CONSTANT");
                break;
            case DEFINER:  // DEFINER name
                strBuf.append("DEFINER");
                break;
            case COMPILER:  // COMPILER name
                strBuf.append("COMPILER");
                break;
            default:       // : name
                strBuf.append(":");
                break;
        }
        
        // Append Word Name
        strBuf.append(" ").append(wordName).append(" ");

        // Append Word Body according to code field
        switch (codeField) {
            case CREATE:   // CREATE name (size)
                strBuf.append(" ( ").append(tapeFile.getMemWord(cfa - 5)).append(" bytes )");
                break;
            case VARIABLE: // VARIABLE name
            case CONSTANT: // xx CONSTANT name
                break;
            case DOCOLON:  // : name <FORTHcode> ;
            case DEFINER:  // DEFINER name <FORTHcode> DOES> ;
            case COMPILER: // COMPILER name <FORTHcode> RUNS> ;
                listDoColonWord(strBuf, cfa);
                break;
            default:       // : name ( CFA=xxxx ) ... unlistable word
                strBuf.append("( CFA=").append(String.format("%04Xh", cfa)).append(" )");
                break;
        }    
        strBuf.append("\n"); // Next line  
    }
    
    /**
     * List DOCOLON word contents
     * @param strBuf  is the output StringBuilder buffer
     * @param cfa is te Code Field Address of the word to disassemble
     */
    private void listDoColonWord(StringBuilder strBuf, int cfa) {
        int parameterFieldSize = tapeFile.getMemWord(cfa - 5) - 7; // Parameter Field Length
        int parameterFieldAddress = cfa + 2;            // Parameter Field Address
        int address = parameterFieldAddress; // current address
        int wordCount = MAX_WORDS_PER_LINE;  // Start with a new line now
        int tab = 1; // start with one space tab
        while (address < parameterFieldAddress + parameterFieldSize) {
            wordCount++; // Count words printed in in this line
            int parameter = tapeFile.getMemWord(address); // get parameter disassemble
            // Check for words that decreases identation
            if (    parameter == ROM_THEN   || parameter == ROM_WHILE     ||
                    parameter == ROM_REPEAT || parameter == ROM_SEMICOLON ||
                    parameter == ROM_LOOP   || parameter == ROM_PLOOP     ||
                    parameter == ROM_ELSE   || parameter == ROM_UNTIL     ||
                    parameter == ROM_DOES   || parameter == ROM_RUNS ) {
                tab--; // decrease tab
            }
            // Check for words that starts a new line
            if (    wordCount >= MAX_WORDS_PER_LINE || parameter == ROM_THEN   ||
                    parameter == ROM_WHILE          || parameter == ROM_REPEAT ||
                    parameter == ROM_SEMICOLON      || parameter == ROM_LOOP   ||
                    parameter == ROM_PLOOP          || parameter == ROM_ELSE   ||
                    parameter == ROM_UNTIL          || parameter == ROM_DOES   ||
                    parameter == ROM_RUNS           || parameter == ROM_DO     ||
                    parameter == ROM_BEGIN          || parameter == ROM_IF ) {
                wordCount = 0;       // Reset Words per Line Counter
                strBuf.append("\n"); // Start a new line
                for(int i = 0; i < tab; i++) { // Identation
                    strBuf.append(" ");
                }  
            }
            // Search parameter in Tape Vocabulary
            String wordString = vocabularyMap.get(parameter); // search in tape vocabulary
            // Search parameter in ROM vocabulary
            if (wordString == null) { // word not in tape vocabulary..
                wordString = ROM_VOC.get(parameter); // search in ROM vocabulary
            }
            // Check if it is a number
            if( parameter == ROM_STK_INT || parameter == ROM_STK_FP ) {
                wordString = "";
            }
            // Check if unknow word
            if (wordString == null) {
                wordString = String.format("[%04Xh]", parameter);
            }

            strBuf.append(wordString); // Append word name
            address += 2; // point to next parameter field data

            // Complement immediate words decoding
            switch (parameter) {
                case ROM_PRINT_STRING: // ."
                    address += appendString(strBuf, address);
                    strBuf.append("\"");
                    break;
                case ROM_COMMENT: // (
                    address += appendString(strBuf, address);
                    strBuf.append(")");
                    break;
                case ROM_STK_BYTE: // Stack Single Byte (ASCII)
                    strBuf.append(" ").append((char) (tapeFile.getMemByte(address))); // Character
                    address++; // drop 1 bytes
                    break;
                case ROM_STK_INT: // Stack Integer Number (2 bytes)
                    strBuf.append(getInt(address));
                    address += 2; // drop 2 bytes
                    break;
                case ROM_STK_FP: // Stack Floating Point Number (4 bytes)
                    address += appendFloat(strBuf, address);
                    break;
                case ROM_DOES:
                case ROM_RUNS:
                    address += 5; // drop 5 bytes
                    break;
                default: // do nothing
            }
            // Check for words with additional 2 byte parameter
            if(     parameter == ROM_IF    || parameter == ROM_ELSE  ||
                    parameter == ROM_WHILE || parameter == ROM_LOOP  ||
                    parameter == ROM_PLOOP || parameter == ROM_UNTIL ||
                    parameter == ROM_REPEAT ) {
                address += 2; // drop 2 bytes
            }
            // Check words that starts a new line after it
            if(     parameter == ROM_THEN         || parameter == ROM_DO      ||
                    parameter == ROM_BEGIN        || parameter == ROM_IF      ||
                    parameter == ROM_ELSE         || parameter == ROM_WHILE   ||
                    parameter == ROM_LOOP         || parameter == ROM_PLOOP   ||
                    parameter == ROM_UNTIL        || parameter == ROM_REPEAT  ||
                    parameter == ROM_PRINT_STRING || parameter == ROM_COMMENT ||
                    parameter == ROM_DOES         || parameter == ROM_RUNS ) {
                wordCount = MAX_WORDS_PER_LINE; // Start a new line
            }
            // Check for words that increase identation after it
            if(     parameter == ROM_DO    || parameter == ROM_BEGIN ||
                    parameter == ROM_IF    || parameter == ROM_ELSE  ||
                    parameter == ROM_WHILE || parameter == ROM_DOES  ||
                    parameter == ROM_RUNS ) {
                tab++; // increase identation
            }
            strBuf.append(" "); // append a space between words
        }
        if ((tapeFile.getMemByte(cfa - 1) & 0x40) != 0) { // bit-6 = immediate flag
            strBuf.append("IMMEDIATE");
        }
    }

    /**
     * Decode a String
     * @param strBuf Output StringBuilder buffer to append the string
     * @param address String location
     * @return string length in bytes
     */
    private int appendString(StringBuilder strBuf, int address) {
        int strlen = tapeFile.getMemWord(address);  // String Length
        address = address + 2;
        byte[] str = new byte[strlen];  // String Characters
        for (int i = 0; i < strlen; i++) {
            str[i] = tapeFile.getMemByte(address);
            address++;
        }
        strBuf.append(" ").append(new String(str)); // print string with leading space
        return strlen + 2;                        // return number of bytes
    }

    /**
     * Decode a floating Point Number (4 bytes long)
     * This routine emulates the way Jupiter Ace prints FP numbers
     * Floating Point number structure:
     * ------ hiWord -----   ------ loWord -----
     * s eeeeeee mmmm mmmm   mmmm mmmm mmmm mmmm
     * s = sign, eeeeeee = exponential (offseted by 65)
     * mmmm = mantissa (6 BCD digits)
     * @param strBuf Output StringBuilder buffer to append decoded number
     * @param address Floatinf point number structure location
     */
    private int appendFloat(StringBuilder strBuf, int address) {
        int loWord = tapeFile.getMemWord(address);
        int hiWord = tapeFile.getMemWord(address + 2);
        if ((hiWord & 0x8000) != 0) { // Negative Number
            strBuf.append("-");
        }

        int exp = ((hiWord & 0x7F00) >> 8) - 65; // Normalized Exponent
        if((hiWord & 0x7F00) == 0) {             // special case for 0.
            exp = 0;
        }
        int mantissa = ((hiWord & 0xFF) << 16) | loWord; // 6 BCD digits Mantissa

        int e = 0; // Print Exponential Format: 1.23456E10
        if (exp >= -4 && exp <= 9) { // Print Normal Format: 123.456
            e = exp; // decimal point position
            exp = 0; // flag Not Exponential Format
        }
        if(e < 0) { // print numbers like .000123
            strBuf.append(".");
            while(e < -1) {
                strBuf.append("0");
                e++;
            }
           e--; // force e = -1
        }

        do { // print mantissa
            strBuf.append((mantissa >> 20) & 0x0F); // print BCD digit
            if(e == 0) { // decimal point position
                strBuf.append(".");
            }
            e--; // decrement decimal place
            mantissa = (mantissa & 0xFFFFF) << 4; // roll digits
        } while(mantissa != 0);
        e++; // revert last decrement during mantissa printing

        while(e > 0) { // print trailing zeros
            strBuf.append("0");
            e--;  // decrement decimal place
            if(e == 0) { // decimal point position
                strBuf.append(".");
            }
        }

        if(exp != 0) { // Print exponential value
            strBuf.append("E").append(exp);
        }
        return 4;
    }

    /**
     * Decode an integer number (2 bytes)
     * @param address Integer number structure location
     * @return Decoded integer number
     */
    private int getInt(int address) {
        int number = tapeFile.getMemWord(address);
        if ((number & 0x8000) != 0) { // if negative number..
            number = -(1 + (number ^ 0xFFFF));  // convert
        }
        return number;
    }
}
