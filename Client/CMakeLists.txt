cmake_minimum_required(VERSION 3.17)
project(Client)

set(CMAKE_CXX_STANDARD 14)
#set(=-c -Wall -Weffc++ -g -std=c++11 -Iinclude -lboost_system)

include_directories(include)

link_libraries("-lboost_system")
link_libraries(-pthread)

add_executable(Client
        include/connectionHandler.h
        src/connectionHandler.cpp
        src/echoClient.cpp src/ReadFromSocketTask.cpp include/ReadFromSocketTask.h)
