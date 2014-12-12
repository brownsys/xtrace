package edu.brown.cs.systems.xtrace;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.brown.cs.systems.xtrace.Metadata.XTraceMetadata;
import edu.brown.cs.systems.xtrace.Metadata.XTraceMetadataOrBuilder;
import edu.brown.cs.systems.xtrace.Reporting.XTraceReport3;
import edu.brown.cs.systems.xtrace.Reporting.XTraceReport3.Builder;

/**
 * The front door to X-Trace v3.
 * 
 * Metadata propagation is provided as static methods on the X-Trace class
 * 
 * Logging is provided as instance methods on instances returned by
 * XTrace.getLogger
 * 
 * Provides static methods for X-Trace metadata propagation. Also provides
 * static methods to return logger instances for logging against named classes.
 * 
 * @author Jonathan Mace
 */
public class XTrace {

  public static byte[] XTRACE_BYTES_EXAMPLE = XTraceMetadata.newBuilder().setTaskID(Long.MIN_VALUE).addParentEventID(Long.MIN_VALUE).setTenantClass(Integer.MAX_VALUE).build().toByteArray();

  static final Trace METADATA = new Trace();
  static final Reporter REPORTER = new PubSubReporter(METADATA);

  public interface Logger {
    /** Returns true if this logger is currently able to send reports */
    public boolean valid();

    /** Creates and sends a report */
    public void log(String message, Object... labels);

    /** Creates and sends a report, adding the provided strings as tags */
    public void tag(String message, String... tags);

    /** Decorates then sends the provided report */
    public void log(Builder report);

    /**
     * Decorates then sends the provided report which came from an out-of-band
     * source, so the XTrace metadata for the current thread is not appended
     * before sending
     */
    public void logOOB(Builder report);
  }

  /**
   * If logging is turned off for an agent, then they're given the null logger
   * which does nothing
   */
  static Logger NULL_LOGGER = new Logger() {
    public boolean valid() {
      return false;
    }

    public void log(String message, Object... labels) {
    }

    public void log(Builder report) {
    }

    public void logOOB(Builder report) {
    }

    public void tag(String message, String... tags) {
    }
  };

  static class LoggerImpl implements Logger {
    private final String agent;

    public LoggerImpl(String agent) {
      this.agent = agent;
    }

    public boolean valid() {
      return REPORTER.valid();
    }

    public void log(String message, Object... labels) {
      REPORTER.report(agent, message, labels);
    }

    public void log(XTraceReport3.Builder report) {
      REPORTER.report(agent, report);
    }

    public void logOOB(XTraceReport3.Builder report) {
      REPORTER.reportNoXTrace(agent, report);
    }

    public void tag(String message, String... tags) {
      REPORTER.reportTagged(agent, message, tags);
    }
  }

  /**
   * Returns the default logger
   * 
   * @return
   */
  public static Logger getLogger() {
    if (XTraceSettings.REPORTING_ENABLED_DEFAULT)
      return new LoggerImpl("default");
    else
      return NULL_LOGGER;
  }
  
  /**
   * @return true if XTrace logging is enabled globally
   */
  public static boolean isLoggingEnabled() {
    return XTraceSettings.REPORTING_ON;
  }
  
  /**
   * @param agent the name of a logging agent
   * @return true if logging is enabled for this agent.
   */
  public static boolean canLog(String agent) {
    if (!XTraceSettings.REPORTING_ON) 
      return false;
    else if (agent==null) 
      return XTraceSettings.REPORTING_ENABLED_DEFAULT;
    else if (XTraceSettings.REPORTING_ENABLED_DEFAULT) 
      return !XTraceSettings.REPORTING_DISABLED.contains(agent);
    else 
      return XTraceSettings.REPORTING_ENABLED.contains(agent);
  }

  public static Logger getLogger(String agent) {
    if (agent == null)
      return getLogger();
    else if (canLog(agent))
      return new LoggerImpl(agent);
    else
      return NULL_LOGGER;
  }

  /**
   * Shorthand for getLogger(agent.getName())
   * 
   * @param agent
   *          The name of the agent will be used as the name of the logger to
   *          retrieve
   * @return an xtrace event logger that can be used to log events
   */
  public static Logger getLogger(Class<?> agent) {
    if (agent == null)
      return NULL_LOGGER;
    else
      return getLogger(agent.getName());
  }

