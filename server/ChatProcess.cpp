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
        ByteArray receivedMessage = ByteArray();
        std::cout<<"Awaiting communtication...\n";
        currentSocket.Read(receivedMessage);
        std::string message = receivedMessage.ToString();
        std::cout<<"Message is " + message<<"\n";
        if(message == "Get"){
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
        else if(message.find("Write") != std::string::npos){
            std::ofstream newOut;
            std::time_t result = std::time(nullptr);
            std::string user = message.substr(0, message.find("Write")-1);
            std::cout<<"Writing to the message";
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
        }

        else if(message == "Kill"){
            std::cout<<"Shutting down server\n";
            processRunning = false;
        }
        else{
            std::cout<<"Unknown transaction\n";
        }
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
                for (int i = runningThreads.size()-1; i >= 0; i--){
                    if(!runningThreads[i]->continueProcess()){
                        this->processrunning=false;
                    }
                    if(!runningThreads[i]->isRunning()){
                        std::cout << "[Stopping and deleting thread " << i << "]\n";
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
        for (int i = runningThreads.size()-1; i >= 0; i--){
            delete(runningThreads[i]);
        }
        return;
    }
};

int main(int arv, char *argv[]){
    int portNumber = atoi(argv[1]);
    ChatProcess chatterbox(portNumber);
}
