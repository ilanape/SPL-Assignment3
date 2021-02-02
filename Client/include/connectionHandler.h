#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__
                                           
#include <string>
#include <iostream>
#include <boost/asio.hpp>

using boost::asio::ip::tcp;

class ConnectionHandler {
private:
	const std::string host_;
	const short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	tcp::socket socket_;
 
public:
    ConnectionHandler(std::string host, short port);
    virtual ~ConnectionHandler();
 
    // Connect to the remote machine
    bool connect();
 
    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);
 
	// Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);
	
    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string& line);
	
	// Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string& line);
 
    // Get Ascii data from the server
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(std::string& frame);
 
    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const std::string& frame);
	
    // Close down the connection properly.
    void close();

    //decodes an ERR message from the server
    bool errDecode(std::string &frame);

    //decodes an ACK message from the server
    bool ackDecode(std::string &frame);

    //Decode 2 bytes to short
    short bytesToShort(char* bytesArr);

    //Encode short to 2 bytes array
    void shortToBytes(short num, char* bytesArr);

    //splits string by given delimiter
    std::vector<std::string> split(const std::string& str, char delimiter);

    //builds frame message for opcodes 7,8
    std::string messageParser(std::string message,std::string temp, short opcode);
 
}; //class ConnectionHandler
 
#endif