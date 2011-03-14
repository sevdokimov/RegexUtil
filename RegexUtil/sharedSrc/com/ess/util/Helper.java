package com.ess.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Helper {
	
	private Helper() {
	}

    public static String toHtml(String s) {
		StringBuilder res = new StringBuilder();
		toHtml(res, s);
		return res.toString();
	}
	
	public static void toHtml(StringBuilder res, String s) {
        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i);
            switch (a) {
                case '<':
                    res.append("&lt;");
                    break;
                case '>':
                    res.append("&gt;");
                    break;
                case '&':
                    res.append("&amp;");
                    break;
                case '/':
                    res.append("&#47;");
                    break;
                case '\r':
                	break;
                case '\n':
                	res.append("<br>");
                	break;
                default:
                    res.append(a);
            }
        }
	}
	
	public static String toHtmlDocument(String s) {
        StringBuilder res = new StringBuilder("<html>");
        toHtml(res, s);
        res.append("</html>");
        return res.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <A, B> Map<A, B> createUnmMap(Object[] data) {
		return Collections.unmodifiableMap((Map<A, B>)createMap(data));
	}
	
	public static String getCharHexCode(char a) {
		char[] s = new char[4];
		for (int i = 4; --i >= 0; ) {
			s[i] = (char)(a & 0x000F);
			s[i] += ((s[i] >= 10) ? 'A' - 10 : '0');
			a >>= 4;
		}
		return String.copyValueOf(s);
	}
	
	public static String toUnicodeString(char a) {
		return "\\u" + getCharHexCode(a);
	}
	
	public static int nativeIndexOf(Object[] array, Object item) {
		for (int i = 0; i < array.length; i++)
			if (array[i] == item)
				return i;
		return -1;
	}
	
	public static int indexOf(Object[] array, Object item) {
		if (item == null) {
			for (int i = 0; i < array.length; i++)
				if (array[i] == null)
					return i;
		} else {
			for (int i = 0; i < array.length; i++)
				if (item.equals(array[i]))
					return i;
		}
		return -1;
	}
	
	public static <T> Map<Character, T> createSimpleMap(final Object[] data) {
		if ((data.length & 1) != 0)
			throw new IllegalArgumentException();
		
		for (int i = 0; i < data.length; i += 2)
			if (data[i] == null || !(data[i] instanceof Character))
				throw new IllegalArgumentException();
				
		return new Map<Character, T>() {

			private Set<Entry<Character, T>> entrySet;
			
			public void clear() {
				throw new UnsupportedOperationException();
			}

			public boolean containsKey(Object key) {
				for (int i = 0; i < data.length; i += 2) {
					if (data[i].equals(key))
						return true;
				}
				return false;
			}

			public boolean containsValue(Object value) {
				for (int i = 1; i < data.length; i += 2) {
					if (value == null ? data[i] == null : value.equals(data[i]))
						return true;
				}
				return false;
			}

			public Set<Entry<Character, T>> entrySet() {
				if (entrySet == null) {
					Set<Entry<Character, T>> res = new HashSet<Entry<Character,T>>();
					for (int i = 0; i < data.length; i += 2) {
						final Character key = (Character)data[i];
						final T value = (T) data[i + 1];
						res.add(new Entry<Character, T>() {
							public Character getKey() {
								return key;
							}
							public T getValue() {
								return value;
							}
							public T setValue(T value) {
								throw new UnsupportedOperationException();
							}
						});
					}
					entrySet = Collections.unmodifiableSet(res);
				}
				return entrySet;
			}

			public T get(Object key) {
				for (int i = 0; i < data.length; i += 2) {
					if (data[i].equals(key))
						return (T) data[i + 1];
				}
				return null;
			}

			public boolean isEmpty() {
				return data.length == 0;
			}

			public Set<Character> keySet() {
				throw new UnsupportedOperationException();
			}

			public T put(Character key, T value) {
				throw new UnsupportedOperationException();
			}

			public void putAll(Map<? extends Character, ? extends T> t) {
				throw new UnsupportedOperationException();
			}

			public T remove(Object key) {
				throw new UnsupportedOperationException();
			}

			public int size() {
				return data.length >> 1;
			}

			public Collection<T> values() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public static <A, B> Map<A, B> createMap(Object[] data) {
		assert (data.length & 1) == 0;
		Map<A, B> res = new HashMap<A, B>();
		for (int i = 0; i < data.length; i += 2) {
			res.put((A)data[i], (B)data[i + 1]);
		}
		return res;
	}

	public static <A, B> Map<A, B> createRevertMap(Map<B, A> map) {
		Map<A, B> res = new HashMap<A, B>();
		for (Entry<B, A> e : map.entrySet()) {
			if (res.put(e.getValue(), e.getKey()) != null)
				throw new IllegalArgumentException("Duplecated value");
		}
		return res;
	}
	
}
