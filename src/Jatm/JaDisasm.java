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
 RAM Vocabulry Header Fields:
 0 to 63 bytes   Name    Word name   last char with bit-7 set
 2 bytes         Length  Lebgth of Parameter Field + 7
 2 bytes         Link    Points to previous word Name Length Field
 1 byte          Name Length (bits 0-5): Name Length   bit 6: IMMEDIATE word flag
 2 bytes         Code        code address to execute this word
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
 *
 * @author Ricardo
 */
public class JaDisasm {

    private final HashMap<Integer, String> tapeFileVoc; // Tape file vocabulary
    List<Integer> cfaList;
    private final JaTape tapeFile;
    private final int baseAddress;

// Word Types based on CFA (Code Field Address)
    private static final int DOCOLON = 0x0EC3;
    private static final int CREATE = 0x0FEC;
    private static final int VARIABLE = 0x0FF0;
    private static final int CONSTANT = 0x0FF5;
    private static final int DEFINER = 0x1085;
    private static final int COMPILE = 0x1108;

// Immediate Words in ROM needs special decoding
    private static final int ROM_STK_BYTE = 0x104B; // ASCII
    private static final int ROM_STK_FP = 0x1064;
    private static final int ROM_STK_INT = 0x1011;
    private static final int ROM_IF = 0x1283;
    private static final int ROM_ELSE = 0x1271;
    private static final int ROM_LOOP = 0x1332;
    private static final int ROM_PLOOP = 0x133C; // +LOOP
    private static final int ROM_UNTIL = 0x128D;
    private static final int ROM_PRINT_STRING = 0x1396; // ."
    private static final int ROM_COMMENT = 0x1379; // (
    private static final int ROM_THEN = 0x12A4;
    private static final int ROM_DO = 0x1323;
    private static final int ROM_BEGIN = 0x129F;
    private static final int ROM_WHILE = 0x1288;
    private static final int ROM_REPEAT = 0x1276;
    private static final int ROM_SEMICOLON = 0x04B6; // ;

//------------------------- Static ROM Vocabulary ----------------
    private static final HashMap<Integer, String> ROM_VOC;

