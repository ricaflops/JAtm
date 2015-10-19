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

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Ricardo
 */
public class BasicFileFilter extends FileFilter {
    String extension;
    String description;
    
    public BasicFileFilter(String ext, String descr) {
        extension = ext;
        description = descr;
    }
    
    @Override
    public boolean accept(File f) {
        if( f.isDirectory() )       // Allow looking in directories 
            return true;
  
        int i = f.getName().lastIndexOf('.');
        if(i<=0)                    // no file extension: not accepted
            return false;

        String ext = f.getName().substring(i+1);  // Get filename extension

        if(ext == null)             // no file extension: not accepted
            return false;

        return ext.equalsIgnoreCase(extension);
    }

    @Override
    public String getDescription() {
        return description;
    }
}
