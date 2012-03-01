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

package net.sf.hale.editor.reference;

/**
 * A simple wrapper for the String constituting a script.  This allows the editor to use
 * the Referenceable framework in order to obtain the list of References to a script.
 * 
 * @author Jared Stephen
 *
 */

public class Script implements Referenceable {
	private final String id;
	
	/**
	 * Create a new Script object wrapping the script with the specified ID.
	 * @param id the ID of the script to wrap
	 */
	
	public Script(String id) {
		this.id = id;
	}
	
	/**
	 * Returns true if and only if this Script's ID String equals the
	 * other Object's default toString().
	 * @param other Object to compare
	 * @return true if and only if this Script's ID String equals the other
	 * Object's default toString()
	 */
	
	@Override public boolean equals(Object other) {
		return this.id.equals(other.toString());
	}
	
	/**
	 * Returns true if and only if this Script's ID String equals
	 * the supplied String.
	 * @param otherID the String to compare against
	 * @return true if and only if this Script's ID String equals
	 * the supplied String.
	 */
	
	public boolean equals(String otherID) {
		return id.equals(otherID);
	}
	
	/**
	 * Returns true if and only if this Script's ID String equals
	 * the supplied Script's ID String
	 * @param other the Script to compare against
	 * @return true if and only if this Script's ID String equals
	 * the supplied Script's ID String
	 */
	
	public boolean equals(Script other) {
		return id.equals(other.id);
	}
	
	@Override public String getReferenceType() { return "Script"; }
	@Override public String getID() { return id; }
	@Override public String toString() { return id; }
	
	/**
	 * Returns a new ReferenceList for this Script.
	 * See {@link ScriptReferenceList}.
	 * 
	 * @return a new ReferenceList for this Script.
	 */
	
	@Override public ReferenceList getReferenceList() {
		return new ScriptReferenceList(this);
	}
}
