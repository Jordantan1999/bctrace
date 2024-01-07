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
package bctrace.core.io.shiftleft.bctrace.asm.helper;

import bctrace.core.io.shiftleft.bctrace.asm.util.ASMUtils;
import java.util.ArrayList;
import bctrace.core.org.objectweb.asm.Opcodes;
import bctrace.core.org.objectweb.asm.tree.ClassNode;
import bctrace.core.org.objectweb.asm.tree.InsnList;
import bctrace.core.org.objectweb.asm.tree.InsnNode;
import bctrace.core.org.objectweb.asm.tree.MethodInsnNode;
import bctrace.core.org.objectweb.asm.tree.MethodNode;
import bctrace.core.org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * @author Ignacio del Valle Alles idelvall@shiftleft.io
 */
public class StartHelper extends Helper {

  public static void addTraceStart(int methodId, ClassNode cn, MethodNode mn, ArrayList<Integer> hooksToUse) {
    if (!isInstrumentationNeeded(hooksToUse)) {
      return;
    }
    InsnList il = new InsnList();
    for (Integer index : hooksToUse) {
      il.add(ASMUtils.getPushInstruction(methodId));
      if (ASMUtils.isStatic(mn.access) || mn.name.equals("<init>")) {
        il.add(new InsnNode(Opcodes.ACONST_NULL));
      } else {
        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
      }
      il.add(ASMUtils.getPushInstruction(index));
      il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
              "io/shiftleft/bctrace/runtime/Callback", "onStart",
              "(ILjava/lang/Object;I)V", false));
    }
    mn.instructions.insert(il);
  }
}