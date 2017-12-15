#include "thread.h"
#include "socketserver.h"
#include "socket.h"
#include <stdlib.h>
#include <time.h>
#include <list>
#include <vector>
#include <map>
#include "SharedObject.h"
#include "Semaphore.h"
#include <bits/stdc++.h>
using namespace Sync;

class ServerThread : Thread {

    public:
        bool running = true;
        Socket * socket;
        Semaphore * s1;
        Semaphore * s2;
        std::string fileID;

    ServerThread(Socket * socket){
        this->socket = new Socket(*socket);
        s1 = new Semaphore("block");
        s2 = new Semaphore("mutex");
        fileID = "chatrooms.txt";
    }

    long ThreadMain(){
        ByteArray * request = new ByteArray();
        socket->Read(*request);
        std::string response;

        // Handle get Chat Rooms request
        if (request->ToString().find("Get") != std::string::npos){

            s2->Wait();
            s1->Wait();
            std::string response = "[";
            std::string str;
            std::ifstream file(fileID.c_str());
            while(std::getline(file, str)){
                response+=str;
                response+=", ";
            }
            response = response.substr(0,response.size()-2);
            response +="]";
            s1->Signal();
            s2->Signal();
            ByteArray resp(response);
            socket->Write(resp);
        }
        // Handle create new Chat Room request
        else if (request->ToString().find("Create") != std::string::npos){

            s1->Wait(); // Block Wait
            int portNum = 2001; // Calculate next port number n
            std::string str;
            std::ifstream file(fileID.c_str());
            while(std::getline(file, str)){
                portNum++;
            }

            std::string name = request->ToString().substr(request->ToString().find(" ") + 1);

            // Start new chatroom process with port number n
            //TODO
            //system("./ChatProcess " + portNum);

            // Return chatroom name and port number in response
            response =  "{ name: " + name + ", port: " + std::to_string(portNum) + " }";

            // Add chatroom name and port number to file
            std::ofstream newOut;
            newOut.open(fileID.c_str(), std::fstream::app);
            newOut<< response<<"\n";
            newOut.close();

            s1->Signal(); // Block Signal
            s2->Signal();
            ByteArray resp(response);
            socket->Write(resp);
        }
        else if (request->ToString().find("Join") != std::string::npos){

            std::string name = request->ToString().substr(request->ToString().find(" ") + 1);

            s2->Wait();
            s1->Wait();
            std::string response = "[";
            std::string str;
            std::ifstream file(fileID.c_str());
            while(std::getline(file, str)){
                std::string extractedName = str.substr(str.find(":") + 2, name.length());
                if (name == extractedName){
                    response+=str;
                }
            }
            response +="]";
            s1->Signal();
            s2->Signal();
            ByteArray resp(response);
            socket->Write(resp);
        }
        // Invalid request
        else {
            response =  "Invalid Request\n";
        }
        //Send response to client
        running = false;
        delete request;
        return 0;
    }

    ~ServerThread(){
        delete s1;
        delete s2;
        delete socket;
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
            for (int i = threads.size()-1; i >= 0; i--){
                if(!threads[i]->running){
                    delete (threads[i]);
                    threads.erase(threads.begin()+i);
                }
            }
        } while (run);

        // Delete Pointer
        delete socketServer;
        for (int i = threads.size()-1; i >= 0; i--){
            delete(threads[i]);
        }
        return 0;
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
    Semaphore * s1 = new Semaphore("block", 1, true);
    Semaphore * s2 = new Semaphore("mutex", 1, true);

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
    delete s1;
    delete s2;
    return 0;
}
