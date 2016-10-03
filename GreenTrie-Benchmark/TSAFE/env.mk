# Contains common Makefile environment information.



##############################################################################
#
# MACHINE-DEPENDENT PROPERTIES
#

ROOT_PATH = .

JAVA = java
JAVAC = javac
JAVADOC = javadoc
JAR = jar
##############################################################################

#
# CLASSPATH
#

# Classpath of add-on libraries
LIB_CLASSPATH = $(ROOT_PATH)/lib/figio.jar

# Final classpath
CLASSPATH = $(ROOT_PATH):$(LIB_CLASSPATH)

