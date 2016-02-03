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
import java.util.HashMap;
import java.util.Stack;

/*
 RAM Vocabulary Word Structure:
 0 to 63 bytes   Name    Word name   last char with bit-7 set
 2 bytes         Length  Length of Parameter Field + 7
 2 bytes         Link    Points to previous word Name Length Field
 1 byte          Name Length (bits 0-5): Name Length   bit 6: IMMEDIATE word flag
 2 bytes         Code        strBuf address to execute this word
 n bytes         Parameter   (n = Length Field - 7)
 */

/**
 * List Jupiter Ace file FORTH vocabulary contents and disassemble words
 * @author Ricardo F. Lopes
 */
public class JaVocabulary {
    private final JaTape tape;                            // The tape file
    private final Stack<Integer> vocStack;                // VOCABULARY words
    private static final HashMap<Integer, String> ROM_CFA_LIST;  // ROM CFA list
    private final HashMap<Integer, String> fileCfaList; // File CFA list
    private String vlist; // VLIST text

    private static final int MAX_WORDS_PER_LINE = 5; // Max Number of Words per line in code listing

    // Address offsets from a word name length position
    private static final int LENGTH_OFFSET = -4; // Word Length
    private static final int LINK_OFFSET   = -2; // Next Link offset from link address
    private static final int CFA_OFFSET    =  1; // CFA offset from link address
    private static final int PFA_OFFSET    =  3; // CFA offset from link address
    // Address ofsets for a VOCABULARY word
    private static final int TOPWRD_OFFSET =  3; // Top Word Link offset from link address
    private static final int VOCLNK_OFFSET =  6; // VOCLNK offset from link address

    // Word Types based on CFA (Code Field Address)
    private static final int DOCOLON  = 0x0EC3;
    private static final int CREATE   = 0x0FEC;
    private static final int VARIABLE = 0x0FF0;
    private static final int CONSTANT = 0x0FF5;
    private static final int DEFINER  = 0x1085;
    private static final int VOCAB    = 0x11B5;
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

    // ROM Vocabulary. Read from a txt file

    static {
        ROM_CFA_LIST = new HashMap<>();
        try (InputStream in = JaVocabulary.class.getResourceAsStream("resources/ROM_words.txt")) {
            BufferedReader buf = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = buf.readLine()) != null) {
                String parts[] = line.split(" ");              // Space delimited values
                ROM_CFA_LIST.put(parseInt(parts[0], 16), parts[1]); // Build Hash Map
            }
            in.close();
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    /**
     * Constructor
     * @param tapeFile Jupiter Ace tape file
     */
    public JaVocabulary(JaTape tapeFile) {
        tape = tapeFile;

        fileCfaList = new HashMap<>(); // Tape File words
        vocStack = new Stack<>();          // File Vocabularies

        if (tape.isDict()) {
            vocabularyScan();      // scan Vocabularies only if Dictionary file
            buildVlist();          // Build vlist string and CFA list
        }
    }

    public String getVlist() {
        return vlist;
    }

    /**
     * Build vocabulary list
     */
    private void vocabularyScan() {
        int link = tape.getParameter(JaTape.CURR_WRD); // newest word link in dictionary
        while(link > 0x2000) {                         // limit scan to RAM words only
            if( isVocabulary(link) ) {
                vocStack.push(link);                   // collect VOCABULARY words
            }
            link = getNextWord(link);                  // get next word link
        }
    }

    /**
     * Get Word Name
     * @param link
     * @return word name string
     */
    private String getWordName(int link) {
        byte nameLength = (byte) (tape.getMemByte(link) & 0x3F); // clear bits 6,7
        // Get Word Name Characters
        byte[] nameChars = new byte[nameLength];
        for (int i = 0; i < nameLength; i++) { // clear bit-7 of all chars
            nameChars[i] = (byte) (tape.getMemByte(link - 4 - nameLength + i) & 0x7F);
        }
        // Create Word Name String
        String name = new String(nameChars, StandardCharsets.UTF_8);
        return name ;
    }

    /**
     * Get next linked word link
     * @param link  name length field address of a vocabulary word
     * @return link to previous word in linking chain
     */
    private int getNextWord(int link) {
        return tape.getMemWord(link + LINK_OFFSET);
    }

    /**
     * get Code Field Value
     * @param link name length field address of a vocabulary word
     * @return contents of the Code Field
     */
    private int getCodeField(int link) {
        return tape.getMemWord(link + CFA_OFFSET);
    }

