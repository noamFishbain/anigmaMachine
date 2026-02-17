#!/bin/bash

# Clone the repository
git clone https://github.com/noamFishbain/anigmaMachine ./enigma

# Enter the project folder
cd enigma

# Build the project using Maven (no 'call' needed on Mac)
mvn clean install

# Navigate to the correct folder (Use forward slash / for Mac)
cd enigma-console/target

# Run the JAR file
java -jar enigma-machine-ex2.jar