    static {
        ROM_VOC = new HashMap<>();
        try (InputStream in = JaDisasm.class.getResourceAsStream("resources/ROM_words.txt")) {
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
// ------------------------------------------------------------

// CONSTRUCTOR
    public JaDisasm(JaTape tape) {
        tapeFileVoc = new HashMap<>(); // Tape File vocabulary
        cfaList = new ArrayList<>();   // CFA list
        tapeFile = tape;
        baseAddress = tapeFile.getParameter(JaTape.ADDRESS); // file base address
        scanWords();
    }

// Get Vocabulary Words Quantity
    public int vocSize() {
        return tapeFileVoc.size();
    }

// List Tape File vocabulary words
    public String vlist() {
        StringBuilder voc = new StringBuilder();
        cfaList.stream().forEach((cfa) -> {
            voc.append(tapeFileVoc.get(cfa)).append("\n");
        });
        return voc.toString();
    }

// Decode all vocabulary words
    public String decode() {
        StringBuilder code = new StringBuilder();
        int[] cfaRevList = new int[cfaList.size()]; // reverse word list to list older words first
        for (int i = 0; i < cfaList.size(); i++) {
            cfaRevList[cfaRevList.length - i - 1] = cfaList.get(i);
        }

        for (int cfa : cfaRevList) {        // Loop for each word in list
            String wordName = tapeFileVoc.get(cfa); // get Word Name
            int codeField = getWord(cfa); // get word Code Field contents
            switch (codeField) {          // process according to Code Field kind
                case DOCOLON:
                    code.append(": ").append(wordName).append("\n ");
                    docolonDisasm(cfa, code);
                    break;
                case CREATE:
                    code.append("CREATE ").append(wordName).append(" ");
                    code.append(getWord(cfa - 5)).append(" ALLOT"); // Data Field Size
                    break;
                case VARIABLE:
                    code.append("VARIABLE ").append(wordName);
                    break;
                case CONSTANT:
                    code.append(getInt(cfa + 2)).append(" CONSTANT ").append(wordName);
                    break;
                case DEFINER:
                    code.append("DEFINER ").append(wordName);
                    break;
                case COMPILE:
                    code.append("COMPILE ").append(wordName);
                    break;
                default:
                    code.append(": ").append(wordName).append(" "); // show word name
                    code.append("( CFA = ").append(cfa).append(" )");
                    break;
            }
            code.append("\n"); // Next line
        }
        return code.toString();
    }

// Scan tape file for JA dictionary words
    private void scanWords() {
        if (tapeFile.isDict()) { // scan Words only if Dictionary file
            int link = tapeFile.getParameter(JaTape.CURR_WRD); // get file first word link
            // scan until linking outside file boudaries
            while (insideFile(link)) {
                // Get Word Name
                byte nameLen = (byte) (getByte(link) & 0x4F); // word name length
                byte[] nameChars = new byte[nameLen];
                for (int i = 0; i < nameLen; i++) {
                    // get each name chars cleaning bit-7
                    nameChars[i] = (byte) (getByte(link - 4 - nameLen + i) & 0x7F);
                }
                String name = new String(nameChars, StandardCharsets.UTF_8);

                tapeFileVoc.put(link + 1, name); // Build Map of CFA to Word name
                cfaList.add(link + 1);          // Build CFA List
                link = getWord(link - 2);       // get next word link
            }
        }
    }

// decode DOCOLON type words
    private void docolonDisasm(int cfa, StringBuilder code) {
        int parameterFieldLength = getWord(cfa - 5) - 7; // Parameter Field Length
        int parameterFieldAddress = cfa + 2;            // Parameter Field Address

        // Decode Word contents
        int wordsPerLine = 0; // controls how many words to print per line
        int address = parameterFieldAddress; // current address
        while (address < parameterFieldAddress + parameterFieldLength) {
            // get parameter to decode
            int parameter = getWord(address);
            // force new line
            switch (parameter) {
                case ROM_THEN:
                case ROM_DO:
                case ROM_BEGIN:
                case ROM_WHILE:
                case ROM_REPEAT:
                case ROM_SEMICOLON:
                case ROM_IF:
                case ROM_ELSE:
                case ROM_LOOP:
                case ROM_PLOOP:
                case ROM_UNTIL:
                    code.append("\n");
                    wordsPerLine = 0;
                    break;
                default:
                    wordsPerLine++; // Count words per line
                    if (wordsPerLine > 5) { // Words per line limit
                        code.append("\n "); // change line and add a space
                        wordsPerLine = 0;   // Reset Words per Line Counter
                    }
                    break;
            }
            // Decode Word
            String word = tapeFileVoc.get(parameter);
            if (word == null) { // word not in tape vocabulary..
                word = ROM_VOC.get(parameter); // check ROM vocabulary
            }
            if (word != null) {    // Word Found..
                code.append(word); // print Word.
            } else {               // Word not Found..
                if ((parameter != ROM_STK_INT) && (parameter != ROM_STK_FP)) { // Not a Number...
                    code.append("(").append(parameter).append(")");            // unknow word.
                }
            }

            // point to next parameter to decod
            address = address + 2;

            // Continue Decoding immediate words
            switch (parameter) {
                case ROM_THEN:
                case ROM_DO:
                case ROM_BEGIN:
                    wordsPerLine = 5; // new line
                    break;
                case ROM_IF:
                case ROM_ELSE:
                case ROM_LOOP:
                case ROM_PLOOP:
                case ROM_UNTIL:
                case ROM_WHILE:
                case ROM_REPEAT:
                    address = address + 2; // drop 2 bytes
                    wordsPerLine = 5; // new line
                    break;
                case ROM_PRINT_STRING: // ."
                    address = address + printString(address, code);
                    code.append(" \"");
                    wordsPerLine = 5; // new line
                    break;
                case ROM_COMMENT: // (
                    address = address + printString(address, code);
                    code.append(" )");
                    wordsPerLine = 5; // new line
                    break;
                case ROM_STK_BYTE: // Stack Single Byte (ASCII)
                    code.append(" ").append((char) (getByte(address))); // Character
                    address++;
                    break;
                case ROM_STK_INT: // Stack Integer Number (2 bytes)
                    code.append(getInt(address));
                    address = address + 2;
                    break;
                case ROM_STK_FP: // Stack Floating Point Number (4 bytes)
                    printFloat(getWord(address), getWord(address + 2), code);
                    address = address + 4;
                    break;
                default: // do nothing
            }
            code.append(" "); // space between words
        }
        if ((getByte(cfa - 1) & 0x40) != 0) { // bit-6 = immediate flag
            code.append("IMMEDIATE");
        }
    }

// print JA String from address. Returns string size in bytes
    private int printString(int address, StringBuilder code) {
        int strlen = getWord(address);  // String Length
        address = address + 2;
        byte[] str = new byte[strlen];  // String Characters
        for (int i = 0; i < strlen; i++) {
            str[i] = getByte(address);
            address++;
        }
        code.append(" ").append(new String(str)); // print string with leading space
        return strlen + 2;                        // return number of bytes
    }

// Print JA Floating Point Number Format from address
    private void printFloat(int loWord, int hiWord, StringBuilder code) {
        // words: --------lo--------- ---------hi--------
        //  bits: s eeeeeee mmmm mmmm mmmm mmmm mmmm mmmm
        // s = sign, e = exponential (offseted by 65), m = mantissa (BCD digits)
        if ((hiWord & 0x8000) != 0) { // Print Number Sign (loWord bit15)
            code.append("-"); // it is a Negative Number
        }
        int exp = ((hiWord >> 8) & 0x7F) - 65;           // Normalized Exponent
        int mantissa = loWord | ((hiWord & 0xFF) << 16); // 6 BCD digits Mantissa

        if (exp >= -4 || exp <= 9) { // Print Normal format (-4 <= exponent <= 9)
            if (exp < 0) { // negative exponent
                code.append("."); // start with decimal point
            }
            while (exp < 0) { // print leading zeros
                code.append("0");
                exp++;
            }
            while (mantissa != 0) { // print all mantissa digits
                exp--;
                if (exp == 0) { // decimal point position
                    code.append(".");
                }
                code.append((char) (mantissa & 0xF00000) >> 20);
                mantissa = (mantissa & 0xFFFFF) << 4;
            }
            while (exp > 0) { // print remaining right sided zeros
                code.append("0");
                exp--;
            }
        } else { // exponential notation
//********************
            code.append("float_").append(hiWord).append("_").append(loWord); // ***********
//********************
        }
    }

// Get JA Integer Number from address
    private int getInt(int address) {
        int number = getWord(address);
        if ((number & 0x8000) != 0) { // if negative number..
            number = -(1 + (number ^ 0xFFFF));  // convert
        }
        return number;
    }

// Check if address is inside file range
    public boolean insideFile(int address) {
        return (address >= baseAddress)
                && (address < baseAddress + tapeFile.getParameter(JaTape.LENGTH));
    }

// Get byte from address
    private byte getByte(int address) {
        if (insideFile(address)) {
            return tapeFile.getDataByte(address - baseAddress);
        }
        return 0;
    }

// Get word (2-byte) from Address
    private int getWord(int address) {
        if (insideFile(address)) {
            return tapeFile.getDataWord(address - baseAddress);
        }
        return 0;
    }

}