    /**
     * get Top Word link in a Vocabulary
     * @param link name length field address of a vocabulary word
     * @return link to newest word in Vocabulary
     */
    private int getVocabTopWordLink(int link) {
        return tape.getMemWord(link + TOPWRD_OFFSET);
    }

    /**
     * Check if a word is a VOCABULARY word
     * @param link name length field address of a vocabulary word
     * @return True if link points to a VOCABULARY word
     */
    private boolean isVocabulary(int link) {
        return getCodeField(link) == VOCAB; // check code field address
    }

    /**
     * @return Number of words in vocabulary
     */
    public int vocabularySize() {
        return fileCfaList.size();
    }

    /**
     * List linked words with it's CFA
     * @param link words in the link chain
     * @param out resulting String Buffer
     * @return number of words listed
     */
    private int listWords(int link, StringBuilder out ) {
        int count = 0;
        while( tape.validAddress(link) ) {
            fileCfaList.put(link + CFA_OFFSET, getWordName(link)); // build list of CFA in tape file
            out.append(String.format("%04Xh: ", link+CFA_OFFSET)); // CFA
            out.append(getWordName(link)).append("\n");            // Word name
            link = getNextWord(link);                              // get next word link
            count++;                                               // count words in dictionary
        }
        return count;
    }

    /**
     * Lists words in vocabulary. One word per line, all vocabularies
     * @return vocabulary list as a String
     */
    private void buildVlist() {
        StringBuilder out = new StringBuilder();
        int link;
        int wordCount = 0;
         // List Vocabulary Words
        for (Integer vocLink : vocStack) {
            out.append(String.format("\n%s DEFINITIONS\n",getWordName(vocLink)));
            link = getVocabTopWordLink(vocLink);
            wordCount += listWords(link, out );
        }

        // List FORTH Vocabulary words
        out.append("\nFORTH DEFINITIONS\n");
        link = tape.getParameter(JaTape.CURR_WRD);     // newest word link in dictionary
        wordCount += listWords(link, out);

        out.append("\nTotal: ").append(wordCount).append(" words in dictionary.");
        vlist = out.toString();
    }

    /**
     * Decompile a single Forth Word
     * @param link Link to the word to be decoded
     * @param out StringBuilder were decoding text will be appended
     */
    private void decodeWord(int link, StringBuilder out) {
        String wordName = getWordName(link); // get word Name
        int cfa = getCodeField(link);        // get word Code Field content
        int wordLength = tape.getMemWord(link + LENGTH_OFFSET) - 7;

        switch (cfa) {  // Append Word prefix according to code field
            case CREATE:   // CREATE name (size)
                out.append(String.format("CREATE %s ( %d bytes )", wordName, wordLength));
                break;
            case VARIABLE: // VARIABLE name
                out.append(String.format("VARIABLE %s", wordName));
                break;
            case CONSTANT: // xx CONSTANT name
                out.append(String.format("%d CONSTANT %s",getInt(link + PFA_OFFSET), wordName));
                break;
             case VOCAB:    // VOCABULARY name
                out.append(String.format("VOCABULARY %s", wordName));
                break;
            case DEFINER:  // DEFINER name <FORTHcode> DOES> ;
                out.append(String.format("DEFINER %s", wordName));
                decodeDoColon(link, out);
                break;
            case COMPILER:  // COMPILER name <FORTHcode> RUNS> ;
                out.append(String.format("COMPILER %s", wordName));
                decodeDoColon(link, out);
                break;
            case DOCOLON:    // : name <FORTHcode> ;
                out.append(String.format(": %s", wordName));
                decodeDoColon(link, out);
                break;
            default:       // : name ( CFA:xxxx ) ... (Unlistable word)
                out.append(String.format(": %s ( CFA:%04Xh )\n;", wordName, cfa) );
                break;
        }
        out.append("\n");    // Next line
    }

    /**
     * Decompile all words in a vocabulary
     * @param link
     * @param out
     */
    private void decodeVocab(int link, StringBuilder out) {
        Stack<Integer> wordStack = new Stack<>(); // list of words in vocabulary
        // build word list
         while( tape.validAddress(link) ) {
            wordStack.push(link);
            link = getNextWord(link);                      // get next word link                                     // count words in dictionary
        }
        // decode each words in vocabulary in proper order
        while( !wordStack.empty() ) {
            link = wordStack.pop();
            decodeWord(link, out);
        }
    }

