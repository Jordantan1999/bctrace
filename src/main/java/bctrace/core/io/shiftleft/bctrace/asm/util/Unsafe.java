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
 * of this source code, which includeas information that is confidential and/or proprietary, and
 * is a trade secret, of ShiftLeft, Inc.
 *
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY
 * OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF ShiftLeft, Inc.
 * IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES.
 * THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT
 * CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS
 * CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package bctrace.core.io.shiftleft.bctrace.asm.util;

import java.lang.reflect.Field;

/**
 * @author Ignacio del Valle Alles idelvall@shiftleft.io
 */
public class Unsafe {

  private static final sun.misc.Unsafe unsafe = getPlatformUnsafe();

  private static sun.misc.Unsafe getPlatformUnsafe() {
    try {
      Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();
      Field instanceField = null;
      for (Field field : fields) {
        if (sun.misc.Unsafe.class.isAssignableFrom(field.getType())) {
          instanceField = field;
          break;
        }
      }
      if (instanceField == null) {
        throw new Error("Unable to get platform sun.misc.Unsafe singleton instance");
      }
      instanceField.setAccessible(true);
      return (sun.misc.Unsafe) instanceField.get(null);
    } catch (Exception ex) {
      throw new Error(ex);
    }
  }

  public static class DirectIntArray {

    private final static long INT_SIZE_IN_BYTES = 4;

    private final long startIndex;
    private final long size;

    private boolean destroyed;

    public DirectIntArray(long size) {
      startIndex = unsafe.allocateMemory(size * INT_SIZE_IN_BYTES);
      unsafe.setMemory(startIndex, size * INT_SIZE_IN_BYTES, (byte) 0);
      this.size = size;
    }

    public void setValue(long index, int value) {
      if (destroyed) {
        throw new IllegalStateException("Instance has been detroyed");
      }
      unsafe.putInt(index(index), value);
    }

    public int getValue(long index) {
      if (destroyed) {
        throw new IllegalStateException("Instance has been detroyed");
      }
      return unsafe.getInt(index(index));
    }

    private long index(long offset) {
      if (destroyed) {
        throw new IllegalStateException("Instance has been detroyed");
      }
      return startIndex + offset * INT_SIZE_IN_BYTES;
    }

    public long getSize() {
      return size;
    }

    public void destroy() {
      if (destroyed) {
        throw new IllegalStateException("Instance has already been detroyed");
      }
      unsafe.freeMemory(startIndex);
      destroyed = true;
    }
  }
}
