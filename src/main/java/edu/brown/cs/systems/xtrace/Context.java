package edu.brown.cs.systems.xtrace;

import com.google.common.io.BaseEncoding;

import edu.brown.cs.systems.xtrace.Metadata.XTraceMetadata;
import edu.brown.cs.systems.xtrace.Metadata.XTraceMetadata.Builder;
import edu.brown.cs.systems.xtrace.Metadata.XTraceMetadataOrBuilder;

/**
 * The Context class holds information about an X-Trace task, prior events in an
 * execution, and information about the originator of the task.
 * 
 * The main purpose of the Context class is to be saved and restored in an
 * instrumented application. Contexts can be serialized and deserialized,
 * created, set, and unset via static methods in the XTrace class.
 * 
 * Modification of the data contained in a Context is possible via a few
 * privileged API methods, but is otherwise extremely restricted
 * 
 * @author Jonathan Mace
 * 
 */
public class Context {

  /**
   * A context can only be modified via a Manager. This disallows arbitrary
   * access to the data of the context, and forces all modifications to be
   * routed through modify()
   * 
   * @author Jonathan Mace
   */
  static class Manager {

    /** The actual Context that is active for this thread */
    private ThreadLocal<Context> context = new ThreadLocal<Context>();

    /** Returns true if a context is currently active for this thread */
    public boolean exists() {
      return context.get() != null;
    }

    /** Sets the thread's current context to the one provided */
    public void set(Context ctx) {
      context.set(ctx);
    }

    /**
     * Sets the thread's current context to one parsed from the bytes provided.
     * If the bytes are null or invalid, the context will be cleared
     */
    public void set(byte[] bytes) {
      context.set(Context.parse(bytes));
    }
    
    /**
     * Sets the thread's current context to one parsed from the base64 encoded byte string provided
     * If the bytes are null or invalid, the context will be cleared
     */
    public void set(String base64_encoded_bytes) {
      context.set(Context.parse(base64_encoded_bytes));
    }
    
    /**
     * Sets the thread's current context to one parsed from the base64 encoded byte string provided
     * If the bytes are null or invalid, the context will be cleared
     */
    public void setBase16(String base16_encoded_bytes) {
      context.set(Context.parseBase16(base16_encoded_bytes));
    }

    /** Returns the thread's context or null if none is set */
    public Context get() {
      Context ctx = context.get();
      if (ctx != null)
        ctx.modifiable = false;
      return ctx;
    }

    /**
     * Returns the builder for the thread's context such that it can be modified
     * If there is not currently a context, a new one will be created
     */
    public XTraceMetadata.Builder modify() {
      Context ctx = context.get();
      if (ctx == null)
        context.set(ctx = new Context(XTraceMetadata.newBuilder()));
      else if (!ctx.modifiable)
        context.set(ctx = new Context(ctx.builder.clone()));
      return ctx.builder;
    }

    /** Returns a readonly view on the thread's context or null if none is set */
    public XTraceMetadataOrBuilder observe() {
      Context ctx = context.get();
      return ctx == null ? null : ctx.observe();
    }

    /**
     * Returns the byte representation of this context, or null if no valid
     * context
     */
    public byte[] bytes() {
      Context ctx = context.get();
      return ctx == null ? null : ctx.bytes();
    }

    /**
     * Returns the base16 encoding of the byte representation of this context,
     * or null if no valid context
     */
    public String base16() {
      Context ctx = context.get();
      return ctx == null ? null : ctx.base16();
    }

    /**
     * Returns the base64 encoding of the byte representation of this context,
     * or null if no valid context
     */
    public String base64() {
      Context ctx = context.get();
      return ctx == null ? null : ctx.base64();
    }
  }

  /**
   * @return the serialized byte representation of this X-Trace context. Can be
   *         deserialized with the method XTrace.parse
   */
  public byte[] bytes() {
    return builder.build().toByteArray();
  }

  /**
   * @return the byte representation of this context, encoded into a base16
   *         string
   */
  public String base16() {
    return BaseEncoding.base16().encode(bytes());
  }

  /**
   * @return the byte representation of this context, encoded into a base64
   *         string
   */
  public String base64() {
    return BaseEncoding.base64().encode(bytes());
  }

  /** Returns a readonly view on the context */
  XTraceMetadataOrBuilder observe() {
    return builder;
  }

  /** Used by the manager to keep track of accesses to the Context */
  private volatile boolean modifiable = true;

  /** The actual protobuf containing the context */
  private final Builder builder;

  /** Create a new Context backed by the provided builder */
  private Context(Builder builder) {
    this.builder = builder;
  }

  /** Create a new empty Context */
  public Context() {
    this(XTraceMetadata.newBuilder());
  }

  /**
   * Parse the protocol buffers bytes and put them in a new Context
   * 
   * @param bytes
   *          protocol buffers serialized representation of the metadata, may be
   *          null
   * @return a new context wrapping the deserialized protocol buffers
   *         representation. Returns null if the provided bytes were null or if
   *         there was an exception deserializing the bytes
   */
  public static Context parse(byte[] bytes) {
    if (bytes == null)
      return null;
    try {
      return new Context(XTraceMetadata.newBuilder().mergeFrom(bytes));
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Parse the string-encoded bytes and put them in a new context
   * 
   * @param string
   *          the base64 encoding of the raw bytes, as returned by the string()
   *          method (NOT the toString() method)
   * @return a new context, or null if invalid
   */
  public static Context parse(String string) {
    if (string == null)
      return null;
    try {
      return parse(BaseEncoding.base64().decode(string));
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Parse the string-encoded bytes and put them in a new context
   * 
   * @param string
   *          the base16 encoding of the raw bytes, as returned by the string()
   *          method (NOT the toString() method)
   * @return a new context, or null if invalid
   */
  public static Context parseBase16(String string) {
    if (string == null)
      return null;
    try {
      return parse(BaseEncoding.base16().decode(string));
    } catch (Exception e) {
      return null;
    }
  }

}