package bctrace.trace;


import java.lang.instrument.UnmodifiableClassException;

import bctrace.core.io.shiftleft.bctrace.Bctrace;
import bctrace.core.io.shiftleft.bctrace.hook.Hook;
import bctrace.spi.io.shiftleft.bctrace.logging.Level;
import bctrace.spi.io.shiftleft.bctrace.logging.Logger;
import bctrace.trace.hooks.StringBuilderAppendHook;
import bctrace.trace.hooks.StringConstructorHook;

public class Agent implements bctrace.core.io.shiftleft.bctrace.Agent {

  private static final Agent INSTANCE = new Agent();

  private static final Logger LOGGER = Bctrace.getAgentLogger();

  private final Hook[] hooks = new Hook[]{
      // TODO register your hooks here
      new StringConstructorHook(),
      new StringBuilderAppendHook()
  };

  private Bctrace bctrace;

  @Override
  public void init(final Bctrace bctrace) {
    this.bctrace = bctrace;
  }

  @Override
  public void afterRegistration() {
    LOGGER.log(Level.INFO, "Starting bctrace agent ...");
    try {
      Class[] classesToReinstrument = new Class[]{
          // TODO loaded classes that need retransformation, if any
          String.class,
          StringBuilder.class
      };
      bctrace.getInstrumentation().retransformClasses(classesToReinstrument);
    } catch (UnmodifiableClassException ex) {
      LOGGER.log(Level.ERROR, ex.getMessage(), ex);
    }
  }

  public static Agent getInstance() {
    return INSTANCE;
  }

  public Bctrace getBctrace() {
    return bctrace;
  }

  @Override
  public Hook[] getHooks() {
    return this.hooks;
  }
}
