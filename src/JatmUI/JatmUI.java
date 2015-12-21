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

import Jatm.JaTape;
import Jatm.JatmFileHex;
import Jatm.JatmFileJac;
import Jatm.JatmFileBin;
import Jatm.JatmFileTap;
import Jatm.JatmFile;
import Jatm.JatmFileWav;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

/**
 * Jupiter Ace tape manager application GUI main class
 * @author Ricardo F. Lopes
 */
public class JatmUI extends javax.swing.JFrame {
    public static final String version = "0.7";
    
    List<JaTape> jaTapeList;                    // Tape Files List
    JaTapeListTableModel tapeListTableModel;    // GUI List Table
    JatmFileTap fileTap;                        // TAP tape
    JatmFileJac fileJac;                        // JAC tape
    JatmFileBin fileBin;                        // BIN tape
    JatmFileHex fileHex;                        // HEX tape
    JatmFileWav fileWav;                        // WAV tape
    FileFilter fileFilterTap;
    FileFilter fileFilterJac;
    FileFilter fileFilterBin;
    FileFilter fileFilterHex;
    FileFilter fileFilterWav;
    JFileChooser jatmFileChooser;               // File Chooser Filter
    ImageIcon jatmIcon;                         // Application Title Icon

    CharacterSet userCharSet;

    public JatmUI() {
        try { // Set System L&F (Native Look & Feel)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException
            | ClassNotFoundException
            | InstantiationException
            | IllegalAccessException e) {
        }

        userCharSet = new CharacterSet(); 
        
        jatmIcon = new ImageIcon("src/JatmUI/resources/jatm.png"); // Title Icon        
        jaTapeList = new ArrayList<>();  // List of Tape Files
        tapeListTableModel = new JaTapeListTableModel(jaTapeList); // GUI list
        
        // File Load/Save Objects
        fileTap = new JatmFileTap();
        fileJac = new JatmFileJac();
        fileBin = new JatmFileBin();
        fileHex = new JatmFileHex();
        fileWav = new JatmFileWav();
        
        // File Filters
        fileFilterTap = new BasicFileFilter(fileTap.getExtension(), fileTap.getDescription());
        fileFilterJac = new BasicFileFilter(fileJac.getExtension(), fileJac.getDescription());
        fileFilterBin = new BasicFileFilter(fileBin.getExtension(), fileBin.getDescription());
        fileFilterHex = new BasicFileFilter(fileHex.getExtension(), fileHex.getDescription());
        fileFilterWav = new BasicFileFilter(fileWav.getExtension(), fileWav.getDescription());
        
        // File Chooser
        jatmFileChooser = new JFileChooser();
        jatmFileChooser.setFileFilter(fileFilterTap); // Default Choose
        jatmFileChooser.addChoosableFileFilter(fileFilterJac);
        jatmFileChooser.addChoosableFileFilter(fileFilterBin);
        jatmFileChooser.addChoosableFileFilter(fileFilterHex);
        jatmFileChooser.addChoosableFileFilter(fileFilterWav);

        initComponents();
        
        setTitle("JAtm - version " + version);
        setIconImage(jatmIcon.getImage());   
    }
    
