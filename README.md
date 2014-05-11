csa

Cert Shim Agent

A Java agent used to instrument the Java library calls dynamically without changing the JDK source code

It is designed for Java SSL implementation defects related with SSLSocketFactory() and checkIdentity() in "The most dangerous code in the world" and part of cert-shim project by OSIRIS Lab SSL Team.

With csa, we can fix:

1. No hostname verification because of SSLSocketFactory()/checkIdentity() in JDK 6
2. No hostname verification because of SSLSocketFactory/checkTrusted()/checkIdentity() in JDK 7

May 11, 2014

daveti@cs.uoregon.edu

http://davejingtian.org
