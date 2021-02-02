#include <string>
#include <iostream>
#include "ReadFromSocketTask.h"


ReadFromSocketTask::ReadFromSocketTask(bool& shouldTerminate, ConnectionHandler& ch,std::string& canContinue):_shouldTerminate(shouldTerminate), _ch(ch),_canContinue(canContinue){}

void ReadFromSocketTask::run() {
    while(!_shouldTerminate) {
        std::string answer;

        if (!_ch.getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            _shouldTerminate=true;
            break;
        }
        std::cout << answer << std::endl;

        if (answer == "ACK 4") {
            _shouldTerminate=true;
            _canContinue="no";
        }
        if (answer=="ERROR 4") _canContinue="yes";
    }
}