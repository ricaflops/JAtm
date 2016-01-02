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

import Jatm.JaVocabulary;
import Jatm.JaTape;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewFileContentsDialog extends javax.swing.JDialog {

    private static Font baseFont, aceFont;
    static {
        InputStream fontStream = ViewFileContentsDialog.class.getResourceAsStream("resources/JupiterAce2.ttf");
        try {
            baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (FontFormatException | IOException ex) {
            Logger.getLogger(ViewFileContentsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        aceFont = baseFont.deriveFont(Font.PLAIN,8);
    }
    
    private final JaTape tape;
    private final byte[] data;
    private final JaVocabulary forthVocabulary;

    /**
     * Creates new form HexViewDialog
     * @param parent
     * @param modal
     * @param t
     */
    public ViewFileContentsDialog(java.awt.Frame parent, boolean modal, JaTape t) {
        super(parent, modal);

        tape = t;
        data = t.getData();
        forthVocabulary = new JaVocabulary(t);

        initComponents();
        hexDumpTextArea.setFont(aceFont);
        codeTextArea.setFont(aceFont);
        vocTextArea.setFont(aceFont);
        this.setTitle("File Contents [ " + t.getFilename()+" ]");

        showVocabulary();
        showCode();
        showHexDump();
    }

    /**
     * Show File Vocabulary
     */
    private void showVocabulary() {
        if (tape.isDict()) {
            vocTextArea.append("   Dict: ");
        } else {
            vocTextArea.append("    Byt: ");
        }
        vocTextArea.append(tape.getFilename());

        vocTextArea.append(String.format("\nAddress: %04Xh (%d)\n   Size: %d bytes\n\n",
                tape.getParameter(JaTape.ADDRESS),
                tape.getParameter(JaTape.ADDRESS),
                tape.getParameter(JaTape.LENGTH)
        ));
        vocTextArea.append("WordLink | CURRENT | CONTEXT | VocLink |  Stack\n");
        vocTextArea.append(String.format("  %04Xh  |  %04Xh  |  %04Xh  |  %04Xh  |  %04Xh\n",
                tape.getParameter(JaTape.CURR_WRD),
                tape.getParameter(JaTape.CURRENT),
                tape.getParameter(JaTape.CONTEXT),
                tape.getParameter(JaTape.VOCLNK),
                tape.getParameter(JaTape.STKBOT)
        ));
        vocTextArea.append(String.format(" (%5d) | (%5d) | (%5d) | (%5d) | (%5d)\n",
                tape.getParameter(JaTape.CURR_WRD),
                tape.getParameter(JaTape.CURRENT),
                tape.getParameter(JaTape.CONTEXT),
                tape.getParameter(JaTape.VOCLNK),
                tape.getParameter(JaTape.STKBOT)
        ));

        if (tape.isDict()) {
            vocTextArea.append(String.format("\nVLIST ( %d words )\n",
                forthVocabulary.vocabularySize()
            ));
            vocTextArea.append(forthVocabulary.vlist());
            vocTextArea.append("\n\nCode Field Addresses:\n");
            vocTextArea.append(forthVocabulary.listCfa());
        }
        vocTextArea.setEditable(false);  // Avoid user editing
        vocTextArea.setCaretPosition(0); // move to top of text
    }

    /**
     * Show file code
     */
    private void showCode() {
        if (tape.isDict()) { // Dcit File Disassemble
            codeTextArea.append(forthVocabulary.listAllWords());
        } else {             // Z80 File Disassemble
            codeTextArea.append("; Jupiter Ace Binary File \"" +
                tape.getFilename() + "\"\n\n"
            );
            codeTextArea.append(String.format("        .org    %04Xh\n\n",
                tape.getParameter(JaTape.ADDRESS)
            ));
            codeTextArea.append("*** Z80 Disassembler not implemented ***\n");
        }
        codeTextArea.setEditable(false);  // Avoid user editing
        codeTextArea.setCaretPosition(0); // move to top of text
    }

    /**
     * Show File Contents in Hexadecimal
     */
    private void showHexDump() {
        StringBuilder sbHex = new StringBuilder(); // Hex values buffer
        StringBuilder sbAsc = new StringBuilder(); // ascii chars buffer
        hexDumpTextArea.append("ADRS                       HEX                               ASCII\n");
        hexDumpTextArea.append("----  -----------------------------------------------   ----------------\n");
        // 16 bytes per line
        int adr = tape.getParameter(JaTape.ADDRESS);
        for(int i = 0; i < data.length; i=i+16) {
            sbHex.setLength(0); // Clear Hex values string buffer
            sbAsc.setLength(0); // Clear Ascii chars string buffer
            sbHex.append(String.format("%04X:", adr)); // Address
            sbAsc.append("   "); // Ascii separation
            for(int n = i; n < i+16; n++) {
                if( n < data.length) {
                    int c = data[n] & 0xFF; // get unbsigned char value
                    sbHex.append(String.format(" %02X", data[n]));
                    if(data[n] == 10) { // replace CR by an equivalent glyph
                        c = 26; 
                    }
                    if(data[n] == 9) { // replace TAB by an equivalent glyph
                        c = 25;
                    }
                    sbAsc.append((char)c);
                } else {
                    sbHex.append("   "); // Fill last line gap
                }
            }
            adr = adr + 16;
            hexDumpTextArea.append(sbHex.toString()); // print HEx values
            hexDumpTextArea.append(sbAsc.toString()); // print Asci chars
            hexDumpTextArea.append("\n"); // End of Line
        }
        hexDumpTextArea.setEditable(false);  // Avoid user editing
        hexDumpTextArea.setCaretPosition(0); // move to top of text
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CloseButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        vocScrollPane = new javax.swing.JScrollPane();
        vocTextArea = new javax.swing.JTextArea();
        codeScrollPane = new javax.swing.JScrollPane();
        codeTextArea = new javax.swing.JTextArea();
        hexDumpScrollPane = new javax.swing.JScrollPane();
        hexDumpTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("File Info");
        setIconImage(null);
        setIconImages(null);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(300, 100));
        setPreferredSize(new java.awt.Dimension(650, 500));

        CloseButton.setMnemonic('C');
        CloseButton.setText("Close");
        CloseButton.setToolTipText("");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });

        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(120, 100));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(640, 480));

        vocScrollPane.setToolTipText("File Characteristics");

        vocTextArea.setColumns(32);
        vocTextArea.setLineWrap(true);
        vocTextArea.setRows(5);
        vocScrollPane.setViewportView(vocTextArea);

        jTabbedPane1.addTab("Vocabulary", new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/information.png")), vocScrollPane, "Vocabulary List"); // NOI18N

        codeScrollPane.setToolTipText("Code Listing");

        codeTextArea.setColumns(20);
        codeTextArea.setRows(5);
        codeScrollPane.setViewportView(codeTextArea);

        jTabbedPane1.addTab("Listing", new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/application_view_detail.png")), codeScrollPane, "Code Listing"); // NOI18N

        hexDumpScrollPane.setToolTipText("Hexadecimal Listing");

        hexDumpTextArea.setColumns(20);
        hexDumpTextArea.setRows(5);
        hexDumpScrollPane.setViewportView(hexDumpTextArea);

        jTabbedPane1.addTab("Hex Dump", new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/cog.png")), hexDumpScrollPane, "Hex and ASCII file dump"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(CloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CloseButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        dispose();
    }//GEN-LAST:event_CloseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CloseButton;
    private javax.swing.JScrollPane codeScrollPane;
    private javax.swing.JTextArea codeTextArea;
    private javax.swing.JScrollPane hexDumpScrollPane;
    private javax.swing.JTextArea hexDumpTextArea;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane vocScrollPane;
    private javax.swing.JTextArea vocTextArea;
    // End of variables declaration//GEN-END:variables


}
