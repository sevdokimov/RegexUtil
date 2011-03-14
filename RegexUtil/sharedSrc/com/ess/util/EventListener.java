package com.ess.util;

public interface EventListener<T> {

	void notify(T event);
	
}
