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

import java.awt.Graphics;

public class ViewScreenDialog extends javax.swing.JDialog {

    // ------------------ STATIC --------------------    
    private static final int SCREEN_MARGIN = 10;
    private static final int CHAR_SIZE = 8;
    private static final int PANEL_WIDTH = 32*CHAR_SIZE+2*SCREEN_MARGIN;
    private static final int PANEL_HEIGTH = 24*CHAR_SIZE+2*SCREEN_MARGIN;
    private static final CharacterSet romCharSet;
    static {
        romCharSet = new CharacterSet(); // Default ROM char Set 
    }
    // ----------------------------------------------
    
    private CharacterSet userCharSet;
    private final byte[] screen;
    
    /**
     * Creates new form ScreenDialog
     * @param parent
     * @param modal
     * @param data
     * @param charSet
     * @param filename
     */
    public ViewScreenDialog(java.awt.Frame parent, boolean modal, byte[] data, CharacterSet charSet, String filename) {
        super(parent, modal);
        initComponents();        
        this.setTitle("Screen Data [ " + filename + " ]");
        
        // fill screen with file data
        screen = new byte[1024];
        for(int i = 0; i < 1024; i++) {
            if(data.length > i) {
                screen[i] = data[i];
            } else {
                screen[i] = 0;
            }
        }        
        userCharSet = charSet;
        screenPanel.setSize(PANEL_WIDTH, PANEL_HEIGTH);
    }
    
    @Override 
    public void paint(Graphics g) {
        super.paint(g);
        
        CharacterSet currentCharSet = romCharSet;
        if(userCharCheckBox.isSelected()) {
            currentCharSet = userCharSet;
        }
        
        Graphics screenPanelGraph = screenPanel.getGraphics();

        for(int lin=0; lin<24; lin++) {
            for(int col=0; col<32; col++) {
                screenPanelGraph.drawImage(currentCharSet.getImage(screen[lin*32+col]),
                        SCREEN_MARGIN + CHAR_SIZE * col,
                        SCREEN_MARGIN + CHAR_SIZE * lin,
                        null);
            }
        }  
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        screenPanel = new javax.swing.JPanel();
        userCharCheckBox = new javax.swing.JCheckBox();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Screen View");
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(290, 252));

        screenPanel.setBackground(new java.awt.Color(0, 0, 0));
        screenPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        screenPanel.setMaximumSize(new java.awt.Dimension(276, 212));
        screenPanel.setMinimumSize(new java.awt.Dimension(276, 212));
        screenPanel.setPreferredSize(new java.awt.Dimension(276, 212));

        javax.swing.GroupLayout screenPanelLayout = new javax.swing.GroupLayout(screenPanel);
        screenPanel.setLayout(screenPanelLayout);
        screenPanelLayout.setHorizontalGroup(
            screenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        screenPanelLayout.setVerticalGroup(
            screenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 206, Short.MAX_VALUE)
        );

        userCharCheckBox.setMnemonic('U');
        userCharCheckBox.setText("User Char Set");
        userCharCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userCharCheckBoxActionPerformed(evt);
            }
        });

        closeButton.setMnemonic('C');
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userCharCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(screenPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(screenPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(userCharCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void userCharCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userCharCheckBoxActionPerformed
        this.repaint();
    }//GEN-LAST:event_userCharCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel screenPanel;
    private javax.swing.JCheckBox userCharCheckBox;
    // End of variables declaration//GEN-END:variables
}
