package com.ess.regexutil.parsedtext;

import java.util.Arrays;

public class TextStyle implements ITextStyle {
	
	public static final ITextStyle EMPTY_STYLE = new TextStyle();
	
	public static final ITextStyle BOLD_STYLE = new TextStyle(null, null, Boolean.TRUE, null);
	public static final ITextStyle ITALIC_STYLE = new TextStyle(null, null, null, Boolean.TRUE);
	
	private Integer foreground;
	private Integer background;
	private Boolean bold;
	private Boolean italic;
	
	public TextStyle() {
	}
	
	public TextStyle(Integer foreground, Integer background, Boolean bold, Boolean italic) {
		this.foreground = foreground;
		this.background = background;
		this.bold = bold;
		this.italic = italic;
	}

	public Integer getBackground() {
		return background;
	}

	public void setBackground(Integer background) {
		this.background = background;
	}

	public Boolean getBold() {
		return bold;
	}

	public void setBold(Boolean bold) {
		this.bold = bold;
	}

	public Integer getForeground() {
		return foreground;
	}

	public void setForeground(Integer foreground) {
		this.foreground = foreground;
	}

	public Boolean getItalic() {
		return italic;
	}

	public void setItalic(Boolean italic) {
		this.italic = italic;
	}

	public void apply(StyleData sd, int start, int end) {
		if (bold != null)
			Arrays.fill(sd.bold, start, end, bold.booleanValue());
		if (italic != null)
			Arrays.fill(sd.italic, start, end, italic.booleanValue());
		if (foreground != null)
			Arrays.fill(sd.foreground, start, end, foreground.intValue());
		if (background != null)
			Arrays.fill(sd.background, start, end, background.intValue());
	}
	
	public static ITextStyle foregroundStyle(int color) {
		return new TextStyle(color, null, null, null);
	}
	
	public static ITextStyle backgroundStyle(int color) {
		return new TextStyle(null, color, null, null);
	}
	
	public static ITextStyle boldStyle(int color) {
		return new TextStyle(null, color, null, null);
	}

}
