#include<iostream>
#include <fstream>
#include <sstream>
#include <ctime>
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
#include <algorithm>
#include <string>

using namespace Sync;

class ChatThread: Thread{
public:
    int threadID = 0;
    bool threadrunning = true;
    bool processRunning = true;
    Semaphore * s1;
    Semaphore * s2;
    Socket currentSocket;
    SocketServer * socketServer;
    std::string fileID;

    ChatThread(Socket currSocket, SocketServer * sockServer, std::string chat, int threadCount): currentSocket(currSocket), socketServer(sockServer)  {
        fileID = chat;
        std::string sem1 = "s1" + chat;
        std::string sem2  ="s2" + chat;
        fileID+=".txt";
        threadID = threadCount;
        s1 = new Semaphore(sem1);
        s2 = new Semaphore(sem2);
    }

    long ThreadMain() override{
        //Awaits a message from clients to the socket
        ByteArray receivedMessage = ByteArray();
        std::cout<<"Awaiting communtication...\n";
        currentSocket.Read(receivedMessage);
        std::string message = receivedMessage.ToString();
        std::cout<<"Message is " + message<<"\n";
        //Invalid message
        if(message.empty()){
            std::string err = "Invalid message";
            ByteArray error(err);
            currentSocket.Write(error);
            threadrunning = false;
            return 0;
        }
        //Client is attempting to retrieve the messages
        //Wait on the appropriate semaphores before reading the message file
        std::string prefix = message.substr(0,3);
        if(prefix == "Get"){
            std::cout<<"Retrieving messages...\n";
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
            currentSocket.Write(resp);
        }
            //A Client is writing a new message to the chatroom
        else if(message.find("Write") != std::string::npos){
            std::ofstream newOut;
            //Timestamp for the message
            std::time_t result = std::time(nullptr);
            //Parsing the user and the message contents
            std::string user = message.substr(0, message.find("Write")-1);
            std::string response = "Writing message";
            ByteArray writeResponse(response);
            std::cout<<response<<"\n";
            //The messages are stored in JSON for easy client-side parsing
            std::string value="{message:\"";
            value += message.substr((message.find("Write") + 6));
            value+="\", time:\"";
            value+=std::to_string(result);
            value+="\", user:\"";
            value+=user;
            value+="\"}";
            s1->Wait();
            newOut.open(fileID.c_str(), std::fstream::app);
            newOut<<value<<"\n";
            newOut.close();
            s1->Signal();
            s2->Signal();
            currentSocket.Write(writeResponse);
        }
            //Chatroom is being shut down
        else if(message == "Kill"){
            std::string resp = "Shutting down server";
            std::cout<<resp<<"\n";
            ByteArray killresponse(resp);
            currentSocket.Write(killresponse);
            processRunning = false;
        }
        else{
            std::cout<<"Unknown transaction\n";
        }
        //Marking that the thread has finished so that the main thread can delete it
        std::cout<<"Transaction complete for thread " + std::to_string(threadID) + "\n";
        threadrunning = false;
        return 0;
    }

    ~ChatThread() override {
        delete s1; delete s2;
        Sync::FlexWait theEnd(1, &terminationEvent);
        theEnd.Wait();
    }

    bool isRunning(){
        return threadrunning;
    }

    bool continueProcess(){
        return processRunning;
    }
};

class ChatProcess{
public:
    //Vector to hold pointers to all the currently operating threads
    std::vector<ChatThread *> runningThreads;
    Semaphore * s1 = new Semaphore("s1", 1, true);
    Semaphore * s2 = new Semaphore("s2", 0, true);
    bool processrunning;
    int threadcount;
    std::string chatroom;

    ChatProcess(int portnum){
        threadcount = 0;
        processrunning = true;
        SocketServer sockServ(portnum);
        //Custom names for the semaphores based on the chatroom port so that multiple chatroom processes can be supported
        chatroom = "messages" + std::to_string(portnum);
        std::string sem1 = "s1" + chatroom;
        std::string sem2  ="s2" + chatroom;
        s1 = new Semaphore(sem1, 1, true);
        s2 = new Semaphore(sem2, 1, true);
        std::cout<<"ChatProcess initialized on port: " + std::to_string(portnum) + "\n";
        this->listen(sockServ);
    }

    void listen(SocketServer socketServer){
        try{
            while(processrunning){
                Socket currSocket = socketServer.Accept();
                std::cout<<"Connection established\n";
                runningThreads.push_back(new ChatThread(currSocket, &socketServer, chatroom, threadcount));
                threadcount++;
                //Checking if any thread has received a kill message, thus triggering the process to end
                for (int i = runningThreads.size()-1; i >= 0; i--){
                    if(!runningThreads[i]->continueProcess()){
                        this->processrunning=false;
                    }
                    //Checking for finished threads and deleting them
                    if(!runningThreads[i]->isRunning()){
                        std::cout << "Stopping and deleting thread " << i << "\n";
                        delete (runningThreads[i]);
                        runningThreads.erase(runningThreads.begin()+i);
                        threadcount--;
                    }
                }
            }
        }
        catch(int e){}
        std::cout<<"Exiting server\n";
        delete s1;
        delete s2;
        //Cleaning up all threads
        for (int i = runningThreads.size()-1; i >= 0; i--){
            delete(runningThreads[i]);
        }
        return;
    }
};

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
        std::string reqString = request->ToString();
        std::string message = reqString.erase(reqString.length() -1);

        // Handle get Chat Rooms request
        if (message.find("Get") != std::string::npos){

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
        else if (message.find("Create") != std::string::npos){

            s1->Wait(); // Block Wait
            int portNum = 2001; // Calculate next port number n
            std::string str;
            std::ifstream file(fileID.c_str());
            while(std::getline(file, str)){
                portNum++;
            }

            std::string name = message.substr(request->ToString().find(" ") + 1);

            // Start new chatroom process with port number n
            //TODO
            int p_id = fork();
            if (p_id == 0) {
                ChatProcess chatterbox(portNum);
            }
            else {

                // Return chatroom name and port number in response
                response = "{ name: " + name + ", port: " + std::to_string(portNum) + " }";

                // Add chatroom name and port number to file
                std::ofstream newOut;
                newOut.open(fileID.c_str(), std::fstream::app);
                newOut << response << "\n";
                newOut.close();

                s1->Signal(); // Block Signal
                s2->Signal();
                ByteArray resp(response);
                socket->Write(resp);
            }
        }
        else if (message.find("Join") != std::string::npos){

            std::string name = message.substr(request->ToString().find(" ") + 1);

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
        Semaphore * s1 = new Semaphore("block", 1, true);
        Semaphore * s2 = new Semaphore("mutex", 1, true);


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
//            for (int i = threads.size()-1; i >= 0; i--){
//                if(!threads[i]->running){
//                    delete (threads[i]);
//                    threads.erase(threads.begin()+i);
//                }
//            }
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
        delete s1;
        delete s2;
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
            continue;
        }
        else{
            std::cout << "Invalid input. Try again.." << std::endl;
        }
    } while(run != "X");

    delete mainThread;
    return 0;
}
