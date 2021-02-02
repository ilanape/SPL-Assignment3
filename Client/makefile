CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread -pthread

# All Targets
all: bin/BGRSclient
	g++ -o bin/BGRSclient bin/connectionHandler.o bin/ReadFromSocketTask.o bin/echoClinet.o $(LDFLAGS)
# Tool invocations
bin/BGRSclient: bin/connectionHandler.o bin/ReadFromSocketTask.o bin/echoClient.o
@echo 'Building target: Client'
@echo 'Invoking: C++ Linker'
	g++ -Wall -Weffc++ -o bin/BGRSclient
@echo 'Finished building target: BGRSclient'

bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/ReadFromSocketTask.o: src/ReadFromSocketTask.cpp
	g++ $(CFLAGS) -o bin/ReadFromSocketTask.o src/ReadFromSocketTask.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClinet.o src/echoClient.cpp

#Clean the build directory
clean:
	rm -f bin/*

