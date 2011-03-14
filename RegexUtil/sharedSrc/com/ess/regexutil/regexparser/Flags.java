package com.ess.regexutil.regexparser;

import com.ess.util.ListenersList;

public class Flags {
	
	public final ListenersList<Integer> changeListeners = new ListenersList<Integer>();
	
	private int flags;

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		if (this.flags != flags) {
			this.flags = flags;
			changeListeners.send(flags);
		}
	}
	
	public void setFlag(int flag, boolean f) {
		if (f) {
			setEnableFlag(flag);
		} else {
			setDisableFlag(flag);
		}
	}
	
	public void setEnableFlag(int flag) {
		setFlags(flags | flag);
	}

	public void setDisableFlag(int flag) {
		setFlags(flags & ~flag);
	}
	
	public boolean isFlag(int flag) {
		return (flags & flag) != 0;
	}
	
}
