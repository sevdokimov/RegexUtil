package com.ess.util;

public interface IListenersList<T> {

	void addListener(EventListener<T> l);

	void removeListener(EventListener<T> l);

}