    /**
     * Decompile all words in file
     * @return String code text
     */
    public String dictCodeListing() {
        StringBuilder codeListing = new StringBuilder(); // resulting String buffer

        //Decode words in FORTH vocabulary
        codeListing.append("FORTH DEFINITIONS\n");
        int wordLink = tape.getParameter(JaTape.CURR_WRD); // start with newest word in dictionary
        decodeVocab(wordLink, codeListing); // decode all linked words in vocabulary

        // Decode words in other vocabularies
        for (int i=vocStack.size()-1; i>=0; i--) { // Decode words in all vocabularies
            int vocLink = vocStack.get(i);
            codeListing.append(String.format("\n%s DEFINITIONS\n",getWordName(vocLink)));
            wordLink = getVocabTopWordLink(vocLink); // start with newest word in this vocabulary
            decodeVocab(wordLink, codeListing); // decode all linked words in vocabulary
        }
        return codeListing.toString();
    }

    /**
     * List DOCOLON word contents
     * @param out  is the output StringBuilder buffer
     * @param cfa is te Code Field Address of the word to disassemble
     */
    private void decodeDoColon(int link, StringBuilder out) {
        int parameterLength = tape.getMemWord(link + LENGTH_OFFSET) - 7; // Parameter Field Length
        int parameterField = link + PFA_OFFSET;    // Parameter Field Address
        int address = parameterField;              // current address
        int wordCount = MAX_WORDS_PER_LINE;        // Start with a new line now
        int tab = 1;                               // start with one space tab
        while (address < parameterField + parameterLength) {
            int cfa = tape.getMemWord(address); // get token to disassemble

            wordCount++; // Count words printed in this line
            // Check for ROM words that decreases identation
            if (    cfa == ROM_THEN   || cfa == ROM_WHILE     ||
                    cfa == ROM_REPEAT || cfa == ROM_SEMICOLON ||
                    cfa == ROM_LOOP   || cfa == ROM_PLOOP     ||
                    cfa == ROM_ELSE   || cfa == ROM_UNTIL     ||
                    cfa == ROM_DOES   || cfa == ROM_RUNS ) {
                tab--;                              // decrease tab
            }
            // Check for ROM words that starts a new line
            if (    wordCount >= MAX_WORDS_PER_LINE || cfa == ROM_THEN   ||
                    cfa == ROM_WHILE          || cfa == ROM_REPEAT ||
                    cfa == ROM_SEMICOLON      || cfa == ROM_LOOP   ||
                    cfa == ROM_PLOOP          || cfa == ROM_ELSE   ||
                    cfa == ROM_UNTIL          || cfa == ROM_DOES   ||
                    cfa == ROM_RUNS           || cfa == ROM_DO     ||
                    cfa == ROM_BEGIN          || cfa == ROM_IF ) {
                    wordCount = 0;                      // Reset Words per Line Counter
                out.append("\n");                   // Start a new line
                for(int i = 0; i < tab; i++) {      // Do Identation
                    out.append(" ");
                }
            }

            // Search CFA in Dictionary
            String wordString = fileCfaList.get(cfa);    // Search in file first..
            if (wordString == null) {
                wordString = ROM_CFA_LIST.get(cfa);      // ..than search in ROM
            }
            // is it a Number ?
            if( cfa == ROM_STK_INT || cfa == ROM_STK_FP ) {
                wordString = "";
            }
            // Unknow Word ?
            if (wordString == null) {
                wordString = String.format("[%04Xh]", cfa);
            }

            out.append(wordString);    // Append word name
            address += 2;              // point to next parameter field data

            // Complement immediate words decoding
            switch (cfa) {
                case ROM_PRINT_STRING: // ." (string)
                    out.append(" ");
                    address += appendString(out, address);
                    out.append("\"");
                    break;
                case ROM_COMMENT:      // ( (comment string)
                    out.append(" ");
                    address += appendString(out, address);
                    out.append(")");
                    break;
                case ROM_STK_BYTE:     // Stack Single Byte (ASCII)
                    out.append(" ").append((char) (tape.getMemByte(address))); // Character
                    address++;         // drop 1 bytes
                    break;
                case ROM_STK_INT:      // Stack Integer Number (2 bytes)
                    out.append(getInt(address));
                    address += 2;      // drop 2 bytes
                    break;
                case ROM_STK_FP:       // Stack Floating Point Number (4 bytes)
                    address += appendFloat(out, address);
                    break;
                case ROM_DOES:
                case ROM_RUNS:
                    address += 5;      // drop 5 bytes
                    break;
                default:               // do nothing
            }
            // Check for words with additional 2 byte parameter
            if(     cfa == ROM_IF    || cfa == ROM_ELSE  ||
                    cfa == ROM_WHILE || cfa == ROM_LOOP  ||
                    cfa == ROM_PLOOP || cfa == ROM_UNTIL ||
                    cfa == ROM_REPEAT ) {
                address += 2; // drop 2 bytes
            }
            // Check words that starts a new line after it
            if(     cfa == ROM_THEN         || cfa == ROM_DO      ||
                    cfa == ROM_BEGIN        || cfa == ROM_IF      ||
                    cfa == ROM_ELSE         || cfa == ROM_WHILE   ||
                    cfa == ROM_LOOP         || cfa == ROM_PLOOP   ||
                    cfa == ROM_UNTIL        || cfa == ROM_REPEAT  ||
                    cfa == ROM_PRINT_STRING || cfa == ROM_COMMENT ||
                    cfa == ROM_DOES         || cfa == ROM_RUNS ) {
                wordCount = MAX_WORDS_PER_LINE; // Start a new line
            }
            // Check for words that increase identation after it
            if(     cfa == ROM_DO    || cfa == ROM_BEGIN ||
                    cfa == ROM_IF    || cfa == ROM_ELSE  ||
                    cfa == ROM_WHILE || cfa == ROM_DOES  ||
                    cfa == ROM_RUNS ) {
                tab++;                             // increase identation
            }
            out.append(" ");                       // append a space between words
        }
        if ((tape.getMemByte(link) & 0x40) != 0) { // bit6 = immediate flag
            out.append("IMMEDIATE");
        }
    }

