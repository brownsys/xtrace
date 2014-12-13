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
    System.out.println("Starting XTraceHelloWorld example");
    System.out.println("Make sure the X-Trace server is running! xtrace/target/appassembler/bin/backend\n");
		
		XTrace.startTask(true);
		
		x.log("Hello World!");
		
		x.log("Goodbye World!");
		
		XTrace.shutdown();
		
		System.out.println("Example complete, Ctrl-C to exit");
		System.out.println("Go to http://localhost:4080 to view generated graphs");
		try {
      Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
		}
	}

}
