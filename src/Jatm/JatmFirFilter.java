/*
 * Copyright (C) 2015 Ricardo
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

/**
 *
 * @author Ricardo
 */
public class JatmFirFilter {
    private static final int MAX_ORDER = 55; // maximum filter order
    private float[] buffer; // A circular buffer to store past values
    private int size;  // circular buffer size
    private int index; // current circular buffer index
    
    public JatmFirFilter(int order) {
        size = (order > 2)? order : 2; // check minimum order
        size = (order <= MAX_ORDER)? order : MAX_ORDER; // check maximum order
        buffer = new float[order];
        clear(); // clear circular buffer
    }
    
    public void clear() {
        for(int i=0; i<size; i++) {
            buffer[i] = 0F;
        }
        index = 0;
    }
    
    //  a simple moving average filter
    public float filter(float x) {
        float sum = 0F;
        buffer[index] = x; // set new sample in the circular buffer
        index++; // advance circular buffer index
        if(index >= size) {
            index = 0;
        }
        for(int i=0; i<size; i++) { // Add samples in circular buffer
            sum += buffer[i];
        }
        return sum / (float)size; // return samples average
    }
}
