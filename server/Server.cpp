#include "thread.h"
#include "socketserver.h"
#include "socket.h"
#include <stdlib.h>
#include <time.h>
#include <list>
#include <vector>
#include <map>
#include "SharedObject.h"

using namespace Sync;

class SharedMap {
    public:
    std::map<std::string, int> map;
};

class ShareInt {
    public:
        int port;
};

class ServerThread : Thread {
    public:
    Socket * socket;
    bool runThread;
    Shared<SharedMap> * chatrooms = new Shared<SharedMap> ("chatrooms");
    Shared<ShareInt> * nextPort = new Shared<ShareInt>("nextPort");


    ServerThread(Socket * socket){
        this->socket = new Socket(*socket);
        this->runThread = true;
    }

    long ThreadMain(){
            ByteArray * request = new ByteArray();

            do {

                if (socket->Read(*request) == 0) {
                    runThread = false;
                    continue;
                }

                std::string response;

                // Handle create new Chat Room request
                if (request->ToString().find("Create") != std::string::npos){
                    // Calculate next port number n
                    int portNum = this->nextPort->get()->port++;
                    std::string name = request->ToString().substr(request->ToString().find(" ") + 1);
                    // Start new chatroom process with port number n
                    //TODO
                    // Add chatroom name and port number to map
                    this->chatrooms->get()->map[name] = portNum;
                    // Return chatroom name and port number in response
                    response =  name + " is now on port: " + std::to_string(portNum);
                }

                // handle join Chat Room request
                if (request->ToString().find("Join") != std::string::npos){
                    // Find chatroom port from map,
                    std::string name = request->ToString().substr(request->ToString().find(" ") + 1);
                    int portNum = this->chatrooms->get()->map[name];
                    // return port in response
                    response =  name + " is running on port: " + std::to_string(portNum);
                }

                // Handle get Chat Rooms request
                if (request->ToString().find("Get") != std::string::npos){
                    response =  "Running Chatrooms:\n";
                    //Return a list of chatrooms and port numbers if theyre public
                    std::map<std::string, int> m = this->chatrooms->get()->map;
                    for(std::map<std::string, int>::iterator it = m.begin(); it != m.end(); ++it) {
                        response += it->first + "\n";
                    }
                }

                //Send response to client
                ByteArray *bytes = new ByteArray(response);
                socket->Write(*bytes);
                delete bytes;

            } while (runThread);


            delete request;
            return 0l;
    }

    ~ServerThread(){
        this->runThread = false;
        delete this->socket;
        delete this->chatrooms;
        delete this->nextPort;
        Sync::FlexWait theEnd(1, &terminationEvent);
        theEnd.Wait();
    }

};

class MainThread : Thread {
    public:
        bool run;
        SocketServer * socketServer;
        int port;
        Shared<SharedMap> * chatrooms= new Shared<SharedMap>("chatrooms", true);
        Shared<ShareInt> * nextPort = new Shared<ShareInt>("nextPort", true);

    MainThread(){
        run = true;
        port = 2000;
        socketServer = new SocketServer(port);
    }

    long ThreadMain(){
        static std::vector<ServerThread*> threads;
        run = "true";
        do {
            Socket socket = socketServer->Accept();
            threads.push_back(new ServerThread(&socket));
        } while (run);
        delete socketServer;
        for (ServerThread *n : threads){
            delete n;
        }
    }

    ~MainThread(){
        run = false;
        socketServer->Shutdown();
        delete this->chatrooms;
        delete this->nextPort;
        Sync::FlexWait theEnd(1, &terminationEvent);
        theEnd.Wait();
    }
};


int main(void)
{
    std::string run;
    MainThread * mainThread = new MainThread();

    do {
        std::cout << "Enter X to close server: ";
        std::cin >> run;
        if(run == "X"){
            delete mainThread;
            continue;
        }
        else{
            std::cout << "Invalid input. Try again.." << std::endl;
        }
    } while(run != "X");


    return 0;
}
