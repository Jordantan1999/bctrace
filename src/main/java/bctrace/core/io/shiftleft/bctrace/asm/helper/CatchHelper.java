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
import bctrace.core.org.objectweb.asm.tree.InsnList;
import bctrace.core.org.objectweb.asm.tree.InsnNode;
import bctrace.core.org.objectweb.asm.tree.LabelNode;
import bctrace.core.org.objectweb.asm.tree.MethodInsnNode;
import bctrace.core.org.objectweb.asm.tree.MethodNode;
import bctrace.core.org.objectweb.asm.tree.TryCatchBlockNode;
import bctrace.core.org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * @author Ignacio del Valle Alles idelvall@shiftleft.io
 */
public class CatchHelper extends Helper {

  public static LabelNode insertStartNode(MethodNode mn, ArrayList<Integer> hooksToUse) {
    if (!isInstrumentationNeeded(hooksToUse)) {
      return null;
    }
    LabelNode ret = new LabelNode();
    mn.instructions.insert(ret);
    return ret;
  }

  public static void addTraceThrowableUncaught(int methodId, MethodNode mn, LabelNode startNode, ArrayList<Integer> hooksToUse) {
    if (!isInstrumentationNeeded(hooksToUse)) {
      return;
    }

    InsnList il = mn.instructions;

    LabelNode endNode = new LabelNode();
    il.add(endNode);

    addCatchBlock(methodId, mn, startNode, endNode, hooksToUse);
  }

  private static void addCatchBlock(int methodId, MethodNode mn, LabelNode startNode, LabelNode endNode, ArrayList<Integer> hooksToUse) {

    InsnList il = new InsnList();
    LabelNode handlerNode = new LabelNode();
    il.add(handlerNode);
    il.add(getThrowableTraceInstructions(methodId, mn, hooksToUse));
    il.add(new InsnNode(Opcodes.ATHROW));

    TryCatchBlockNode blockNode = new TryCatchBlockNode(startNode, endNode, handlerNode, "java/lang/Throwable");
    mn.tryCatchBlocks.add(blockNode);
    mn.instructions.add(il);
  }

  private static InsnList getThrowableTraceInstructions(int methodId, MethodNode mn, ArrayList<Integer> hooksToUse) {
    InsnList il = new InsnList();
    for (int i = hooksToUse.size() - 1; i >= 0; i--) {
      Integer index = hooksToUse.get(i);
      il.add(new InsnNode(Opcodes.DUP)); // dup throwable
      il.add(ASMUtils.getPushInstruction(methodId)); // method id
      if (ASMUtils.isStatic(mn.access) || mn.name.equals("<init>")) { // instance
        il.add(new InsnNode(Opcodes.ACONST_NULL));
      } else {
        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
      }
      il.add(ASMUtils.getPushInstruction(index)); // hook id
      il.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
              "io/shiftleft/bctrace/runtime/Callback", "onFinishedThrowable",
              "(Ljava/lang/Throwable;ILjava/lang/Object;I)V", false));
    }
    return il;
  }
}