  /**
   * Instructs X-Trace to start propagating the provided metadata in this thread
   * 
   * @param bytes
   *          byte representation of the X-Trace metadata to start propagating
   *          in this thread
   */
  public static void set(byte[] bytes) {
    METADATA.set(bytes);
  }

  /**
   * Instructs X-Trace to start propagating the provided metadata in this thread
   * 
   * @param metadata
   *          the metadata to start propagating in this thread
   */
  public static void set(Context metadata) {
    METADATA.set(metadata);
  }

  /**
   * Instructs X-Trace to start propagating the provided metadata in this thread
   * 
   * @param base64_encoded_bytes
   *          base64 encoding of the byte representation of the X-Trace metadata
   *          to start propagating in this thread.
   */
  public static void set(String base64_encoded_bytes) {
    METADATA.set(base64_encoded_bytes);
  }
  
  /**
   * Used to specify the type of String encoding used
   * @author a-jomace
   */
  public static enum ENCODING {
    BASE64, BASE16;
  }


  /**
   * Instructs X-Trace to start propagating the provided metadata in this thread
   * 
   * @param encoded_bytes
   *          string encoding of the byte representation of the X-Trace metadata
   *          to start propagating in this thread.
   * @param encoding
   *          enum value specifying the type of string encoding used
   */
  public static void set(String encoded_bytes, ENCODING encoding) {
    switch(encoding) {
    case BASE16: METADATA.setBase16(encoded_bytes); return;
    case BASE64: METADATA.set(encoded_bytes); return;
    }
  }

  /**
   * Merge the metadata provided into the metadata currently being propagated by
   * this thread. If nothing is currently being propagated in this thread, this
   * method call is equivalent to set
   * 
   * @param metadata
   *          the metadata to merge into this thread
   */
  public static void join(Context metadata) {
    METADATA.join(metadata);
  }

  /**
   * Merge the metadata provided into the metadata currently being propagated by
   * this thread. If nothing is currently being propagated in this thread, this
   * method call is equivalent to set
   * 
   * @param bytes
   *          the byte representation of the X-Trace metadata to merge into this
   *          thread
   */
  public static void join(byte[] bytes) {
    METADATA.join(bytes);
  }

  /**
   * @return the X-Trace metadata being propagated in this thread
   */
  public static Context get() {
    return METADATA.get();
  }

  /**
   * @return the byte representation of the X-Trace metadata being propagated in
   *         this thread
   */
  public static byte[] bytes() {
    return METADATA.bytes();
  }

  /**
   * @return a base16 encoded string of the X-Trace metadata being propagated in
   *         this thread, or null if no currently valid context.  base16 is just
   *         hexadecimal and does not include any padding characters
   */
  public static String base16() {
    return METADATA.base16();
  }

  /**
   * @return a base64 encoded string of the X-Trace metadata being propagated in
   *         this thread, or null if no currently valid context
   */
  public static String base64() {
    return METADATA.base64();
  }

  /**
   * @return some clients wish to bound the size of the metadata that is sent on
   *         the wire this method returns the byte representation of the X-Trace
   *         metadata, but only sends a single parent id. this will lose
   *         causality, so the client should log before sending this call
   */
  public static byte[] bytesBounded() {
    XTraceMetadataOrBuilder md = METADATA.observe();
    if (md != null && md.getParentEventIDCount() > 1) {
      long firstParentEventID = md.getParentEventID(0);
      try {
        return XTraceMetadata.newBuilder().mergeFrom(bytes()).clearParentEventID().addParentEventID(firstParentEventID).build().toByteArray();
      } catch (Exception e) {
        // Do nothing, return bytes
      }
    }
    return bytes();
  }

  /**
   * @return true if X-Trace is currently propagating metadata in this thread
   */
  public static boolean active() {
    return METADATA.exists();
  }

  /**
   * Stops propagating any X-Trace metadata in this thread
   */
  public static void stop() {
    METADATA.clear();
  }

  /**
   * @return true if a task ID is being propagated by X-Trace in this thread
   */
  public static boolean hasTaskID() {
    XTraceMetadataOrBuilder xmd = METADATA.observe();
    return xmd == null ? false : xmd.hasTaskID();
  }

  /**
   * @return true if a tenant class is being propagated by X-Trace in this
   *         thread
   */
  public static boolean hasTenantClass() {
    XTraceMetadataOrBuilder xmd = METADATA.observe();
    return xmd == null ? false : xmd.hasTenantClass();
  }

