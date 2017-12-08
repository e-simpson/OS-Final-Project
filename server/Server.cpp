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
        std::string rooms[100];
};

class ShareInt {
    public:
        int port;
};

class ServerThread : Thread {
    public:
    Socket * socket;
    bool runThread;
    Shared<ShareInt> nextPort = Shared<ShareInt>("nextPort");
    Shared<SharedMap> chatrooms = Shared<SharedMap>("chatrooms");

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
                    int portNum = this->nextPort.get()->port++;
                    std::string name = request->ToString().substr(request->ToString().find(" ") + 1);
                    // Start new chatroom process with port number n
                    //TODO
                    //system("./Chatroom " + portNum);
                    // Add chatroom name and port number to map
                    //TODO
                    chatrooms->rooms[portNum-2001] = name;
                    // Return chatroom name and port number in response
                    response =  name + " is now on port: " + std::to_string(portNum);
                }

                // handle join Chat Room request
                else if (request->ToString().find("Join") != std::string::npos){
                    // Find chatroom port from map,
                    std::string name = request->ToString().substr(request->ToString().find(" ") + 1);
                    // TODO
                    int portNum = 0;
                    for (int i = 0; i < this->nextPort.get()->port - 2001; i++) {
                        if (name == chatrooms->rooms[i]) {
                            portNum = i + 2001;
                            response = name + " is running on port: " + std::to_string(portNum);
                        } else {
                            response = "No chatroom named " + name;
                        }
                    }
                }

                // Handle get Chat Rooms request
                else if (request->ToString().find("Get") != std::string::npos){
                    response =  "Running Chatrooms:\n";
                    // Return a list of chatrooms and port numbers if theyre public
                    // TODO
                    for (int i = 0; i < this->nextPort.get()->port - 2001; i++) {

                       std::string chatInfo = chatrooms->rooms[i] + " " + std::to_string(i+2001) + "\n";
                        response += chatrooms->rooms[i] + "\n";
                    }
                }

                // Invalid request
                else {
                    response =  "Invalid Request\n";
                }

                //Send response to client
                ByteArray *bytes = new ByteArray(response);
                socket->Write(*bytes);
                delete bytes;

            } while (runThread);


            delete request;
            delete this->socket;
            return 0l;
    }

    ~ServerThread(){
        this->runThread = false;
        Sync::FlexWait theEnd(1, &terminationEvent);
        theEnd.Wait();
    }

};

class MainThread : Thread {
    public:
        bool run;
        SocketServer * socketServer;
        int port;


    MainThread(){
        run = true;
        port = 2000;
        socketServer = new SocketServer(port);
    }

    long ThreadMain(){
        // Vector for worker threads
        static std::vector<ServerThread*> threads;

        // Loop  for main thread
        run = "true";
        do {
            // Create socket an pass to new thread
            Socket socket = socketServer->Accept();
            threads.push_back(new ServerThread(&socket));
        } while (run);

        // Delete Pointer
        delete socketServer;
        for (ServerThread *n : threads){
            delete n;
        }
        return 0l;
    }

    ~MainThread(){
        run = false;
        socketServer->Shutdown();
        Sync::FlexWait theEnd(1, &terminationEvent);
        theEnd.Wait();
    }
};


int main(void)
{
    std::string run;
    MainThread * mainThread = new MainThread();
    Shared<ShareInt> nextPort = Shared<ShareInt>("nextPort", true);
    Shared<SharedMap> chatrooms = Shared<SharedMap>("chatrooms", true);
    nextPort.get()->port = 2001;

    chatrooms->rooms[0] = "Hello";



    do {
        std::cout << "Enter X to close server: ";
        std::cin >> run;
        if(run == "X"){
            continue;
        }
        else{
            std::cout << "Invalid input. Try again.." << std::endl;
        }
    } while(run != "X");

    delete mainThread;
    return 0;
}
