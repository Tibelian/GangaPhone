package com.tibelian.gangaphone.api;

public class SyncResult {

    private static final long TIMEOUT = 20000L;
    private Object result;

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

    public void setResult(Object result) {
        this.result = result;
        synchronized (this) {
            notify();
        }
    }

}
