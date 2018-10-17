#!/bin/bash


javac -source 1.8 -target 1.8 -d bin -cp bin src/GridDiagram.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/GridAlgorithm.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/Energy.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/WangLandau.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/RunWangLandau.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/AnalyzeGrids.java
javac -source 1.8 -target 1.8 -d bin -cp bin src/testingCode.java