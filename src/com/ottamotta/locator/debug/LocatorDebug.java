package com.ottamotta.locator.debug;

import android.util.Log;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class LocatorDebug {

    private static final String LOG_PREFIX = "Locator:";
    
    private static final Map<String, Stack<Record>> methodsNotFinished = new ConcurrentHashMap<>();
    private static final Map<String, Stack<Record>> methodsFinished = new ConcurrentHashMap<>();
    
    public static void startTrace(String methodName) {
        Stack<Record> notFinished = getNotFinishedStack(methodName);
        notFinished.push(new Record(System.currentTimeMillis()));
    }

    private static Stack<Record> getNotFinishedStack(String methodName) {
        synchronized (methodsNotFinished) {
            Stack<Record> records;
            if (methodsNotFinished.containsKey(methodName)) {
                records = methodsNotFinished.get(methodName);
            } else {
                records = new Stack<>();
                methodsNotFinished.put(methodName, records);                
            }
            return records;
        }
    }
    
    private static Stack<Record> getFinishedStack(String methodName) {
        synchronized (methodsFinished) {
            Stack<Record> records;
            if (methodsFinished.containsKey(methodName)) {
                records = methodsNotFinished.get(methodName);
            } else {
                records = new Stack<>();                
            }
            return records;
        }
    }

    public static void stopTrace(String methodName) {
        try {
            synchronized (methodsNotFinished) {
                Record finished = methodsNotFinished.get(methodName).pop();
                finished.finish = System.currentTimeMillis();
                Stack<Record> finishedStack = getFinishedStack(methodName);
                finishedStack.push(finished);
                log(methodName, finished);
            }            
        } catch (NullPointerException e) {
            throw new IllegalStateException("Trying to stop method {" + methodName +"} but it's not started");
        }
        
    }
    
    private static void log(String methodName, Record finishedRecord) {
        Log.d(LOG_PREFIX + methodName, finishedRecord.getDuration() + "ms");
    }

    private static class Record {
        long start;
        long finish;

        Record(long start) {
            this.start = start;
        }
        long getDuration() {
            return finish-start;
        }
    }
    
    

}
