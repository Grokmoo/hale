/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.editor.scripteditor;

import java.io.IOException;
import java.io.Reader;

/**
 * An Buffered Iterator with helper functions for checking the contents
 * of a Reader
 * @author Matthias Mann
 */
public final class CharacterIterator {

    public static final int EOF = -1;

    private final Reader r;
    
    private char[] buffer;
    private int pos;
    private int start;
    private int end;
    private int marker;
    private boolean atEOF;

    /**
     * Creates a new CharacterIterator which will iterate over
     * the data specified by the Reader
     * @param r the Reader to iterator over.
     */
    
    public CharacterIterator(Reader r) {
        this.r = r;
        this.buffer = new char[4096];
        this.marker = -1;
    }

    /**
     * Returns the current number of characters that have been read by this Iterator
     * since the last clear() operation
     * @return the number of characters that have been read by this Iterator
     */
    
    public int length() {
        return pos - start;
    }

    /**
     * Returns a String representation of the characters read by this Iterator
     * since the last clear() operation
     * @return the String representation of characters read since the last clear() operation
     */
    
    public String getString() {
        return new String(buffer, start, length());
    }
    
    /**
     * Resets the amount of read data by this CharacterIterator.  After
     * this operation completes, calls to length() will return 0.  Note
     * that this operation does not affect the Reader that this CharacterIterator
     * is iterating over
     */

    public void clear() {
        start = pos;
        marker = -1;
    }

    /**
     * Return the integer character code of the next character returned by
     * the Reader.  This implementation skips Carriage Returns '\r' but not
     * newlines '\n', making it easily compatible with both Windows and Unix
     * style text.
     * @return the character code of the next character
     */
    
    public int peek() {
        if(pos < end || refill()) {
            char ch = buffer[pos];
            
            // skip carriage return character;
            if (ch == '\r') {
            	++pos;
                return peek();
            }
            return ch;
        }
        atEOF = true;
        return EOF;
    }

    /**
     * Pushes the last character read by peek() back into the buffer.  The next call
     * to peek() will return the same character as the previous call to peek().  Note
     * that if this method is called immediately after the clear() method, there
     * will be no character to push back and so nothing will be done.  Also note that the
     * EOF character can not be pushed back.
     */
    
    public void pushback() {
        if(pos > start && !atEOF) {
            pos--;
            marker = -1;
        }
    }
    
    /**
     * Reads all characters in the buffer until reaching a newline character, then stops.
     * 
     * After this method returns, the next call to peek() will return the first character
     * on the next line.
     */
    
    public void advanceToEOL() {
        for(;;) {
            int ch = peek();
            if(ch < 0 || ch == '\n') {
                return;
            }
            pos++;
        }
    }

    /**
     * Reads all characters in the buffer until reaching a nonJavaIdentifier character.
     * 
     * See {@link Character#isJavaIdentifierPart}
     */
    
    public void advanceIdentifier() {
        while(Character.isJavaIdentifierPart(peek())) {
            pos++;
        }
    }

    /**
     * Returns the next character code in the sequence of characters provided by the Reader.
     * 
     * @return the next character code in the sequence of characters
     */
    
    public int next() {
        int ch = peek();
        if(ch >= 0) {
            pos++;
        }
        return ch;
    }

    /**
     * Returns true if the last read character is contained in the specified String
     * 
     * @param characters the String to check for the last read character
     * @return true if and only if the last read character by this CharacterIterator is
     * contained in the specified String
     */
    
    public boolean check(String characters) {
        if(pos < end || refill()) {
            return characters.indexOf(buffer[pos]) >= 0;
        }
        return false;
    }

    /**
     * Sets the internal marker for this CharacterIterator at the current buffer position
     * if pushback is false or the last read character position if pushback is true.
     * 
     * The internal marker can be used to quickly return to a previously read character
     * and is also used for checking for keywords.
     * 
     * @param pushback whether the push back the marker location by 1.
     */
    
    public void setMarker(boolean pushback) {
        marker = pos;
        if(pushback && pos > start) {
            marker--;
        }
    }

    /**
     * Returns true if and only if the current marker position is at the current read
     * starting position.  This is the position at which the last clear() operation was called.
     * @return true if and only if the current marker position equals the current start position
     */
    
    public boolean isMarkerAtStart() {
        return marker == start;
    }
    
    /**
     * Resets the read position of this CharacterIterator to that last set by setMarker.
     * The next character read out will be the character at the marker's position.
     */
    
    public void rewindToMarker() {
        if(marker >= start) {
            pos = marker;
            marker = -1;
        }
    }

    /**
     * Checks if the text read since the last time the internal marker was set is
     * equal to any of the keywords in the specified keyword list.
     * @param list the list of keywords to check against
     * @return true if the text read since the internal marker was set is a keyword, false otherwise
     */
    
    public boolean isKeyword(KeywordList list) {
        return marker >= 0 && list.isKeyword(buffer, marker, pos - marker);
    }

    private void compact() {
        pos -= start;
        marker -= start;
        end -= start;
        if(pos > buffer.length*3/2) {
            char[] newBuffer = new char[buffer.length * 2];
            System.arraycopy(buffer, start, newBuffer, 0, end);
            buffer = newBuffer;
        } else if(end > 0) {
            System.arraycopy(buffer, start, buffer, 0, end);
        }
        start = 0;
    }

    private boolean refill() {
        compact();

        try {
            int read = r.read(buffer, end, buffer.length - end);
            if(read <= 0) {
                return false;
            }
            end += read;
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
