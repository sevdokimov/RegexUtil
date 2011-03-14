package com.ess.regexutil.views;

import org.eclipse.jface.preference.IPreferenceStore;

import com.ess.regexutil.Activator;

public class DefaultFormValueManager {

	private static final String DEFAULT_TEXT = "1900-01-01 2007/08/13 1900.01.01 1900 01 01 1900-01.01 1900 13 01 1900 02 31";
	private static final String DEFAULT_REGEX_TEXT = "(19|20)\\d\\d([- /.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])";
	private static final String DEFAULT_REPLACEMENT = "1920$210$201";
	private static final int DEFAULT_MODE = 0;
	
	private static DefaultFormValueManager instance = new DefaultFormValueManager();

	public static DefaultFormValueManager getInstance() {
		return instance;
	}
	
	private boolean isInit;
	private String defaultRegex;
	private String defaultText;
	private String defaultReplacement;
	private int defaultMode;
	
	private DefaultFormValueManager() {
	}
	
	private void init() {
		if (!isInit) {
			IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
			boolean saved = pref.getBoolean("saved");
			if (saved) {
				defaultRegex = pref.getString("defaultRegex");
				defaultText = pref.getString("defaultText");
				defaultReplacement = pref.getString("defaultReplacement");
				defaultMode = pref.getInt("defaultMode");
				if (DEFAULT_REPLACEMENT.equals(defaultReplacement) && !DEFAULT_REGEX_TEXT.equals(defaultRegex)) {
					defaultReplacement = "";
				}
			} else {
				defaultRegex = DEFAULT_REGEX_TEXT;
				defaultText = DEFAULT_TEXT;
				defaultReplacement = DEFAULT_REPLACEMENT;
				defaultMode = DEFAULT_MODE;
			}
			isInit = true;
		}
	}
	
	public String getDefaultRegex() {
		init();
		return defaultRegex;
	}

	public String getDefaultText() {
		init();
		return defaultText;
	}
	
	public int getDefaultMode() {
		init();
		return defaultMode;
	}

	public String getDefaultReplacement() {
		init();
		return defaultReplacement;
	}

	public void save(String regex, String text, String replacement, int mode) {
		defaultRegex = regex;
		defaultText = text;
		defaultReplacement = replacement;
		defaultMode = mode; 
		IPreferenceStore pref = Activator.getDefault().getPreferenceStore();
		pref.setValue("defaultRegex", defaultRegex);
		pref.setValue("defaultText", defaultText);
		pref.setValue("defaultReplacement", defaultReplacement);
		pref.setValue("defaultMode", defaultMode);
		pref.setValue("saved", true);
		Activator.getDefault().savePluginPreferences();
	}
	
}
