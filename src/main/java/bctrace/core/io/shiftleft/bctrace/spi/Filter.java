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
package bctrace.core.io.shiftleft.bctrace.spi;

import java.security.ProtectionDomain;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A filter determines which classes and methods are instrumented.
 * <br><br>
 * If the class is transformable, the framework performs an initial query to the
 * {@link #instrumentClass(String, ProtectionDomain, ClassLoader) instrumentClass}
 * method. If this return <code>true</code> the filter
 * {@link #instrumentMethod(ClassNode, MethodNode) instrumentMethod} method will
 * be invoked once per non abstract nor native method in the class. Invocations
 * returning <code>true</code> lead to a hook insertions into the bytecode of
 * the method.
 *
 * @author Ignacio del Valle Alles idelvall@shiftleft.io
 */
public interface Filter {

  /**
   * Whether or not instrument the methods of a class.
   *
   * @param className
   * @param protectionDomain
   * @param cl
   * @return
   */
  public boolean instrumentClass(String className, ProtectionDomain protectionDomain, ClassLoader cl);

  /**
   * Whether or not instrument the specified method.
   *
   * @param classInfo
   * @param mn
   * @return
   */
  public boolean instrumentMethod(HierarchyClassInfo classInfo, MethodNode mn);
}
