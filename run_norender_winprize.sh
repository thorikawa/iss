#!/bin/sh

rm issout.txt
java -Xmx512m -jar ISSVis.jar -exec ./universe.py -beta 72
java -Xmx512m -jar ISSVis.jar -exec ./universe.py -beta 74
java -Xmx512m -jar ISSVis.jar -exec ./universe.py -beta -70
java -Xmx512m -jar ISSVis.jar -exec ./universe.py -beta -74
java -Xmx512m -jar ISSVis.jar -exec ./universe.py -beta 70
java -Xmx512m -jar ISSVis.jar -exec ./universe.py -beta -72

