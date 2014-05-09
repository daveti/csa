csa

Cert Shim Agent

A Java Agent for Cert-Shim

Based on JDK 6

May 8, 2014

daveti@cs.uoregon.edu

http://davejingtian.org

Some backgroud story...

Certshim covers C/C++/PHP/Python as either OpenSSL or GnuTLS is used directly or wrapped up, like libcurl. Accordingly, certshim is able to fix most of the bugs mentioned in the most dangerous code (TMDC) paper. So what about Java? Here it goes:

The basic issue mentioned in TMDC for Java is the usage of SSLSocketFacotry(). Using this function to create SSL socket may leave an important SSL parameter ‘algorithm’ to be null. Unfortunately, checkIdentity(), used for hostname verification, within the default Sun SSL implementation would NOT report any error if ‘algorithm==null’, meaning hostname verification is DISABLED. Can we use similar idea to fix that, like PRE_LOAD in C? Yes, we can!

Since Java 1.5, Java instrument API is introduced. With this API, we can inject byte code dynamically into certain function calls during runtime without changing the source code of the instrumented function. So the idea for certshim for java is that we instrument checkIdentity() to force ‘algorithm’ to be NOT null even if it is passed null (default value gonna be HTTPS). By doing this, we will not be afraid of the usage of SSLSocketFactory() and the corresponding wrappers.

I have implemented this thing (called certShimAgent) on JDK 1.6 and verified that it works. Comparing with traditional Java app invoking - ‘java app’, now users can run ‘java -javaagent:certShimAgent app’ without any code change to guarantee the hostname verification for Java SSL apps.

Some thoughts:
1. Need to create different agent for different version of JDK. For JDK 1.7, ASM code is used as instrumenting instead of byte code. But the idea is the same.
2. Need to instrument/hook different functions for different version of JDK. For JDK 1.7, according to TMDC paper, the other 2 functions need to be hooked besides checkIdentity().
3. Currently we only consider the case mentioned in TMDC paper - SSLSocketFactory() and Sun SSL implementation. Not sure if there are other SSL implementations. But it seems Sun’s default one should have a good coverage.

