/*********************************
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 * Copyright (c) 2008 Symantec Corporation.  All rights reserved.
 *********************************/
package com.ess.util;

import javax.swing.*;

/**
 * @author sergey_evdokimov
 */
public class UIThreadUtil implements Runnable {

    private final UIGetter getter;

    private Object result;

    private UIThreadUtil(UIGetter getter) {
        this.getter = getter;
    }

    public static Object get(UIGetter getter) {
        UIThreadUtil u = new UIThreadUtil(getter);
        synchronized (u) {
            SwingUtilities.invokeLater(u);
            try {
                u.wait();
            } catch (InterruptedException e) {
                // Ignore
            }
            return u.result;
        }
    }

    public synchronized void run() {
        result = getter.get();
        notifyAll();
    }

    public static interface UIGetter {
        public Object get();
    }

}
