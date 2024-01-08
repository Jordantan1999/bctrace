package bctrace.core.io.shiftleft.bctrace.hook.util;

import java.security.ProtectionDomain;

import org.objectweb.asm.tree.MethodNode;

import bctrace.core.io.shiftleft.bctrace.asm.util.ASMUtils;
import bctrace.core.io.shiftleft.bctrace.filter.MethodFilter;
import bctrace.core.io.shiftleft.bctrace.hierarchy.UnloadedClass;
import bctrace.core.io.shiftleft.bctrace.hook.GenericMethodHook;
import io.shiftleft.bctrace.runtime.listener.generic.GenericMethodStartListener;

public abstract class MainMethodStartHook extends
    GenericMethodHook<MethodFilter, GenericMethodStartListener> {

  private volatile boolean active = true;

  public MainMethodStartHook() {
    setFilter(
        new MethodFilter() {
          @Override
          public boolean acceptClass(String className, ProtectionDomain protectionDomain,
              ClassLoader cl) {
            return active;
          }

          @Override
          public boolean acceptMethod(UnloadedClass clazz, MethodNode mn) {
            return ASMUtils.isStatic(mn.access) && ASMUtils.isPublic(mn.access) && mn.name
                .equals("util")
                && mn.desc.equals("([Ljava/lang/String;)V");
          }
        }
    );

    setListener(
        new GenericMethodStartListener() {
          @Override
          public synchronized void onStart(int methodId, Class clazz, Object instance,
              Object[] args) {
            if (active) {
              onMainStart(clazz.getName(), (String[]) args[0]);
              active = false;
            }
          }
        }
    );
  }

  protected abstract void onMainStart(String className, String[] args);
}
