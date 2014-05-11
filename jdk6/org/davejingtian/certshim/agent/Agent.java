/*
 * Agent.java
 * A Java agent used to instrument Java Sun SSL library calls
 * for Cert-shim project
 * Requirement:
 * JDK 6 + Javassist
 * Instrumented calls:
 * sun.security.ssl.X509TrustManagerImpl.checkIdentity
 * May 7, 2014
 * daveti@cs.uoregon.edu
 * http://davejingtian.org
 */
package org.davejingtian.certshim.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.security.cert.X509Certificate;

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
        System.out.println("Before loading class " + className);

	// Target class to instrument
        final String TARGET_CLASS =
		"sun/security/ssl/X509TrustManagerImpl";

        if (!className.equals(TARGET_CLASS)) {
          return null;
        }

        LoaderClassPath path = new LoaderClassPath(classLoader);
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        pool.appendClassPath(path);

        try {
          CtClass targetClass = pool.get(TARGET_CLASS.replace('/', '.'));
          System.out.println("Enhancing class " + targetClass.getName());
          CtMethod[] methods = targetClass.getDeclaredMethods();
          for (CtMethod method : methods) {
	    // Hook the function call
            if (!method.getName().contains("checkIdentity")) {
              continue;
            }
            System.out.println("Enhancing method " + method.getSignature());
/*
            String certShimCheckIdentity = 
		"org.davejingtian.certshim.agent.Agent.certShimCheckIdentity($$);";
            method.insertBefore(certShimCheckIdentity);
*/
	    // Let us force the algorithm to be "HTTPS" by default
	    method.insertBefore("{ System.out.println(\"daveti\"); System.out.println($1); System.out.println($3); if($3==null) $3=\"HTTPS\";}");
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

  // Instrumenting implementation
  public static void certShimCheckIdentity(String hostname,X509Certificate cert,String algorithm)
  {
    System.out.println("<<<My injected code>>>!");
  }
}

