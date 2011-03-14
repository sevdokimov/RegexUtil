package com.ess.util;

import java.util.ArrayList;

public class ListenersList<T> implements IListenersList<T> {

	private final ArrayList<EventListener<T>> list = new ArrayList<EventListener<T>>();
	private Object[] array = EmptyArrays.OBJECTS;
	
	public ListenersList(IListenersList<T> ... parentLists) {
		EventListener<T> listener = new EventListener<T>() {
			public void notify(T event) {
				send(event);
			}
		};
		for (IListenersList<T> l : parentLists) {
			l.addListener(listener);
		}
	}
	
	public ListenersList() {
		
	}
	
	public void addListener(EventListener<T> l) {
		list.add(l);
		array = list.toArray();
	}

	public void removeListener(EventListener<T> l) {
		list.remove(l);
		array = list.toArray();
	}
	
	public void send() {
		send(null);
	}
	
	public void safeSend(T event) {
		Object[] m = array;
		for (Object o : m) {
			try {
				((EventListener<T>)o).notify(event);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send(T event) {
		Object[] m = array;
		for (Object o : m) {
			((EventListener<T>)o).notify(event);
		}
	}
	
}
