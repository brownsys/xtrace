package edu.brown.cs.systems.xtrace.examples.helloworld;

import edu.brown.cs.systems.xtrace.XTrace;

public class XTraceMultiHelloWorld {
  
  private static final XTrace.Logger x = XTrace.getLogger(XTraceMultiHelloWorld.class);
  
  private static class HelloWorldThread extends Thread {
    
    private final String name;
    
    public HelloWorldThread(String name) {
      this.name = name;
      start();
    }
    
    public void run() {
      
      XTrace.startTask(true);
      
      x.log("Hello from " + name);
      
      x.log("Goodbye from " + name);
      
      
    }
  }
  
  public static void main(String[] args) throws InterruptedException {
    System.out.println("Starting XTraceMultiHelloWorld example");
    System.out.println("Make sure the X-Trace server is running! xtrace/target/appassembler/bin/backend\n");

    Thread a = new HelloWorldThread("Rodrigo Fonseca");
    Thread b = new HelloWorldThread("Jonathan Mace");
    Thread c = new HelloWorldThread("Peter Bodik");
    
    a.join();
    b.join();
    c.join();
    
    XTrace.shutdown();
    
    System.out.println("Example complete, Ctrl-C to exit");
    System.out.println("Go to http://localhost:4080 to view generated graphs");
    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
    }
  }

}
