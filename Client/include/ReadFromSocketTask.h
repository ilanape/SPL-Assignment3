#ifndef CLIENT_READFROMSOCKETTASK_H
#define CLIENT_READFROMSOCKETTASK_H

#include "connectionHandler.h"


class ReadFromSocketTask {
private:
    bool& _shouldTerminate;
    ConnectionHandler& _ch;
    std::string& _canContinue;

public:
    ReadFromSocketTask(bool& shouldTerminate, ConnectionHandler& ch, std::string& canContinue);

    void run();

};


#endif //CLIENT_READFROMSOCKETTASK_H
