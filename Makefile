JDKPATH = /Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home
LIBPATH = /Users/Nicole/Documents/IntelliJ/BufferManager/lib/bufmgrAssign.jar

CLASSPATH = .:..:$(LIBPATH)
BINPATH = $(JDKPATH)/bin
JAVAC = $(JDKPATH)/bin/javac 
JAVA  = $(JDKPATH)/bin/java 

PROGS = xx

all: $(PROGS)

compile:src/*/*.java
	$(JAVAC) -cp $(CLASSPATH) -d bin src/*/*.java

xx : compile
	$(JAVA) -cp $(CLASSPATH):bin tests.BMTest
