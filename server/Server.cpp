#include "thread.h"
#include "socketserver.h"
#include "socket.h"
#include <stdlib.h>
#include <time.h>
#include <list>
#include <vector>
#include <map>

using namespace Sync;

class ServerThread : Thread {
    public:
    Socket * socket;
    bool runThread;


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
                    // Start new chatroom process with port number n
                    // Add chatroom name and port number to map
                    // Return chatroom name and port number in response
                    response =  "****" + request->ToString() + "****";
                }

                // handle join Chat Room request
                if (request->ToString().find("Join") != std::string::npos){
                    // Find chatroom port from map,
                    // return port in response
                    response =  "****" + request->ToString() + "****";
                }

                // Handle get Chat Rooms request
                if (request->ToString().find("Get") != std::string::npos){
                    //Return a list of chatrooms and port numbers if theyre public
                    response =  "****" + request->ToString() + "****";
                }

                //Send response to client
                ByteArray *bytes = new ByteArray(response);
                socket->Write(*bytes);

            } while (runThread);


            delete request;
            return 0l;
    }

    ~ServerThread(){
        this->runThread = false;
        delete this->socket;
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
