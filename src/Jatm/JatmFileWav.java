/*
 * JatmFileWav - Wav file format for Jatm
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
import java.nio.file.Path;
import java.util.List;

/**
 * .WAV Wave file format for JAtm
 * @author Ricardo
 */
public final class JatmFileWav extends JatmFile {

    public JatmFileWav() {
        extension = "wav";
        description = "Wav files (*.wav)";
    }

    @Override
    public int load(Path filePath, List<JaTape> list) {
        int tapeCount = 0; // Count number of Jupiter Ace tape files found

        // Open WAV file for reading
        JatmWaveLoad audioIn = new JatmWaveLoad(); // Load Audio
        if (audioIn.open(filePath.toString())) {
            return 0; // an error occured when opening file
        }

        // Loop loading tapes from WAV file until EOF
        JaTape tape;
        do {
            tape = audioIn.load();
            if (tape != null) {
               list.add( tape );    // Add tape to tape list
                tapeCount++;
            }
        } while(tape != null);

        // Close WAV file
        audioIn.close();
        return tapeCount; // return number of tapes loaded
    }

    @Override
    public int save(Path filePath, List<JaTape> list, int[] selection) {
        if(selection.length <= 0) {
            return -1;  // Error: No tape selection
        }

        // Create WAV file
        JatmWaveSave audioOut = new JatmWaveSave();
        if( audioOut.open(filePath.toString()) ) {
            return -1; // Error writing WAV file
        }

        // Save each selected tape
        JaTape tape;
        for(int i=0; i < selection.length; i++) {
            tape = list.get(selection[i]);  // Get next tape to save
            audioOut.save(tape);
        }

        // Close WAV file
        audioOut.close();
        return 1;   // One WAV file saved
    }
}
