package bctrace.core.io.shiftleft.bctrace;

import bctrace.core.io.shiftleft.bctrace.hook.Hook;

public interface Agent {

  public void init(Bctrace bctrace);

  public void afterRegistration();

  public Hook[] getHooks();

}