    // Right Text on Status Bar: shows the number of tape files in list
    private void setStatusBarRight(int files) {
        if(files == 0) {
            statusBarRightText.setText("No files");
            return;
        }
        if(files > 1) {
            statusBarRightText.setText(files + " files");
        } else{
            statusBarRightText.setText("One file");
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

        jPanel1 = new javax.swing.JPanel();
        toolBar = new javax.swing.JToolBar();
        newButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        renameButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        screenButton = new javax.swing.JButton();
        charactersButton = new javax.swing.JButton();
        inspectButton = new javax.swing.JButton();
        mapButton = new javax.swing.JButton();
        statusBar = new javax.swing.JPanel();
        statusBarLeftText = new javax.swing.JLabel();
        statusBarRightText = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tapeListTable = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        File = new javax.swing.JMenu();
        newList = new javax.swing.JMenuItem();
        openFile = new javax.swing.JMenuItem();
        saveFile = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        exit = new javax.swing.JMenuItem();
        Edit = new javax.swing.JMenu();
        editName = new javax.swing.JMenuItem();
        editAttributes = new javax.swing.JMenuItem();
        fixCrc = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        selectAllMenuItem = new javax.swing.JMenuItem();
        selectNoneMenuItem = new javax.swing.JMenuItem();
        invertSelectionMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        moveTop = new javax.swing.JMenuItem();
        moveUp = new javax.swing.JMenuItem();
        moveDown = new javax.swing.JMenuItem();
        moveBottom = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        removeItem = new javax.swing.JMenuItem();
        View = new javax.swing.JMenu();
        viewScreen = new javax.swing.JMenuItem();
        viewCharacters = new javax.swing.JMenuItem();
        viewInspect = new javax.swing.JMenuItem();
        viewMap = new javax.swing.JMenuItem();
        Setup = new javax.swing.JMenu();
        wavSaveSetup = new javax.swing.JMenuItem();
        wavLoadSetup = new javax.swing.JMenuItem();
        Help = new javax.swing.JMenu();
        JARsite = new javax.swing.JMenuItem();
        JAtmSourceCode = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        about = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(300, 300));

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/page_white.png"))); // NOI18N
        newButton.setToolTipText("Discard all loaded files");
        newButton.setFocusable(false);
        newButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearListActionPerformed(evt);
            }
        });
        toolBar.add(newButton);

        loadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/folder.png"))); // NOI18N
        loadButton.setToolTipText("Load file(s)");
        loadButton.setFocusable(false);
        loadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        loadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFileActionPerformed(evt);
            }
        });
        toolBar.add(loadButton);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/disk.png"))); // NOI18N
        saveButton.setToolTipText("Save selected files");
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileActionPerformed(evt);
            }
        });
        toolBar.add(saveButton);
        toolBar.add(jSeparator4);

        renameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/tag_blue_edit.png"))); // NOI18N
        renameButton.setToolTipText("Edit name");
        renameButton.setFocusable(false);
        renameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        renameButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        renameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editNameActionPerformed(evt);
            }
        });
        toolBar.add(renameButton);

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/cross.png"))); // NOI18N
        removeButton.setToolTipText("Remove from list");
        removeButton.setFocusable(false);
        removeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeItemActionPerformed(evt);
            }
        });
        toolBar.add(removeButton);
        toolBar.add(jSeparator6);

        screenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/Display.png"))); // NOI18N
        screenButton.setToolTipText("View as screen");
        screenButton.setFocusable(false);
        screenButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        screenButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        screenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewScreenActionPerformed(evt);
            }
        });
        toolBar.add(screenButton);

        charactersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/game.png"))); // NOI18N
        charactersButton.setToolTipText("View as character set");
        charactersButton.setFocusable(false);
        charactersButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        charactersButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        charactersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewCharactersActionPerformed(evt);
            }
        });
        toolBar.add(charactersButton);

        inspectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/View.png"))); // NOI18N
        inspectButton.setToolTipText("Inspect File Contents");
        inspectButton.setFocusable(false);
        inspectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        inspectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        inspectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewInspectActionPerformed(evt);
            }
        });
        toolBar.add(inspectButton);

        mapButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/script.png"))); // NOI18N
        mapButton.setToolTipText("View Memory Map");
        mapButton.setFocusable(false);
        mapButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        mapButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMapActionPerformed(evt);
            }
        });
        toolBar.add(mapButton);

        statusBarLeftText.setText("Jupiter Ace Tape Manager");

        statusBarRightText.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        statusBarRightText.setText("No Files");

        javax.swing.GroupLayout statusBarLayout = new javax.swing.GroupLayout(statusBar);
        statusBar.setLayout(statusBarLayout);
        statusBarLayout.setHorizontalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusBarLeftText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(statusBarRightText)
                .addContainerGap())
        );
        statusBarLayout.setVerticalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusBarLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusBarLeftText)
                    .addComponent(statusBarRightText)))
        );

        jScrollPane1.setMinimumSize(new java.awt.Dimension(300, 280));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 280));

        tapeListTable.setModel(tapeListTableModel);
        tapeListTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tapeListTable.setRowHeight(20);
        tapeListTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tapeListTable.getTableHeader().setReorderingAllowed(false);
        // Set narrow Column Widths for jatmIcon columns in table
        tapeListTable.getColumnModel().getColumn(0).setMaxWidth(36);
        tapeListTable.getColumnModel().getColumn(0).setMinWidth(36);
        tapeListTable.getColumnModel().getColumn(1).setMaxWidth(40);
        tapeListTable.getColumnModel().getColumn(1).setMinWidth(40);
        jScrollPane1.setViewportView(tapeListTable);
        tapeListTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        File.setMnemonic('F');
        File.setText("File");
        File.setToolTipText("");

        newList.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/page_white.png"))); // NOI18N
        newList.setMnemonic('C');
        newList.setText("Clear List");
        newList.setToolTipText("Discard all loaded files");
        newList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearListActionPerformed(evt);
            }
        });
        File.add(newList);

        openFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/folder.png"))); // NOI18N
        openFile.setMnemonic('L');
        openFile.setText("Load..");
        openFile.setToolTipText("Load file(s)");
        openFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFileActionPerformed(evt);
            }
        });
        File.add(openFile);

        saveFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/disk.png"))); // NOI18N
        saveFile.setMnemonic('S');
        saveFile.setText("Save..");
        saveFile.setToolTipText("Save selected files");
        saveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileActionPerformed(evt);
            }
        });
        File.add(saveFile);
        File.add(jSeparator2);

        exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/Turn_off.png"))); // NOI18N
        exit.setMnemonic('x');
        exit.setText("Exit");
        exit.setToolTipText("Close program and discard files");
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitActionPerformed(evt);
            }
        });
        File.add(exit);

        jMenuBar1.add(File);

        Edit.setMnemonic('E');
        Edit.setText("Edit");

        editName.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.CTRL_MASK));
        editName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/tag_blue_edit.png"))); // NOI18N
        editName.setMnemonic('N');
        editName.setText("Name..");
        editName.setToolTipText("Edit file name");
        editName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editNameActionPerformed(evt);
            }
        });
        Edit.add(editName);

        editAttributes.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.ALT_MASK));
        editAttributes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/page_white_edit.png"))); // NOI18N
        editAttributes.setMnemonic('H');
        editAttributes.setText("Header..");
        editAttributes.setToolTipText("Edit file header");
        editAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editHeaderActionPerformed(evt);
            }
        });
        Edit.add(editAttributes);

        fixCrc.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        fixCrc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/wand.png"))); // NOI18N
        fixCrc.setMnemonic('C');
        fixCrc.setText("Fix CRC");
        fixCrc.setToolTipText("Fix selected files checksum");
        fixCrc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixCrcActionPerformed(evt);
            }
        });
        Edit.add(fixCrc);
        Edit.add(jSeparator7);

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.setToolTipText("Select all files in list");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        Edit.add(selectAllMenuItem);

        selectNoneMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        selectNoneMenuItem.setText("Select None");
        selectNoneMenuItem.setToolTipText("Clear all selection");
        selectNoneMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNoneMenuItemActionPerformed(evt);
            }
        });
        Edit.add(selectNoneMenuItem);

        invertSelectionMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        invertSelectionMenuItem.setMnemonic('I');
        invertSelectionMenuItem.setText("Invert Selection");
        invertSelectionMenuItem.setToolTipText("Invert current selection");
        invertSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invertSelectionMenuItemActionPerformed(evt);
            }
        });
        Edit.add(invertSelectionMenuItem);
        Edit.add(jSeparator1);

        moveTop.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_UP, java.awt.event.InputEvent.ALT_MASK));
        moveTop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/Top.png"))); // NOI18N
        moveTop.setMnemonic('T');
        moveTop.setText("Move to Top");
        moveTop.setToolTipText("Move selection to list top");
        moveTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTopActionPerformed(evt);
            }
        });
        Edit.add(moveTop);

        moveUp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.ALT_MASK));
        moveUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/Up.png"))); // NOI18N
        moveUp.setMnemonic('U');
        moveUp.setText("Move Up");
        moveUp.setToolTipText("Move selection one position up in list");
        moveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpActionPerformed(evt);
            }
        });
        Edit.add(moveUp);

        moveDown.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.ALT_MASK));
        moveDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/Down.png"))); // NOI18N
        moveDown.setMnemonic('D');
        moveDown.setText("Move Down");
        moveDown.setToolTipText("Move selection one position down in list");
        moveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownActionPerformed(evt);
            }
        });
        Edit.add(moveDown);

        moveBottom.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_DOWN, java.awt.event.InputEvent.ALT_MASK));
        moveBottom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/Bottom.png"))); // NOI18N
        moveBottom.setMnemonic('B');
        moveBottom.setText("Move to Bottom");
        moveBottom.setToolTipText("Move selection to list bottom");
        moveBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveBottomActionPerformed(evt);
            }
        });
        Edit.add(moveBottom);
        Edit.add(jSeparator8);

        removeItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        removeItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/cross.png"))); // NOI18N
        removeItem.setMnemonic('R');
        removeItem.setText("Remove");
        removeItem.setToolTipText("Remove selected files from list");
        removeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeItemActionPerformed(evt);
            }
        });
        Edit.add(removeItem);

        jMenuBar1.add(Edit);

        View.setMnemonic('V');
        View.setText("View");
        View.setToolTipText("");

        viewScreen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        viewScreen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/Display.png"))); // NOI18N
        viewScreen.setMnemonic('S');
        viewScreen.setText("Screen..");
        viewScreen.setToolTipText("View File as Screen");
        viewScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewScreenActionPerformed(evt);
            }
        });
        View.add(viewScreen);

        viewCharacters.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        viewCharacters.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/game.png"))); // NOI18N
        viewCharacters.setMnemonic('C');
        viewCharacters.setText("Characters..");
        viewCharacters.setToolTipText("View File as Character Set");
        viewCharacters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewCharactersActionPerformed(evt);
            }
        });
        View.add(viewCharacters);

        viewInspect.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.ALT_MASK));
        viewInspect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/View.png"))); // NOI18N
        viewInspect.setMnemonic('I');
        viewInspect.setText("Inspect Content..");
        viewInspect.setToolTipText("View file Contents");
        viewInspect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewInspectActionPerformed(evt);
            }
        });
        View.add(viewInspect);

        viewMap.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_MASK));
        viewMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/script.png"))); // NOI18N
        viewMap.setText("Memory Map..");
        viewMap.setToolTipText("View Memory Map");
        viewMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMapActionPerformed(evt);
            }
        });
        View.add(viewMap);

        jMenuBar1.add(View);

        Setup.setMnemonic('S');
        Setup.setText("Setup");

        wavSaveSetup.setText("WAV Save..");
        wavSaveSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wavSaveSetupActionPerformed(evt);
            }
        });
        Setup.add(wavSaveSetup);

        wavLoadSetup.setText("WAV Load..");
        wavLoadSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wavLoadSetupActionPerformed(evt);
            }
        });
        Setup.add(wavLoadSetup);

        jMenuBar1.add(Setup);

        Help.setMnemonic('H');
        Help.setText("Help");

        JARsite.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.SHIFT_MASK));
        JARsite.setText("Jupiter Ace Resource Archive");
        JARsite.setToolTipText("www.jupiter-ace.co.uk");
        JARsite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JARsiteActionPerformed(evt);
            }
        });
        Help.add(JARsite);

        JAtmSourceCode.setText("JAtm source code");
        JAtmSourceCode.setToolTipText("JAtm source code at GitHub repository");
        JAtmSourceCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JAtmSourceCodeActionPerformed(evt);
            }
        });
        Help.add(JAtmSourceCode);
        Help.add(jSeparator3);

        about.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        about.setIcon(new javax.swing.ImageIcon(getClass().getResource("/JatmUI/resources/information.png"))); // NOI18N
        about.setMnemonic('A');
        about.setText("About JAtm..");
        about.setToolTipText("");
        about.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutActionPerformed(evt);
            }
        });
        Help.add(about);

        jMenuBar1.add(Help);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void msgNoSelectionError() {
        JOptionPane.showMessageDialog(this,
        "No file selected",
        "Error",
        JOptionPane.ERROR_MESSAGE);
    }    
    
    private void loadFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadFileActionPerformed
        int result;
        
        jatmFileChooser.setMultiSelectionEnabled(true); // Multiple files selection

        if(jatmFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File[] files = jatmFileChooser.getSelectedFiles();
            for (File file : files) {
                String filename = file.getName();
                String ext = filename.substring(filename.lastIndexOf(".")+1,filename.length());
                
                JatmFile jatmFile = fileBin; // BIN file load
                if( ext.equalsIgnoreCase(fileTap.getExtension()) ) { // TAP file load
                    jatmFile = fileTap;
                }
                if( ext.equalsIgnoreCase(fileJac.getExtension()) ) { // JAC file load
                    jatmFile = fileJac;
                }
                if( ext.equalsIgnoreCase(fileHex.getExtension()) ) { // HEX file load
                    jatmFile = fileHex;
                }
                if( ext.equalsIgnoreCase(fileWav.getExtension()) ) { // WAV file load
                    jatmFile = fileWav;
                } 
                
                result = jatmFile.load(file.toPath(), jaTapeList);
                if(result <= 0) { // an error occured
                    JOptionPane.showMessageDialog(this,
                            "Error while loading file " + filename,
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE);
                    break;
                } else {
                    
                }
            }          
            tapeListTableModel.fireTableDataChanged(); // refresh table draw
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        setStatusBarRight(jaTapeList.size());
    }//GEN-LAST:event_loadFileActionPerformed

    private void exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitActionPerformed
        if(!jaTapeList.isEmpty()) {
            int action;
            action = JOptionPane.showConfirmDialog(this, 
                    "Do you really want to quit?", 
                    "Confirm Exit",
                    JOptionPane.OK_CANCEL_OPTION);
            if(action != JOptionPane.OK_OPTION) {
                return;
            }
        }
        System.exit(0);
    }//GEN-LAST:event_exitActionPerformed

    private void aboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutActionPerformed
        ImageIcon icon = new ImageIcon("src/images/jatm.png");
        JOptionPane.showMessageDialog(this,
                "JAtm - The Jupiter Ace tape manager version " + version
                +"\nhttp://www.jupiter-ace.co.uk \n\n"
                +"Copyright \u00a9 2015  Ricardo Fernandes Lopes.\n\n"
                
    +"This program is free software: you can redistribute it and/or modify\n"
    +"it under the terms of the GNU General Public License as published by\n"
    +"the Free Software Foundation, either version 3 of the License, or\n"
    +"(at your option) any later version.\n\n"

    +"This program is distributed in the hope that it will be useful,\n"
    +"but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
    +"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
    +"GNU General Public License for more details.\n\n"

    +"You should have received a copy of the GNU General Public License\n"
    +"along with this program.  If not, see <http://www.gnu.org/licenses/>.\n\n"              
                
                +"JAtm includes or uses:\n"
                +"- Silk Icon Set http://www.famfamfam.com/lab/icon/\n\n"
                +"Java runtime version: " + System.getProperty("java.version"),
                "About JAtm",
                JOptionPane.INFORMATION_MESSAGE, icon);
    }//GEN-LAST:event_aboutActionPerformed

    private void editNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editNameActionPerformed
        int row = tapeListTable.getSelectedRow();
        if(row >= 0) { // at least one selection
            JaTape tape = jaTapeList.get( row );
            String result = JOptionPane.showInputDialog(this,
                    "Rename " + tape.getFilename() + " to:",
                    tape.getFilename());
            if(result != null) {
                tape.setFilename(result);
                tape.fixCrc();
                tapeListTable.updateUI();
            }        
        } else { // Abort if no user selection
            msgNoSelectionError();
        }
    }//GEN-LAST:event_editNameActionPerformed

    private void moveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpActionPerformed
        int[] selection = tapeListTable.getSelectedRows(); // Selection list
        int row;
        JaTape tape;
        
        if(selection.length > 0) { // Check if anything is selected
            if(selection[0] > 0) { // Check first row selection
                for (int i=0; i<selection.length; i++) { // move up all selection
                    row = selection[i];   // get selection index
                    selection[i] = row-1; // update selection index
                    tape = jaTapeList.get( row ); // get selected object
                    jaTapeList.set(row, jaTapeList.get(row-1) ); // move next object up
                    jaTapeList.set(row-1, tape ); // place object in new position
                }
                tapeListTable.clearSelection();
                tapeListTableModel.fireTableDataChanged(); // refresh table draw 
                for(int i=0; i<selection.length; i++) { // Update selection in new position
                    row = selection[i];
                    tapeListTable.addRowSelectionInterval(row, row);
                }
            }
        } else {
            msgNoSelectionError(); // No files selected Error
        } 
    }//GEN-LAST:event_moveUpActionPerformed

    private void clearListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearListActionPerformed
        if(jaTapeList.size()>0) {
            int action;
            action = JOptionPane.showConfirmDialog(this, 
                    "Do you really want to clear all list?", 
                    "Confirm Clear",
                    JOptionPane.OK_CANCEL_OPTION);
            if(action == JOptionPane.OK_OPTION) {
                jaTapeList.clear();
                tapeListTableModel.fireTableDataChanged(); // refresh table draw
                setStatusBarRight(jaTapeList.size());
            }
        }
    }//GEN-LAST:event_clearListActionPerformed

    private void editHeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editHeaderActionPerformed
        int row = tapeListTable.getSelectedRow();
        if(row >= 0) { // At least one selection
            EditHeaderDialog dialog;
            dialog = new EditHeaderDialog(this,true,jaTapeList.get(row));
            dialog.setVisible(true);
            tapeListTable.updateUI();
        } else { // Abort if no selection
            msgNoSelectionError();
        }
    }//GEN-LAST:event_editHeaderActionPerformed

    private void fixCrcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixCrcActionPerformed
        int[] selection = tapeListTable.getSelectedRows();
        if(selection.length > 0) { // at least one selection
            for(int i = 0; i < selection.length; i++) { // Fix all selected files
                jaTapeList.get(selection[i]).fixCrc();
            }
            tapeListTable.updateUI();
        } else { // Abort if no selection
            msgNoSelectionError();
        }
    }//GEN-LAST:event_fixCrcActionPerformed

    private void moveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownActionPerformed
        int[] selection = tapeListTable.getSelectedRows(); // Selection list
        int row;
        JaTape tape;
        
        if(selection.length > 0) { // Check if anything is selected
            if(selection[selection.length-1] < jaTapeList.size()-1) { // Check last row selection
                for (int i=selection.length-1; i >= 0; i--) { // MOve All selection down
                    row = selection[i];     // get current selection index
                    selection[i] = row + 1; // update selection index
                    tape = jaTapeList.get( row ); // get selected object
                    jaTapeList.set(row, jaTapeList.get(row+1) ); // move up object below
                    jaTapeList.set(row+1, tape ); // place object in new position
                }
                tapeListTable.clearSelection();
                tapeListTableModel.fireTableDataChanged(); // refresh table draw
                for(int i=0; i<selection.length; i++) { // Update selection in new position
                    row = selection[i];
                    tapeListTable.addRowSelectionInterval(row, row);
                }
            }
        } else {
            msgNoSelectionError(); // No files selected Error
        }        
    }//GEN-LAST:event_moveDownActionPerformed

    private void moveTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveTopActionPerformed
        int[] selection = tapeListTable.getSelectedRows(); // Selection list
        int row;
        JaTape tape;
        
        if(selection.length > 0) { // Check if anything is selected
            int firstSelection = selection[0];
            if(firstSelection > 0) { // Check first row selection
                // Move Up in steps
                for(int line=0; line<firstSelection; line++) {
                    // Move up one line at a time
                    for (int i=0; i<selection.length; i++) {
                        row = selection[i];
                        selection[i] = row-1; // update selection index
                        tape = jaTapeList.get(row); // get selected object
                        jaTapeList.set(row, jaTapeList.get(row-1) ); // move down upper object
                        jaTapeList.set(row-1, tape); // place object in new position
                    }
                }
                tapeListTable.clearSelection();
                tapeListTableModel.fireTableDataChanged(); // refresh table draw
                for(int i=0; i<selection.length; i++) { // Update selection in new position
                    row = selection[i];
                    tapeListTable.addRowSelectionInterval(row, row);
                }
            }
        } else {
            msgNoSelectionError(); // No files selected Error
        } 
    }//GEN-LAST:event_moveTopActionPerformed

    private void moveBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveBottomActionPerformed
        int[] selection = tapeListTable.getSelectedRows(); // Selection list
        int row;
        JaTape tape;
        
        if(selection.length > 0) { // Check if anything is selected
            int lastSelection = selection[selection.length-1];
            if(lastSelection < jaTapeList.size()-1) { // Check last row selection
                // Move Down in steps
                for(int line = lastSelection; line<jaTapeList.size()-1; line++) {
                    // Move down one line at a time
                    for (int i=selection.length-1; i >= 0; i--) {
                        row = selection[i];
                        selection[i] = row + 1;
                        tape = jaTapeList.get(row); // get selected object
                        jaTapeList.set(row, jaTapeList.get(row+1) ); // move next object up
                        jaTapeList.set(row+1, tape); // place object in new position
                    }
                }
                tapeListTable.clearSelection();
                tapeListTableModel.fireTableDataChanged(); // refresh table draw
                for(int i=0; i<selection.length; i++) { // Update selection in new position
                    row = selection[i];
                    tapeListTable.addRowSelectionInterval(row, row);
                }
            }
        } else {
            msgNoSelectionError(); // No files selected Error
        } 
    }//GEN-LAST:event_moveBottomActionPerformed

    private void removeItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeItemActionPerformed
        int[] selection = tapeListTable.getSelectedRows(); // Selection list
       
        if(selection.length > 0) { // Check if anything is selected
            for (int row=selection.length-1; row>=0; row--) {
                jaTapeList.remove(selection[row]); // delete selected rows
            }
            tapeListTable.clearSelection();
            tapeListTableModel.fireTableDataChanged(); // refresh table draw
            setStatusBarRight(jaTapeList.size()); // Update Status Bar
        } else {
            msgNoSelectionError(); // No files selected Error
        }
    }//GEN-LAST:event_removeItemActionPerformed

    private void saveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileActionPerformed
        int[] selection = tapeListTable.getSelectedRows();
        if(selection.length <= 0) { // No files selected to save
            msgNoSelectionError();
            return;
        }
        
        jatmFileChooser.setMultiSelectionEnabled(false);
        jatmFileChooser.setSelectedFile(new File("")); // no default filename

        if(jatmFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // Cancelled by User
        }

        String textPath = jatmFileChooser.getSelectedFile().toString();
        String ext;
        // Check if there is a tape extension
        if(textPath.lastIndexOf(".") > 0
                && (textPath.length()-textPath.lastIndexOf(".")) == 4) {
            // There is a 3 char extension
            ext = textPath.substring(textPath.lastIndexOf(".")+1);
        } else {
            // No tape extension: Append one
            ext = fileTap.getExtension(); // Default extension
            FileFilter filter = jatmFileChooser.getFileFilter();
            if(filter == fileFilterJac) {
                ext = fileJac.getExtension();
            }
            if(filter == fileFilterBin) {
                ext = fileBin.getExtension();
            }
            if(filter == fileFilterHex) {
                ext = fileHex.getExtension();
            }
            if(filter == fileFilterWav) {
                ext = fileWav.getExtension();
            }
            textPath += "." + ext; // Append missing extension
        }

        // Check Overwrite
        Path path = Paths.get(textPath);
        if(Files.exists(path)) {
            if(JOptionPane.showConfirmDialog(null,
                    "Overwrite existing file?\n"+path.getFileName().toString(),
                    "File Already Exists",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
        }

        JatmFile jatmFile = null;
        Boolean isMultiTapeFile = false; // File Format accepts more than one tape
        
        if( ext.equalsIgnoreCase(fileTap.getExtension()) ) { // TAP file save
            jatmFile = fileTap;
            isMultiTapeFile = true;
        }
        if( ext.equalsIgnoreCase(fileBin.getExtension()) ) { // BIN file save
            jatmFile = fileBin;
        }
        if( ext.equalsIgnoreCase(fileJac.getExtension()) ) { // JAC file save
            jatmFile = fileJac;
        }
        if( ext.equalsIgnoreCase(fileHex.getExtension()) ) { // HEX file save
            jatmFile = fileHex;
        }
        if( ext.equalsIgnoreCase(fileWav.getExtension()) ) { // WAV file save
            jatmFile = fileWav;
            isMultiTapeFile = true;
        }

        if(jatmFile != null) { // Valid File Extension
            if(isMultiTapeFile || selection.length==1) {
                // Save tape
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                jatmFile.save(path, jaTapeList, selection);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else { // Multiple Tape files not allowed with selected tape format 
                JOptionPane.showMessageDialog(this,
                        "File Format: ."+ext
                                +"\ndo not accept multiple tape files",
                        "Save Error: Multiple Selection",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else { // Invalid File Extension
            JOptionPane.showMessageDialog(this,
                    "Unknow file format: ."+ext,
                    "Save Error: Unknow Format",
                    JOptionPane.ERROR_MESSAGE);
        }
        setStatusBarRight(jaTapeList.size());
    }//GEN-LAST:event_saveFileActionPerformed

    private void viewCharactersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewCharactersActionPerformed
        int[] selection = tapeListTable.getSelectedRows();
        if(selection.length > 0) {
            JaTape tape = jaTapeList.get( selection[0] );
            ViewCharacterSetDialog dialog;
            dialog = new ViewCharacterSetDialog(this, true, tape.getData(), 0, userCharSet, tape.getFilename());
            dialog.setVisible(true);            
        } else { // No files selected to save
            msgNoSelectionError();
        }

    }//GEN-LAST:event_viewCharactersActionPerformed

    private void viewScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewScreenActionPerformed
        int[] selection = tapeListTable.getSelectedRows();
        if(selection.length > 0) {
            JaTape tape = jaTapeList.get( selection[0] );
            ViewScreenDialog dialog;
            dialog = new ViewScreenDialog(this,true,tape.getData(),userCharSet,tape.getFilename());
            dialog.setVisible(true);           
        } else { // No files selected to save
            msgNoSelectionError();
        }
    }//GEN-LAST:event_viewScreenActionPerformed

    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
        tapeListTable.selectAll();
    }//GEN-LAST:event_selectAllMenuItemActionPerformed

    private void selectNoneMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNoneMenuItemActionPerformed
        if(tapeListTable.getRowCount() < 1) {
            return;
        }
        tapeListTable.removeRowSelectionInterval(0, tapeListTable.getRowCount()-1);
    }//GEN-LAST:event_selectNoneMenuItemActionPerformed

    private void invertSelectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertSelectionMenuItemActionPerformed
        int[] selection = tapeListTable.getSelectedRows();
        tapeListTable.selectAll();
        for (int prevSel : selection) {
            tapeListTable.removeRowSelectionInterval(prevSel, prevSel);
        }
    }//GEN-LAST:event_invertSelectionMenuItemActionPerformed

    private void viewMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMapActionPerformed
        int[] selection = tapeListTable.getSelectedRows();    // Get User Selection
        if(selection.length > 0) {
            ViewMapDialog dialog;
            dialog = new ViewMapDialog(this, true, jaTapeList.get( selection[0] ));
            dialog.setVisible(true);
        } else { // Abort if no selection
            msgNoSelectionError();
        }
    }//GEN-LAST:event_viewMapActionPerformed

    private void viewInspectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewInspectActionPerformed
        int[] selection = tapeListTable.getSelectedRows();    // Get User Selection
        if(selection.length > 0) {
            ViewInspectDialog dialog;
            dialog = new ViewInspectDialog(this, true, jaTapeList.get( selection[0] ));
            dialog.setVisible(true);
        } else { // Abort if no selection
            msgNoSelectionError();
        }
    }//GEN-LAST:event_viewInspectActionPerformed

    private void JARsiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JARsiteActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("http://www.jupiter-ace.co.uk"));
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(JatmUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_JARsiteActionPerformed

    private void wavLoadSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wavLoadSetupActionPerformed
        JDialog dialog = new LoadWavParamsDialog(this,true);
        dialog.setVisible(true);
    }//GEN-LAST:event_wavLoadSetupActionPerformed

    private void JAtmSourceCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JAtmSourceCodeActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/ricaflops/JAtm"));
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(JatmUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_JAtmSourceCodeActionPerformed

    private void wavSaveSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wavSaveSetupActionPerformed
        SaveWavParamsDialog dialog = new SaveWavParamsDialog(this,true);
        dialog.setVisible(true);
    }//GEN-LAST:event_wavSaveSetupActionPerformed

    /**
     * @param args the command line arguments
     */
    
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JatmUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu Edit;
    private javax.swing.JMenu File;
    private javax.swing.JMenu Help;
    private javax.swing.JMenuItem JARsite;
    private javax.swing.JMenuItem JAtmSourceCode;
    private javax.swing.JMenu Setup;
    private javax.swing.JMenu View;
    private javax.swing.JMenuItem about;
    private javax.swing.JButton charactersButton;
    private javax.swing.JMenuItem editAttributes;
    private javax.swing.JMenuItem editName;
    private javax.swing.JMenuItem exit;
    private javax.swing.JMenuItem fixCrc;
    private javax.swing.JButton inspectButton;
    private javax.swing.JMenuItem invertSelectionMenuItem;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton mapButton;
    private javax.swing.JMenuItem moveBottom;
    private javax.swing.JMenuItem moveDown;
    private javax.swing.JMenuItem moveTop;
    private javax.swing.JMenuItem moveUp;
    private javax.swing.JButton newButton;
    private javax.swing.JMenuItem newList;
    private javax.swing.JMenuItem openFile;
    private javax.swing.JButton removeButton;
    private javax.swing.JMenuItem removeItem;
    private javax.swing.JButton renameButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveFile;
    private javax.swing.JButton screenButton;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem selectNoneMenuItem;
    private javax.swing.JPanel statusBar;
    private javax.swing.JLabel statusBarLeftText;
    private javax.swing.JLabel statusBarRightText;
    private javax.swing.JTable tapeListTable;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenuItem viewCharacters;
    private javax.swing.JMenuItem viewInspect;
    private javax.swing.JMenuItem viewMap;
    private javax.swing.JMenuItem viewScreen;
    private javax.swing.JMenuItem wavLoadSetup;
    private javax.swing.JMenuItem wavSaveSetup;
    // End of variables declaration//GEN-END:variables

}
