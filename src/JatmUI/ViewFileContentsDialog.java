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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewFileContentsDialog extends javax.swing.JDialog {

    private final JaTape tape;
    private final JaVocabulary forthVocabulary;
    private final byte[] data;

    // Special Jupiter Ace like Character Fonts
    private static Font baseAceFont;
    private static Font aceFontSmall;
    private static Font aceFontMedium;
    private static Font aceFontLarge;
    static {
        InputStream fontStream = ViewFileContentsDialog.class.getResourceAsStream("resources/JupiterAce2.ttf");
        try {
            baseAceFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);

        } catch (FontFormatException | IOException ex) {
            Logger.getLogger(ViewFileContentsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

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
        forthVocabulary = new JaVocabulary(t); // Tape File Decompiler
        aceFontSmall  = baseAceFont.deriveFont(Font.PLAIN,8);  // Small Fonts
        aceFontMedium = baseAceFont.deriveFont(Font.PLAIN,12); // Medium Fonts
        aceFontLarge  = baseAceFont.deriveFont(Font.PLAIN,16); // Large Fonts

        initComponents();

        // Set initial Fonts
        vocTextArea.setFont(aceFontSmall);
        hexDumpTextArea.setFont(aceFontSmall);
        codeTextArea.setFont(aceFontSmall);

        // Set Dialog Title
        this.setTitle("File Contents [ " + t.getFilename()+" ]");

        // Populate frames
        showVocabulary();
        showCode();
        showHexDump();
    }

    /**
     * Show File Vocabulary
     */
    private void showVocabulary() {
        vocTextArea.append(invert(" "+tape.getFileType()+": ")+" "+tape.getFilename());
        vocTextArea.append(String.format(" %d bytes\n\n",
                tape.getParameter(JaTape.LENGTH)));
        vocTextArea.append(String.format("     Address: %04Xh  ( %5d )\n",
                tape.getParameter(JaTape.ADDRESS),
                tape.getParameter(JaTape.ADDRESS)));
        vocTextArea.append(String.format("Current Word: %04Xh  ( %5d )\n",
                tape.getParameter(JaTape.CURR_WRD),
                tape.getParameter(JaTape.CURR_WRD)));
        vocTextArea.append(String.format("     CURRENT: %04Xh  ( %5d )\n",
                tape.getParameter(JaTape.CURRENT),
                tape.getParameter(JaTape.CURRENT)));
        vocTextArea.append(String.format("     CONTEXT: %04Xh  ( %5d )\n",
                tape.getParameter(JaTape.CONTEXT),
                tape.getParameter(JaTape.CONTEXT)));
        vocTextArea.append(String.format("      VOCLNK: %04Xh  ( %5d )\n",
                tape.getParameter(JaTape.VOCLNK),
                tape.getParameter(JaTape.VOCLNK)));
        vocTextArea.append(String.format("Stack Bottom: %04Xh  ( %5d )\n\n",
                tape.getParameter(JaTape.STKBOT),
                tape.getParameter(JaTape.STKBOT)));

        if (tape.isDict()) { // Dictionary File Type (dict)
            vocTextArea.append(invert("   CFA and Word list   "));
            vocTextArea.append("\n");
            vocTextArea.append(forthVocabulary.getVlist());
        }
        vocTextArea.setEditable(false);  // Avoid user editing
        vocTextArea.setCaretPosition(0); // Move to text top
    }

    /**
     * Show file code listings
     */
    private void showCode() {
        if (tape.isDict()) {
            // Dictionary File Type (dict)
            codeTextArea.append(forthVocabulary.dictCodeListing());
        } else {
            // Binary File Type (byt)
            codeTextArea.append("; Jupiter Ace Binary File \"" +
                tape.getFilename() + "\"\n\n"
            );
            codeTextArea.append(String.format("        .org    %04Xh\n\n",
                tape.getParameter(JaTape.ADDRESS)
            ));
            codeTextArea.append(invert("*** Z80 Disassembler not implemented ***\n"));
        }

        codeTextArea.setEditable(false);  // Avoid user editing
        codeTextArea.setCaretPosition(0); // move to top of text
    }

    private String invert(String str) {
        char[] charArray = str.toCharArray();
        for(int i=0; i<charArray.length; i++) {
            charArray[i] |= 0x80; // Set bit 7
        }
        return new String(charArray);
    }

    /**
     * Show File Contents in Hexadecimal
     */
    private void showHexDump() {
        StringBuilder sbHex = new StringBuilder(); // Hex values buffer
        StringBuilder sbAsc = new StringBuilder(); // ascii chars buffer
        hexDumpTextArea.append(invert("ADRS") + "  ");
        hexDumpTextArea.append(invert("                     H E X                     ")+"  ");
        hexDumpTextArea.append(invert("      ASCII     ")+"\n");
        // 16 bytes per line
        int adr = tape.getParameter(JaTape.ADDRESS);
        for(int i = 0; i < data.length; i=i+16) {
            sbHex.setLength(0); // Clear Hex values string buffer
            sbAsc.setLength(0); // Clear Ascii chars string buffer
            sbHex.append(String.format("%04X:", adr)); // Address
            sbAsc.append("  "); // Ascii block separation
            for(int n = i; n < i+16; n++) {
                if( n < data.length) {
                    int c = data[n] & 0xFF; // get unsigned char value
                    sbHex.append(String.format(" %02X", c));
                    // filter lower ASCII characters like CR, TAB, NULL, etc..
                    if(c < 16) {
                        c += 16;
                    }
                    sbAsc.append((char)c);
                } else {
                    sbHex.append("   "); // Fill last line Hex gaps
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        vocScrollPane = new javax.swing.JScrollPane();
        vocTextArea = new javax.swing.JTextArea();
        codeScrollPane = new javax.swing.JScrollPane();
        codeTextArea = new javax.swing.JTextArea();
        hexDumpScrollPane = new javax.swing.JScrollPane();
        hexDumpTextArea = new javax.swing.JTextArea();
        CloseButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        SmallFontRadioButton = new javax.swing.JRadioButton();
        MediumFontRadioButton = new javax.swing.JRadioButton();
        LargeFontRadioButton = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("File Info");
        setIconImage(null);
        setIconImages(null);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(300, 100));
        setPreferredSize(new java.awt.Dimension(650, 500));

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

        CloseButton.setMnemonic('C');
        CloseButton.setText("Close");
        CloseButton.setToolTipText("");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonGroup1.add(SmallFontRadioButton);
        SmallFontRadioButton.setSelected(true);
        SmallFontRadioButton.setText("Small Font");
        SmallFontRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SmallFontRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(MediumFontRadioButton);
        MediumFontRadioButton.setText("Medium Font");
        MediumFontRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MediumFontRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(LargeFontRadioButton);
        LargeFontRadioButton.setText("Large Font");
        LargeFontRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LargeFontRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SmallFontRadioButton)
                .addGap(18, 18, 18)
                .addComponent(MediumFontRadioButton)
                .addGap(18, 18, 18)
                .addComponent(LargeFontRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(MediumFontRadioButton)
                .addComponent(SmallFontRadioButton)
                .addComponent(LargeFontRadioButton))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(CloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        dispose();
    }//GEN-LAST:event_CloseButtonActionPerformed

    private void SmallFontRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SmallFontRadioButtonActionPerformed
        // Set Small Fonts
        hexDumpTextArea.setFont(aceFontSmall);
        codeTextArea.setFont(aceFontSmall);
        vocTextArea.setFont(aceFontSmall);
    }//GEN-LAST:event_SmallFontRadioButtonActionPerformed

    private void LargeFontRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LargeFontRadioButtonActionPerformed
        // Set Large Fonts
        hexDumpTextArea.setFont(aceFontLarge);
        codeTextArea.setFont(aceFontLarge);
        vocTextArea.setFont(aceFontLarge);
    }//GEN-LAST:event_LargeFontRadioButtonActionPerformed

    private void MediumFontRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MediumFontRadioButtonActionPerformed
        // Set Medium Fonts
        hexDumpTextArea.setFont(aceFontMedium);
        codeTextArea.setFont(aceFontMedium);
        vocTextArea.setFont(aceFontMedium);
    }//GEN-LAST:event_MediumFontRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CloseButton;
    private javax.swing.JRadioButton LargeFontRadioButton;
    private javax.swing.JRadioButton MediumFontRadioButton;
    private javax.swing.JRadioButton SmallFontRadioButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JScrollPane codeScrollPane;
    private javax.swing.JTextArea codeTextArea;
    private javax.swing.JScrollPane hexDumpScrollPane;
    private javax.swing.JTextArea hexDumpTextArea;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane vocScrollPane;
    private javax.swing.JTextArea vocTextArea;
    // End of variables declaration//GEN-END:variables

}
