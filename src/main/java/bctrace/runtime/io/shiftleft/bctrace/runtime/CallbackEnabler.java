package bctrace.runtime.io.shiftleft.bctrace.runtime;

import java.util.concurrent.atomic.AtomicInteger;

public class CallbackEnabler {

  public static final ThreadLocal<AtomicInteger> NOTIFY_DISABLED_FLAG = new ThreadLocal<>() {
    @Override
    protected AtomicInteger initialValue() {
      return new AtomicInteger(0);
    }
  };

  public static boolean isThreadNotificationEnabled() {
    return NOTIFY_DISABLED_FLAG.get().intValue() == 0;
  }

  public static void enableThreadNotification() {
    NOTIFY_DISABLED_FLAG.get().decrementAndGet();
  }

  public static void disableThreadNotification() {
    NOTIFY_DISABLED_FLAG.get().incrementAndGet();
  }
}