  /**
   * @return the task ID currently being propagated by X-Trace in this thread,
   *         or null if none being propagated
   */
  public static Long getTaskID() {
    XTraceMetadataOrBuilder xmd = METADATA.observe();
    return xmd == null ? null : xmd.hasTaskID() ? xmd.getTaskID() : null;
  }

  public static boolean isCausalityEnabled() {
    XTraceMetadataOrBuilder xmd = METADATA.observe();
    return xmd == null ? false : xmd.getParentEventIDCount() > 0;
  }

  public static List<Long> getParentIDs() {
    XTraceMetadataOrBuilder xmd = METADATA.observe();
    return xmd == null ? null : xmd.getParentEventIDList();
  }

  /**
   * @return the tenant class currently being propagated by X-Trace in this
   *         thread, or -1 if none being propagated
   */
  public static int getTenantClass() {
    XTraceMetadataOrBuilder xmd = METADATA.observe();
    return xmd == null ? -1 : xmd.hasTenantClass() ? xmd.getTenantClass() : -1;
  }

  /**
   * @return the tenant class of the provided context
   *         thread, or -1 if none being propagated
   */
  public static int getTenantClass(Context ctx) {
    if (ctx==null)
      return -1;
    XTraceMetadataOrBuilder xmd = ctx.observe();
    return xmd == null ? -1 : xmd.hasTenantClass() ? xmd.getTenantClass() : -1;
  }

  /**
   * @return true if the thread currently has multiple parent X-Trace event IDs,
   *         and it is therefore worth logging a message before serializing.
   */
  public static boolean shouldLogBeforeSerialization() {
    XTraceMetadataOrBuilder xmd = METADATA.observe();
    return xmd == null ? false : xmd.getParentEventIDCount() > 1;
  }

  /**
   * Start propagating a task ID in this thread if we aren't already propagating
   * a task ID. If we aren't currently propagating a task ID, a new one is
   * randomly generated
   * 
   * @param trackCausality
   *          should we also track causality for this task?
   */
  public static void startTask(boolean trackCausality) {
    setTask(Reporter.random.nextLong(), trackCausality);
  }

  /**
   * Start propagating the specified taskID in this thread. If X-Trace is
   * already propagating a taskid in this thread, then this method call does
   * nothing.
   * 
   * @param taskid
   *          the taskID to start propagating in this thread
   * @param trackCausality
   *          should we also track causality for this task?
   */
  public static void setTask(long taskid, boolean trackCausality) {
    XTraceMetadataOrBuilder current = METADATA.observe();
    if (current != null && current.hasTaskID())
      return;

    if (trackCausality)
      METADATA.modify().setTaskID(Reporter.random.nextLong()).clearParentEventID().addParentEventID(0L);
    else
      METADATA.modify().setTaskID(Reporter.random.nextLong()).clearParentEventID();
  }

  /**
   * Start propagating the specified tenant class in this thread. If X-Trace is
   * already propagating a tenant class, then it will be overwritten by the
   * tenant class provided.
   */
  public static void setTenantClass(int tenantclass) {
    METADATA.modify().setTenantClass(tenantclass);
  }
  
  /**
   * Returns a key-value map of environment variables that can be used to start/resume xtrace for this task.
   * 
   * Typically this is useful if a process is kicking off a child process, which can resume the xtrace
   * context using the setFromEnvironment method
   */
  public static Map<String, String> environment() {
    String base64 = XTrace.base64();
    if (base64==null)
      return Collections.emptyMap();
    
    Map<String, String> environment = new HashMap<String, String>();
    environment.put(XTraceConstants.XTRACE_CONTEXT_ENVIRONMENT_VARIABLE, base64);
    
    return environment;
  }
  
  /**
   * Sets the current xtrace metadata based off values set in the process's environment.
   * 
   * Typically this is useful if a process is kicking off a child process, which can resume the xtrace
   * context using the setFromEnvironment method
   */
  public static void set(Map<String, String> environmentVariables) {
    String base64 = environmentVariables.get(XTraceConstants.XTRACE_CONTEXT_ENVIRONMENT_VARIABLE);
    if (base64!=null) {
      XTrace.set(base64);
    }
  }
  
  public static void shutdown() {
    REPORTER.close();
  }

}
