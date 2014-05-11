/*
 * Agent.java
 * A Java agent used to instrument Java Sun SSL library calls
 * for Cert-shim project
 * Requirement:
 * JDK 7 + Javassist
 * Instrumented calls:
 * sun.security.ssl.X509TrustManagerImpl.checkIdentity
 * javax.net.ssl.SSLParameters.setEndpointIdentificationAlgorithm
 * May 11, 2014
 * daveti@cs.uoregon.edu
 * http://davejingtian.org
 */
package org.davejingtian.certshim.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

public class Agent {

  public static void premain(String agentArgs, Instrumentation instr) {
    System.out.println("daveti: Agent starts!");
    instr.addTransformer(new ClassFileTransformer() {

      @Override
      public byte[] transform(ClassLoader classLoader, String className, Class<?> arg2, ProtectionDomain arg3,
          byte[] bytes)
          throws IllegalClassFormatException {

	// Multiple instrumentation
	boolean isTargetClass = false;
	boolean isTargetClass1 = false;

        System.out.println("Before loading class " + className);

	// Target class to instrument
        final String TARGET_CLASS =
		"sun/security/ssl/X509TrustManagerImpl";
	// The other target class to instrument
	final String TARGET_CLASS_1 =
		"javax/net/ssl/SSLParameters";

        if (className.equals(TARGET_CLASS)) {
		isTargetClass = true;
		System.out.println("daveti: debug - target");
	}
	else if (className.equals(TARGET_CLASS_1)) {
		isTargetClass1 = true;
		System.out.println("daveti: debug - target1");
	}
	else {
		return null;
        }

        LoaderClassPath path = new LoaderClassPath(classLoader);
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        pool.appendClassPath(path);

        try {
	  System.out.println("daveti: debug - try");
          CtClass targetClass = null;
	  String targetFunction = null;
	  String instrumentCode = null;

	  if (isTargetClass) {
		targetClass = pool.get(TARGET_CLASS.replace('/', '.'));
		targetFunction = "checkIdentity";
		instrumentCode = "{System.out.println(\"daveti-target\"); System.out.println($1); System.out.println($3); if($3==null) $3=\"HTTPS\";}";
		System.out.println("daveti: debug - try target");
	  }
	  else if (isTargetClass1) {
		targetClass = pool.get(TARGET_CLASS_1.replace('/', '.'));
		targetFunction = "setEndpointIdentificationAlgorithm";
		instrumentCode = "{System.out.println(\"daveti-target1\"); System.out.println($1); if($1==null) $1=\"HTTPS\";}";
		System.out.println("daveti: denbug - try target1");
	  }

	  //System.out.println("daveti: debug - WTF");
          System.out.println("Enhancing class " + targetClass.getName());
	  //System.out.println("daveti: WTH....");
          CtMethod[] methods = targetClass.getDeclaredMethods();
          for (CtMethod method : methods) {
	    // Hook the function call
            if (!method.getName().contains(targetFunction)) {
              continue;
            }
            System.out.println("Enhancing method " + method.getSignature());
	    // Let us force the algorithm to be "HTTPS" by default
	    method.insertBefore(instrumentCode);
          }
          System.out.println("Enhanced bytecode");

          return targetClass.toBytecode();
        }
        catch (CannotCompileException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        catch (IOException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        catch (NotFoundException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }

    });
  }
}

