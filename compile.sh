#!/bin/bash


javac -source 1.8 -target 1.8 -d bin -cp bin src/GridDiagram.java
javac -source 1.8 -target 1.8 -d bin -cp bin -Xlint:deprecation src/GridAlgorithm.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/Energy.java
javac -source 1.8 -target 1.8 -d bin -cp bin -Xlint:deprecation src/WangLandau.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/WangLandauT.java
javac -source 1.8 -target 1.8 -d bin -cp bin -Xlint:deprecation src/RunWangLandau.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/AnalyzeGrids.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/testingCode.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/ExtendWeights.java
