#!/bin/sh
cat *.java | grep ^import | sort | uniq > /tmp/Robot.java
cat *.java | grep -v ^import | >> /tmp/Robot.java
javac /tmp/Robot.java