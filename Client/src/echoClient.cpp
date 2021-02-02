#include <stdlib.h>
#include <connectionHandler.h>
#include "ReadFromSocketTask.h"
#include <thread>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main(int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    std::string canContinue = "";
    bool shouldTerminate = false;

    //read from socket task
    ReadFromSocketTask task(shouldTerminate, connectionHandler, canContinue);
    std::thread thread1(&ReadFromSocketTask::run, &task);

    while (!shouldTerminate) {
        //read from keyboard and write to socket task
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if (!connectionHandler.sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        if (line == "LOGOUT") {
            while (canContinue == "") {} //busy wait
            if (canContinue == "no") thread1.join();
            else canContinue = "";
        }
    }
    return 0;
}
