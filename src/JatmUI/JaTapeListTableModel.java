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
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

public class JaTapeListTableModel extends AbstractTableModel {

    // ------------------ STATIC --------------------
    private static final ImageIcon dictIcon, bytIcon, goodCrcIcon, badCrcIcon;
    static{
        dictIcon    = new ImageIcon(JaTapeListTableModel.class.getResource("resources/book.png"));
        bytIcon     = new ImageIcon(JaTapeListTableModel.class.getResource("resources/cog.png"));
        goodCrcIcon = new ImageIcon(JaTapeListTableModel.class.getResource("resources/ok.png"));
        badCrcIcon  = new ImageIcon(JaTapeListTableModel.class.getResource("resources/error.png"));
    }
    // ----------------------------------------------

    private List<JaTape> db;
    private final String[] columnNames;

    public JaTapeListTableModel(List<JaTape> db) {
        this.db = db;
        columnNames = new String[]{"CRC","Type","Name","Size","Address"};
    }

    public void setDb(List<JaTape> db) {
        this.db = db;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
            return db.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public Object getValueAt(int row, int col) {
        JaTape tapeFile = db.get(row);
        switch(col) {
            case 0: return tapeFile.crcOk() ? goodCrcIcon : badCrcIcon;
            case 1: return tapeFile.isDict() ? dictIcon : bytIcon;
            case 2: return tapeFile.getFilename();
            case 3: return tapeFile.getParameter(JaTape.LENGTH);
            case 4: return tapeFile.getParameter(JaTape.ADDRESS);
        }
        return null;
    }
}
