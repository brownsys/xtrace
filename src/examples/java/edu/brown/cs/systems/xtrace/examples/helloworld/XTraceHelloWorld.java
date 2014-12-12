package edu.brown.cs.systems.xtrace.examples.helloworld;

import edu.brown.cs.systems.xtrace.XTrace;

/**
 * This example creates an XTrace task and prints hello world
 * 
 * A single task will show up in the XTrace WebUI, that has two events
 */
public class XTraceHelloWorld {
	
	public static final XTrace.Logger x = XTrace.getLogger(XTraceHelloWorld.class);
	
	public static void main(String[] args) throws InterruptedException {
		
		XTrace.startTask(true);
		
		x.log("Hello World!");
		
		x.log("Goodbye World!");
		
		XTrace.shutdown();
		
	}

}
