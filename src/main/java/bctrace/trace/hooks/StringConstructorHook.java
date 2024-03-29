package bctrace.trace.hooks;


import java.security.ProtectionDomain;

import org.objectweb.asm.tree.MethodNode;

import bctrace.core.io.shiftleft.bctrace.Bctrace;
import bctrace.core.io.shiftleft.bctrace.filter.MethodFilter;
import bctrace.core.io.shiftleft.bctrace.hierarchy.UnloadedClass;
import bctrace.core.io.shiftleft.bctrace.hook.GenericMethodHook;
import bctrace.spi.io.shiftleft.bctrace.logging.Level;
import io.shiftleft.bctrace.runtime.listener.generic.GenericMethodReturnListener;


/**
 * A sample hook that logs a message every time a String constructor is invoked
 */
public class StringConstructorHook extends GenericMethodHook {

  public StringConstructorHook() {
    setFilter(new MethodFilter() {
      @Override
      public boolean acceptClass(String className, ProtectionDomain protectionDomain,
          ClassLoader cl) {
        return className.equals("java/lang/String");
      }

      @Override
      public boolean acceptMethod(UnloadedClass clazz, MethodNode methodNode) {
        return methodNode.name.equals("<init>");
      }
    });

    setListener(new GenericMethodReturnListener() {
      @Override
      public Object onReturn(int methodId, Class clazz, Object instance, Object[] args,
          Object ret) {
        Bctrace.getAgentLogger().log(Level.INFO, "Created String instance: \"" + instance + "\"");
        return ret;
      }
    });
  }
}