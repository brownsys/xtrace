package edu.brown.cs.systems.xtrace.examples.helloworld;

import java.util.ArrayList;
import java.util.Collection;

import edu.brown.cs.systems.xtrace.Context;
import edu.brown.cs.systems.xtrace.XTrace;

/**
 * Multiple threads publishing to the same task
 * @author a-jomace
 *
 */
public class XTraceMultiHelloWorld2 {
  
  // Get the logger for this class
  static XTrace.Logger x = XTrace.getLogger("XTraceMultiHelloWorld2");
  
  // When the thread runs, it joins the XTrace task that
  // was handed to it upon thread creation, then logs
  // some events
  public static class MyThread extends Thread {
    
    final String name;
    final Context threadCreationContext;
    public volatile Context threadCompletionContext;

    public MyThread(String name) {
      XTraceMultiHelloWorld2.x.log("Creating MyThread " + name);
      
      this.name = name;
      this.threadCreationContext = XTrace.get();
      
      start();
    }
    
    public void run() {
      XTrace.set(threadCreationContext);
      
      // not necessary, but you can get other loggers for your task too
      XTrace.Logger myx = XTrace.getLogger("MyThread " + name);
      
      myx.log("MyThread " + name + " running!");
      
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
      }
      
      myx.log("MyThread " + name + " complete, adios!");
      
      threadCompletionContext = XTrace.get();
    }
    
  }

  
  public static void main(String[] args) {
    int numthreads = 10;
    System.out.println("Starting XTraceMultiHelloWorld2 example");
    System.out.println("Make sure the X-Trace server is running! xtrace/target/appassembler/bin/backend\n");
  
    // Start one task for the entire example    
    XTrace.startTask(true);
    
    // Tag the task (same as a log statement but a tag adds metadata for the web UI. A legacy thing, but useful for usability 
    x.tag("Starting XTraceMultiHelloWorld2", "XTraceMultiHelloWorld2");
    
    // Another log statement    
    x.log("Kicking off " + numthreads + " MyThreads");
    
    // Create a bunch of MyThreads.  This automatically starts them too
    Collection<MyThread> mythreads = new ArrayList<MyThread>();
    for (int i = 0; i < numthreads; i++) {
      mythreads.add(new MyThread(Integer.toString(i)));
    }
    
    // Now await thread completions
    try {
      for (MyThread t : mythreads) {
        // Log the thread we're waiting for
        x.log("Awaiting MyThread " + t.name);
        
        // Actually join the thread
        t.join();
        
        // Join the X-Trace context from completion of the thread to our current X-Trace context
        XTrace.join(t.threadCompletionContext);
        
        // Log an event. The parent of this event is BOTH the previous event in this thread, AND the
        // previous event before the MyThread's completion.
        x.log("Joined up with MyThread " + t.name);
      }
    } catch (InterruptedException e) {
    }
    
    // Log a final message saying we're complete
    x.log("XTraceMultiHelloWorld2 complete!");
    
    // Shut down X-Trace
    XTrace.shutdown();
    
    System.out.println("Example complete, Ctrl-C to exit");
    System.out.println("Go to http://localhost:4080 to view generated graphs");
    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
    }
  }
}
