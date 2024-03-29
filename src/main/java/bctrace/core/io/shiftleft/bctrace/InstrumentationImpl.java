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
package bctrace.core.io.shiftleft.bctrace;

import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bctrace.core.io.shiftleft.bctrace.asm.TransformationSupport;
import bctrace.core.io.shiftleft.bctrace.jmx.ClassMetrics;

/**
 * Single implementation of the {@link Instrumentation Instrumentation} interface.
 *
 * @author Ignacio del Valle Alles idelvall@shiftleft.io
 */
public final class InstrumentationImpl implements Instrumentation {

  private final java.lang.instrument.Instrumentation javaInstrumentation;

  private final Map<String, List<WeakReference<ClassLoader>>> loadedClassesMap = new HashMap<>();
  private final Map<String, List<WeakReference<ClassLoader>>> transformedClassesMap = new HashMap<>();


  public InstrumentationImpl(java.lang.instrument.Instrumentation javaInstrumentation) {
    this.javaInstrumentation = javaInstrumentation;
  }

  @Override
  public boolean isRetransformClassesSupported() {
    return javaInstrumentation != null && javaInstrumentation.isRetransformClassesSupported();
  }

  @Override
  public boolean isModifiableClass(Class<?> clazz) {
    return isRetransformClassesSupported()
        && TransformationSupport.isRetransformable(clazz)
        && javaInstrumentation.isModifiableClass(clazz);
  }

  @Override
  public boolean isModifiableClass(String jvmClassName) {
    return isRetransformClassesSupported() && TransformationSupport
        .isTransformable(jvmClassName, null);
  }

  public java.lang.instrument.Instrumentation getJavaInstrumentation() {
    return javaInstrumentation;
  }

  @Override
  public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
    if (javaInstrumentation != null && classes != null && classes.length > 0) {
      for (Class<?> clazz : classes) {
        if (!isModifiableClass(clazz)) {
          throw new UnmodifiableClassException(clazz.getName());
        }
        ClassMetrics.getInstance().addRequestedToInstrument(clazz);
      }
      javaInstrumentation.retransformClasses(classes);
    }
  }

  private void addAllLoadedClasses() {
    synchronized (loadedClassesMap) {
      if (javaInstrumentation != null) {
        Class[] allLoadedClasses = javaInstrumentation.getAllLoadedClasses();
        for (Class<?> clazz : allLoadedClasses) {
          registerClass(clazz.getName(), clazz.getClassLoader(), loadedClassesMap);
        }
      }
    }
  }

  public void addLoadedClass(String className, ClassLoader cl) {
    synchronized (loadedClassesMap) {
      if (loadedClassesMap.isEmpty()) {
        addAllLoadedClasses();
      }
      registerClass(className, cl, loadedClassesMap);
    }
  }

  public void removeLoadedClass(String className, ClassLoader cl) {
    synchronized (loadedClassesMap) {
      removeClass(className, cl, loadedClassesMap);
    }
  }

  public void addTransformedClass(String className, ClassLoader cl) {
    synchronized (transformedClassesMap) {
      registerClass(className, cl, transformedClassesMap);
    }
  }

  public void removeTransformedClass(String className, ClassLoader cl) {
    synchronized (transformedClassesMap) {
      removeClass(className, cl, transformedClassesMap);
    }
  }

  private void registerClass(String className, ClassLoader cl,
      Map<String, List<WeakReference<ClassLoader>>> map) {
    List<WeakReference<ClassLoader>> list = map.get(className);
    if (list == null) {
      list = new ArrayList<>();
      map.put(className, list);
    } else {
      for (WeakReference<ClassLoader> wr : list) {
        if (cl == null) { // Bootstrap classloader
          if (wr == null) {
            return;
          }
        } else {
          if (wr != null && wr.get() == cl) {
            return;
          }
        }
      }
    }
    if (cl == null) {
      list.add(null);
    } else {
      list.add(new WeakReference<>(cl));
    }
  }

  private synchronized void removeClass(String className, ClassLoader cl,
      Map<String, List<WeakReference<ClassLoader>>> map) {
    ArrayList<WeakReference<ClassLoader>> list = (ArrayList<WeakReference<ClassLoader>>) map
        .get(className);
    if (list != null) {
      if (cl == null) { // Bootstrap classloader
        list.remove(null);
      } else {
        for (int i = 0; i < list.size(); i++) {
          WeakReference<ClassLoader> wr = list.get(i);
          if (wr != null && wr.get() == cl) {
            list.remove(wr);
            break;
          }
        }
      }
      if (list.isEmpty()) {
        map.remove(className);
      }
    }
  }


  @Override
  public boolean isLoadedByAnyClassLoader(String name) {
    List<WeakReference<ClassLoader>> classLoaders = this.loadedClassesMap.get(name);
    if (classLoaders == null) {
      return false;
    }
    for (WeakReference<ClassLoader> wk : classLoaders) {
      if (wk == null || wk.get() != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<ClassLoader> getClassLoadersLoading(String className) {
    List<WeakReference<ClassLoader>> classLoaders = this.loadedClassesMap.get(className);
    if (classLoaders == null) {
      return null;
    }
    List<ClassLoader> ret = new ArrayList<>();
    for (int i = 0; i < classLoaders.size(); i++) {
      WeakReference<ClassLoader> wk = classLoaders.get(i);
      if (wk == null) {
        ret.add(null);
      } else {
        ClassLoader cl = wk.get();
        if (cl != null) {
          ret.add(cl);
        }
      }
    }
    return ret;
  }

  @Override
  public Class<?> getClassIfLoadedByClassLoader(final String name, final ClassLoader cl) {
    List<WeakReference<ClassLoader>> classLoaders = this.loadedClassesMap.get(name);
    if (classLoaders == null) {
      return null;
    }

    for (WeakReference<ClassLoader> wk : classLoaders) {
      if (cl == null) {
        if (wk == null) { try {
            return Class.forName(name, false, null);
          } catch (ClassNotFoundException e) {
            // some classes like sun.reflect.GeneratedMethodAccessor cannot be loaded again
            return null;
          }}
      } else {
        if (wk != null && wk.get() == cl) {
        	 try {
                 return Class.forName(name, false, cl);
               } catch (ClassNotFoundException e) {
                 // some classes like sun.reflect.GeneratedMethodAccessor cannot be loaded again
                 return null;
               }
        }
      }
    }

    return null;
  }

  @Override
  public boolean isLoadedBy(String className, ClassLoader cl) {
    List<WeakReference<ClassLoader>> classLoaders = this.loadedClassesMap.get(className);
    if (classLoaders == null) {
      return false;
    }
    for (WeakReference<ClassLoader> wk : classLoaders) {
      if (cl == null) {
        if (wk == null) {
          return true;
        }
      } else {
        if (wk != null && wk.get() == cl) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Class[] getAllLoadedClasses() {
    if (javaInstrumentation == null) {
      return new Class[0];
    }
    return javaInstrumentation.getAllLoadedClasses();
  }
}
