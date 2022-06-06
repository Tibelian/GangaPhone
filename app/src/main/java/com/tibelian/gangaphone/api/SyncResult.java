package com.tibelian.gangaphone.api;

/**
 * Synchronize one variable
 */
public class SyncResult {

    // max time to wait
    private static final long TIMEOUT = 20000L;

    // the variable we will sync
    private Object result;

    /**
     * Wait until result variable is changed
     * @return Object
     */
    public Object getResult() {
        long startTimeMillis = System.currentTimeMillis();
        while (result == null && System.currentTimeMillis() - startTimeMillis < TIMEOUT) {
            synchronized (this) {
                try {
                    wait(TIMEOUT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * change the result variable
     * and notifies the sync method
     * @param result
     */
    public void setResult(Object result) {
        this.result = result;
        synchronized (this) {
            notify();
        }
    }

}
