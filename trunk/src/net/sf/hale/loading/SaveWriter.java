/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package net.sf.hale.loading;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import net.minidev.json.JSONValue;

/**
 * An object providing for better formatting of output file than the standard JSONObject
 * @author Jared
 *
 */

public class SaveWriter {
	private static char[] DIGITS = "0123456789".toCharArray();
	
	private static void appendInt(int l, PrintWriter out) throws IOException {
//		out.append(new Integer(l).toString());
		if (l < 0) {
			out.append('-');
			l = -l;
		}
		do {
			out.append(DIGITS[(int) (l % 10)]);
			l = l / 10;
		} while (l > 0);
	}
	
	@SuppressWarnings("unchecked")
	private static void writeJSONValue(Object value, PrintWriter out, String indent) throws IOException {
		if (value == null) {
			out.append("null");
		} else if (value instanceof String) {
			out.append('"');
			JSONValue.escape((String) value, out);
			out.append('"');
		} else if (value instanceof Number) {
			if (value instanceof Double) {
				if (((Double) value).isInfinite())
					out.append("null");
				else
					out.append(value.toString());
			} else if (value instanceof Float) {
				if (((Float) value).isInfinite())
					out.append("null");
				else
					out.append(value.toString());
			} else {
				out.append(value.toString());
			}
		} else if (value instanceof Boolean) {
			out.append(value.toString());
		} else if (value instanceof Map<?, ?>) {
			SaveWriter.writeJSON((Map<String, Object>) value, out, indent);
		} else if (value.getClass().isArray()) {
			Class<?> arrayClz = value.getClass();
			Class<?> c = arrayClz.getComponentType();

			boolean needSep = false;
			String indentPlusOne = indent + "  ";
			
			out.append('[');
			out.println();
			out.append(indentPlusOne);
			
			if (c.isPrimitive()) {
				if (c == int.class) {
					for (int b : ((int[]) value)) {
						if (needSep)
							out.append(", ");
						else
							needSep = true;
						appendInt(b, out);
					}
				} else if (c == short.class) {
					for (int b : ((short[]) value)) {
						if (needSep)
							out.append(", ");
						else
							needSep = true;
						appendInt(b, out);
					}
				} else if (c == byte.class) {
					for (int b : ((byte[]) value)) {
						if (needSep)
							out.append(", ");
						else
							needSep = true;
						appendInt(b, out);
					}
				} else if (c == long.class) {
					for (long b : ((long[]) value)) {
						if (needSep)
							out.append(", ");
						else
							needSep = true;
						if (b < 0) {
							out.append('-');
							b = -b;
						}
						do {
							out.append(DIGITS[(int) (b % 10)]);
							b = b / 10;
						} while (b > 0);
					}
				} else if (c == float.class) {
					for (float b : ((float[]) value)) {
						if (needSep)
							out.append(", ");
						else
							needSep = true;
						out.append(Float.toString((float) b));
					}
				} else if (c == double.class) {
					for (double b : ((double[]) value)) {
						if (needSep)
							out.append(", ");
						else
							needSep = true;
						out.append(Double.toString((double) b));
					}
				} else if (c == boolean.class) {
					for (boolean b : ((boolean[]) value)) {
						if (needSep)
							out.append(", ");
						else
							needSep = true;
						if (b)
							out.append("true");
						else
							out.append("false");
					}
				}
			} else {
				for (Object o : ((Object[]) value)) {
					if (needSep)
						out.append(", ");
					else
						needSep = true;
					
					SaveWriter.writeJSONValue(o, out, indentPlusOne);
				}
			}
			
			out.println();
			out.append(indent);
			out.append(']');
		}
	}
	
	/**
	 * Writes the specified key value map to the specified print writer
	 * @param map the map to write
	 * @param out the print writer to write to
	 * @throws IOException
	 */
	
	private static void writeJSON(Map<String, ? extends Object> map, PrintWriter out, String indent) throws IOException {
		if (map == null) {
			out.append("null");
			return;
		}
		
		String indentPlusOne = indent + "  ";
		
		boolean first = true;
		
		out.append('{');
		out.println();
		
		for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {
			if (first)
				first = false;
			else {
				out.append(',');
				out.println();
			}
			
			out.append(indentPlusOne);
			String key = entry.getKey();
			if (key == null)
				out.append("null");
			else {
				out.append('"');
				JSONValue.escape(key, out);
				out.append('"');
			}
			out.append(" : ");
			
			SaveWriter.writeJSONValue(entry.getValue(), out, indentPlusOne);
		}
		out.println();
		out.append(indent);
		out.append('}');
	}
	
	/**
	 * Writes the specified key value map to the specified print writer
	 * @param map the map to write
	 * @param out the print writer to write to
	 * @throws IOException
	 */
	
	public static void writeJSON(Map<String, ? extends Object> map, PrintWriter out) throws IOException {
		writeJSON(map, out, "");
	}
}
