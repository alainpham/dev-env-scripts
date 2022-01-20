# Set JDK installation directory according to selected Java compiler

export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")