    /**
     * Decode a String
     * @param out Output StringBuilder buffer to append the string
     * @param address String location
     * @return string length in bytes
     */
    private int appendString(StringBuilder out, int address) {
        int strlen = tape.getMemWord(address);  // String Length
        address = address + 2;
        int c;
        for (int i = 0; i < strlen; i++) {
            c = tape.getMemByte(address) & 0xFF;
            out.append((char)c);
            address++;
        }
        return strlen + 2;                       // return number of bytes
    }

    /**
     * Decode a floating Point Number (4 bytes long)
     * This routine emulates the way Jupiter Ace prints FP numbers
     * Floating Point number structure:
     * ------ hiWord -----   ------ loWord -----
     * s eeeeeee mmmm mmmm   mmmm mmmm mmmm mmmm
     * s = sign, eeeeeee = exponential (offseted by 65)
     * mmmm = mantissa (6 BCD digits)
     * @param out Output StringBuilder buffer to append decoded number
     * @param address Floatinf point number structure location
     */
    private int appendFloat(StringBuilder out, int address) {
        int loWord = tape.getMemWord(address);
        int hiWord = tape.getMemWord(address + 2);
        if ((hiWord & 0x8000) != 0) {                     // Negative Number
            out.append("-");
        }

        int exp = ((hiWord & 0x7F00) >> 8) - 65;          // Normalized Exponent
        if((hiWord & 0x7F00) == 0) {                      // special case for 0.
            exp = 0;
        }
        int mantissa = ((hiWord & 0xFF) << 16) | loWord; // 6 BCD digits Mantissa

        int e = 0;                   // Print Exponential Format: 1.23456E10
        if (exp >= -4 && exp <= 9) { // Print Normal Format: 123.456
            e = exp;                 // decimal point position
            exp = 0;                 // flag Not Exponential Format
        }
        if(e < 0) {                  // print numbers like .000123
            out.append(".");
            while(e < -1) {
                out.append("0");
                e++;
            }
           e--;                      // force e = -1
        }

        do {                                        // print mantissa
            out.append((mantissa >> 20) & 0x0F); // print BCD digit
            if(e == 0) {                            // decimal point position
                out.append(".");
            }
            e--;                                    // decrement decimal place
            mantissa = (mantissa & 0xFFFFF) << 4;   // roll digits
        } while(mantissa != 0);
        e++;                         // revert last decrement during mantissa printing

        while(e > 0) {               // print trailing zeros
            out.append("0");
            e--;                     // decrement decimal place
            if(e == 0) {             // decimal point position
                out.append(".");
            }
        }

        if(exp != 0) {               // Print exponential value
            out.append("E").append(exp);
        }
        return 4;
    }

    /**
     * Decode an signed integer number (2 bytes)
     * @param address Integer number structure location
     * @return Decoded integer number
     */
    private int getInt(int address) {
        int number = tape.getMemWord(address);
        if ((number & 0x8000) != 0) {            // if negative number..
            number = -(1 + (number ^ 0xFFFF));   // ..convert
        }
        return number;
    }
}
