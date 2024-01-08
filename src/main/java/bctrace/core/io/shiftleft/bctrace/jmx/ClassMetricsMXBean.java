package bctrace.core.io.shiftleft.bctrace.jmx;

import bctrace.core.io.shiftleft.bctrace.jmx.ClassMetrics.ClassInfo;

public interface ClassMetricsMXBean {

  public ClassInfo[] getQueriedClasses(String classNameToken);

  public ClassInfo[] getClassesRequestedToTransform(String classNameToken);
}
