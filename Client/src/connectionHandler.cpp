#include <connectionHandler.h>
#include <cstdlib>
#include "boost/lexical_cast.hpp"

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port) : host_(host), port_(port), io_service_(),
                                                                socket_(io_service_) {}

//destructor
ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to " << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception &e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp) {
            tmp += socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLine(std::string &line) {
    return getFrameAscii(line);
}

bool ConnectionHandler::sendLine(std::string &line) {
    return sendFrameAscii(line);
}

bool ConnectionHandler::getFrameAscii(std::string &frame) {
    char opcodeBytes[2];
    //Reading two first bytes from socket to opcodeBytes
    try {
        if (!getBytes(opcodeBytes, 2)) return false;
    } catch (std::exception &e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    //2 byte array to short convertor
    short opcode = bytesToShort(opcodeBytes);

    //ERR message
    if (opcode == 13) return errDecode(frame);
    //ACK message
    return ackDecode(frame);
}

bool ConnectionHandler::errDecode(std::string &frame) {
    char otherOpcodeBytes[2];
    try {
        if (!getBytes(otherOpcodeBytes, 2)) return false;
        short otherOpcode = bytesToShort(otherOpcodeBytes);
        string temp = "ERROR ";
        temp.append(std::to_string(otherOpcode));
        frame = temp;
        return true;
    } catch (std::exception &e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
}

bool ConnectionHandler::ackDecode(std::string &frame) {
    char otherOpcodeBytes[2];
    short otherOpcode;
    try {
        if (!getBytes(otherOpcodeBytes, 2)) return false;
        otherOpcode = bytesToShort(otherOpcodeBytes);
    } catch (std::exception &e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }

    string temp = "ACK ";
    temp.append(std::to_string(otherOpcode));
    string message;
    char ch;
    try {
        do {
            if (!getBytes(&ch, 1)) return false;
            message.append(1, ch);
        } while (ch != '\0');
    } catch (std::exception &e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    if (message.length() <= 1) frame = temp; //short ack message
    else { //long message - need to adjust
        if ((otherOpcode == 7) | (otherOpcode == 8)) { //special adjustment
            //frame message [] removal
            message = message.erase(0, 1);
            message.erase(message.length() - 1, 1);
            frame = messageParser(message, temp, otherOpcode);
        } else frame = temp + '\n' + message; //regular parameters
    }
    return true;
}

string ConnectionHandler::messageParser(std::string message, std::string temp, short otherOpcode) {
    int pos = message.find_first_of(',');
    string firstSen = message.substr(0, pos);
    string secondSen;
    if (otherOpcode == 8) {
        secondSen = message.substr(pos + 2, message.length() - 1 - (pos + 2));
        return temp + '\n' + firstSen + '\n' + secondSen;
    } else { //opcode==7
        string temp2 = message.substr(pos + 2, message.length());
        int pos2 = temp2.find_first_of(',');
        secondSen = temp2.substr(0, pos2);
        string thirdSen = temp2.substr(pos2 + 2, temp2.length() - 1 - (pos2 + 2));
        return temp + '\n' + firstSen + '\n' + secondSen + '\n' + thirdSen;
    }
}

short ConnectionHandler::bytesToShort(char *bytesArr) {
    short result = (short) ((bytesArr[0] & 0xff) << 8);
    result += (short) (bytesArr[1] & 0xff);
    return result;
}


bool ConnectionHandler::sendFrameAscii(const std::string &frame) {
    size_t pos = frame.find_first_of(' '); //the position of the first ' '
    string opcode;
    if (pos != string::npos) opcode = frame.substr(0, pos); //message with space
    else opcode = frame; //command that is without a space - the whole command
    char opcodeBytes[2];

    if (opcode == "ADMINREG") shortToBytes((short) 1, opcodeBytes);
    if (opcode == "STUDENTREG") shortToBytes((short) 2, opcodeBytes);
    if (opcode == "LOGIN") shortToBytes((short) 3, opcodeBytes);
    if (opcode == "LOGOUT") shortToBytes((short) 4, opcodeBytes);
    if (opcode == "COURSEREG") shortToBytes((short) 5, opcodeBytes);
    if (opcode == "KDAMCHECK") shortToBytes((short) 6, opcodeBytes);
    if (opcode == "COURSESTAT") shortToBytes((short) 7, opcodeBytes);
    if (opcode == "STUDENTSTAT") shortToBytes((short) 8, opcodeBytes);
    if (opcode == "ISREGISTERED") shortToBytes((short) 9, opcodeBytes);
    if (opcode == "UNREGISTER") shortToBytes((short) 10, opcodeBytes);
    if (opcode == "MYCOURSES")shortToBytes((short) 11, opcodeBytes);

    //write the opcode of the action to socket
    if (!sendBytes(opcodeBytes, 2)) return false;
    //write the rest of the message if exists
    if (opcode != frame) { //long command
        if ((opcode == "ADMINREG") | (opcode == "STUDENTREG") | (opcode == "LOGIN") | (opcode == "STUDENTSTAT")) {
            std::vector<string> wordsVector = split(frame.substr(pos + 1, frame.length()), ' ');
            for (const string &word: wordsVector) {
                bool result = sendBytes(word.c_str(), word.length() + 1); //adds '/0' to each word
                if (!result) return false;
            }
        } else { //other long commands
            string number = frame.substr(pos + 1, frame.length());
            char otherBytes[2];
            shortToBytes(boost::lexical_cast<short>(number), otherBytes);
            bool result = sendBytes(otherBytes, 2);
            if (!result) return false;
        }
    }
    return true;
}

void ConnectionHandler::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}


std::vector<string> ConnectionHandler::split(const string &str, char delimiter) {
    std::vector<string> toReturn;
    std::stringstream ss(str); // Turn the string into a stream.
    string word;
    while (getline(ss, word, delimiter)) {
        toReturn.push_back(word);
    }
    return toReturn;
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try {
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}
