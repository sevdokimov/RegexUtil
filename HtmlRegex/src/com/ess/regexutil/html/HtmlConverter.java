package com.ess.regexutil.html;

import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.regexparser.ParsedRegex;
import com.ess.regexutil.regexparser.RegexConfig;
import com.ess.util.Helper;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class HtmlConverter {

    private StringBuilder res;

    private ParsedRegex parsedRegex;

    private ArrayList<Integer> colors;

    private int length;

    private String regex;

    private int flags;

    private static final String colorDupOpt = "0123456789"; // *
    private static final String colorSkipOpt = "`~!@#%^&()_=|{"; // -
    private static final String blockOpt = ";:<>,.?/"; // +

    private HtmlConverter(String regex, int flags) {
        res = new StringBuilder();
        length = regex.length();
        this.regex = regex;
        this.flags = flags;
        parsedRegex = new ParsedRegex(regex, flags);
        colors = new ArrayList<Integer>();
    }

    private void appendNumber(StringBuilder res, int x, char c) {
    	StringBuilder sb = new StringBuilder();
        do {
            sb.append((char)((x % 26) + c));
            x /= 26;
        } while (x > 0);
        sb.reverse();
        res.append(sb);
    }

    private void appendNumber(StringBuilder res, int x) {
        char c = 'a';
        if (res.length() > 0) {
            char a = res.charAt(res.length() - 1);
            if (a >= 'a' && a <= 'z') {
                c = 'A';
            }
        }
        appendNumber(res, x, c);
    }

    private void appendNumOpt(StringBuilder res, int x, String opt, char m) {
        if (x >= opt.length()) {
            res.append(m);
            appendNumber(res, x - opt.length());
        } else {
            res.append(opt.charAt(x));
        }
    }

    /**
     * - skip
     * 0..9 * duplicate
     */
    private String packColor(int[] a, int[] def) {
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < length; ) {
            Integer c = a[i];
            if (c.equals(def[i])) {
                c = null;
            }
            int k;
            for ( k = i + 1; k < length; k++) {
                Integer c2 = a[k];
                if (c2.equals(def[k]))
                    c2 = null;
                if (c == null ? c2 != null : !c.equals(c2))
                    break;
            }

            if (c == null) {
                if (k == length)
                    break;

                appendNumOpt(res, k - i - 1, colorSkipOpt, '-');
            } else {
                appendNumber(res, getColorIndex(c));
                if (k > i + 1) {
                    appendNumOpt(res, k - i - 2, colorDupOpt, '*');
                }
            }
            i = k;
        }

        return res.toString();
    }

    private int getColorIndex(int color) {
        for (int i = 0; i < colors.size(); i++) {
            if (colors.get(i) == color) {
                return i;
            }
        }
        colors.add(color);
        return colors.size()-1;
    }

    private String packBoolean(boolean[] a, boolean[] def) {
        StringBuilder res = new StringBuilder();

        boolean pred = true;
        for (int i = 0; i < length; ) {
            boolean c = (a[i] != def[i]);
            int k;
            for ( k = i + 1; k < length && c == (a[k] != def[k]); k++);

            if (pred == c) {
                appendNumber(res, k-i-1, 'A');
            } else {
                if (!c && k == length)
                    break;
                appendNumber(res, k-i-1);
            }
            i = k;
            pred = c;
        }

        return res.toString();
    }

    private void convert() {
		// Get titles
		res.append("{\n");

        // Add regex
        res.append("regex:'").append(regex.replaceAll("\\\\", "\\\\\\\\").replaceAll("\'", "\\\\'")).append('\'');

        res.append(",\n");

        // Add hints
        res.append("h:[");
        Map<String, Integer> hintMap = new HashMap<String, Integer>();
        for (int i = 0; i < length; i++) {
			String hint = "";
			ITextItem item = parsedRegex.getItem(i);
			if (item != null)
				hint = item.getHint();
			if (hint == null)
				hint = "";

			if (i > 0)
				res.append(',');

            Integer index = hintMap.get(hint);
            if (index != null) {
                res.append(index);
            } else {
                hintMap.put(hint, i);
                res.append('\'').append(Helper.toHtml(hint).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'")).append("'\n");
            }
		}
		res.append("]");


        res.append(",\n");


        // Add default style
        ITextStyle defaultStyle = RegexConfig.getInstance().getDefaultStyle();

        StyleData sd = new StyleData(length);
        defaultStyle.apply(sd, 0, regex.length());

        StyleData sdDef = new StyleData(length);
        defaultStyle.apply(sdDef, 0, length);

        parsedRegex.paint(sdDef, -1);
        res.append("df:").append(getColorIndex(defaultStyle.getForeground()));
        res.append(',');
        res.append("db:").append(getColorIndex(defaultStyle.getBackground()));
        res.append(',');
        res.append("dw:").append(defaultStyle.getBold());
        res.append(',');
        res.append("di:").append(defaultStyle.getItalic());
        res.append(",\n");

        String s = packColor(sdDef.background, sd.background);
        if (s.length() > 0)
            res.append("b:'").append(s).append("',\n");

        s = packColor(sdDef.foreground, sd.foreground);
        if (s.length() > 0)
            res.append("f:'").append(s).append("',\n");

        s = packBoolean(sdDef.bold, sd.bold);
        if (s.length() > 0)
            res.append("w:'").append(s).append("',\n");

        s = packBoolean(sdDef.italic, sd.italic);
        if (s.length() > 0)
            res.append("i:'").append(s).append("',\n");


        // Add highlighting
        StyleData sd2 = new StyleData(length);

        res.append("s:'");
		for (int i = 0; i < length; ) {
			defaultStyle.apply(sd, 0, length);
			parsedRegex.paint(sd, i+1);

            int k;
            for ( k = i+1; k < length; k++) {
                defaultStyle.apply(sd2, 0, length);
                parsedRegex.paint(sd2, k+1);

                if (!sd.isEquals(sd2, 0, length))
                    break;
            }

            s = packColor(sd.background, sdDef.background);
            if (s.length() > 0)
                res.append(s);

            s = packColor(sd.foreground, sdDef.foreground);
            if (s.length() > 0)
                res.append('}').append(s);

            s = packBoolean(sd.bold, sdDef.bold);
            if (s.length() > 0)
                res.append('[').append(s);

            s = packBoolean(sd.italic, sdDef.italic);
            if (s.length() > 0)
                res.append(']').append(s);

            appendNumOpt(res, k - i - 1, blockOpt, '+');
            
            i = k;
        }
        res.append("',\n");

        // Add colors list
        res.append("c:'");
        for (Integer color : colors) {
            s = Integer.toString(color, 16);
            for (int i = s.length(); i < 6; i++)
                res.append('0');
            res.append(s);
        }
        res.append('\'');


        res.append("}\n");
	}

    public static String convert(String regex, int flags) {
        HtmlConverter converter = new HtmlConverter(regex, flags);
        converter.convert();
        return converter.res.toString();
    }

}
