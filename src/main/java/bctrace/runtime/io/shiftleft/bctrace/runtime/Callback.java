/*
 * ShiftLeft, Inc. CONFIDENTIAL
 * Unpublished Copyright (c) 2017 ShiftLeft, Inc., All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of ShiftLeft, Inc.
 * The intellectual and technical concepts contained herein are proprietary to ShiftLeft, Inc.
 * and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written permission is obtained
 * from ShiftLeft, Inc. Access to the source code contained herein is hereby forbidden to
 * anyone except current ShiftLeft, Inc. employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure
 * of this source code, which includes information that is confidential and/or proprietary, and
 * is a trade secret, of ShiftLeft, Inc.
 *
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY
 * OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF ShiftLeft, Inc.
 * IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES.
 * THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT
 * CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS
 * CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package bctrace.runtime.io.shiftleft.bctrace.runtime;


import io.shiftleft.bctrace.runtime.listener.generic.GenericMethodMutableStartListener;
import io.shiftleft.bctrace.runtime.listener.generic.GenericMethodReturnListener;
import io.shiftleft.bctrace.runtime.listener.generic.GenericMethodStartListener;
import io.shiftleft.bctrace.runtime.listener.generic.GenericMethodThrowableListener;

/**
 * @author Ignacio del Valle Alles idelvall@shiftleft.io
 */
public final class Callback {

  public static Object[] listeners;
  public static ErrorListener errorListener;

  // Avoid notifications caused by listener methods code
  private static final ThreadLocal<Boolean> NOTIFYING_FLAG = new ThreadLocal<>();

  public static void onStart(Object[] args, int methodId, Class<?> clazz, Object instance, int i) {
    if (!CallbackEnabler.isThreadNotificationEnabled() || (Boolean.TRUE == NOTIFYING_FLAG.get())) {
      return;
    }
    try {
      NOTIFYING_FLAG.set(Boolean.TRUE);
      ((GenericMethodStartListener) listeners[i]).onStart(methodId, clazz, instance, args);
    } catch (Throwable th) {
      handleThrowable(th);
      return;
    } finally {
      NOTIFYING_FLAG.set(Boolean.FALSE);
    }
  }

  public static Object[] onMutableStart(Object[] args, int methodId, Class<?> clazz, Object instance,
      int i) {
    if (!CallbackEnabler.isThreadNotificationEnabled() || (Boolean.TRUE == NOTIFYING_FLAG.get())) {
      return args;
    }
    try {
      NOTIFYING_FLAG.set(Boolean.TRUE);
      return ((GenericMethodMutableStartListener) listeners[i])
          .onStart(methodId, clazz, instance, args);
    } catch (Throwable th) {
      handleThrowable(th);
      return args;
    } finally {
      NOTIFYING_FLAG.set(Boolean.FALSE);
    }
  }

  /**
   * Callback method for FinishListener instances.
   *
   * @param ret The original value to be changed by the listener.
   */
  public static Object onReturn(Object ret, int methodId,
      Class<?> clazz, Object instance, int i, Object[] args) {
    if (!CallbackEnabler.isThreadNotificationEnabled() || (Boolean.TRUE == NOTIFYING_FLAG.get())) {
      return ret;
    }
    try {
      NOTIFYING_FLAG.set(Boolean.TRUE);
      return ((GenericMethodReturnListener) listeners[i])
          .onReturn(methodId, clazz, instance, args, ret);
    } catch (Throwable thr) {
      handleThrowable(thr);
      // In case of exception raised in the listener, return the original value
      return ret;
    } finally {
      NOTIFYING_FLAG.set(Boolean.FALSE);
    }
  }

  /**
   * Callback method for FinishListener instances.
   *
   * @param th The original Throwable to be changed by the listener.
   */
  public static Throwable onThrow(Throwable th, int methodId,
      Class<?> clazz, Object instance, int i, Object[] args) {
    if (!CallbackEnabler.isThreadNotificationEnabled() || (Boolean.TRUE == NOTIFYING_FLAG.get())) {
      return th;
    }
    try {
      NOTIFYING_FLAG.set(Boolean.TRUE);
      return ((GenericMethodThrowableListener) listeners[i])
          .onThrow(methodId, clazz, instance, args, th);
    } catch (Throwable thr) {
      handleThrowable(thr);
      // In case of exception raised in the listener, return the original value
      return th;
    } finally {
      NOTIFYING_FLAG.set(Boolean.FALSE);
    }
  }

  private static void handleThrowable(Throwable th) {
    if (th instanceof BctraceRuntimeException) {
      throw ((BctraceRuntimeException) th).getWrappedException();
    } else {
      if (errorListener != null) {
        errorListener.onError(th);
      } else {
        th.printStackTrace();
      }
    }
  }

  public static interface ErrorListener {

    public void onError(Throwable th);
  }
}
