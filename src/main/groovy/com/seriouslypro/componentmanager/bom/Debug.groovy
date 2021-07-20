package com.seriouslypro.componentmanager.bom

class Debug {
    static boolean traceEnabled = false

    static public void trace(String message) {
        if (traceEnabled) {
            System.out.println(message)
        }
    }
}
