package ru.idealplm.specification.mvm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PerfTrack {
	
	public static ArrayList<String> perfLog = new ArrayList<String>();
	public static Map<String, Long> startTimes = new HashMap<String, Long>();
	
	public static long endTime = 0;
	
	public static void prepare(String methodName){
		startTimes.put(methodName, System.currentTimeMillis());
	}
	
	public static void addToLog(String methodName){
		endTime = System.currentTimeMillis();
		perfLog.add("methodName: " + methodName + " time: " + String.valueOf(endTime - startTimes.get(methodName)) + "ms");
		endTime = 0;
	}
	
	public static void printLog(){
		System.out.println("--- PERFORMANCE LOG ---");
		for(String line : perfLog){
			System.out.println(line);
		}
		System.out.println("--- PERFORMANCE LOG ---");
	}

}
