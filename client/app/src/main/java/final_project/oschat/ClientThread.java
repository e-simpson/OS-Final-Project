package final_project.oschat;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by evans on 12/8/2017.
 */


public class ClientThread {
    private static final String SERVER = "127.0.0.1";
    private static int PORT;

    private Socket currentSocket;
    private BufferedReader in;
    private PrintWriter out;
    public boolean running = false;


    ClientThread(final int serverPort) {
        PORT = serverPort;
        openSocket();
    }

    public String sendOnSocket(final String message) throws InterruptedException {
        final String[] result = {""};
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentSocket.getOutputStream())), true);
                    out.println(message);
                    in = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
                    running = true;
                    while (running) {
                        String incoming = in.readLine();
                        if (incoming != null) {
                            result[0] = incoming;
                            running = false;
                        }
                    }
                } catch (Exception e) {
                    Log.e("TCP", "C: Error", e);
                } finally {
                    try {
                        out.flush();
                        out.close();
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
        t.join();
        return result[0];
    }

    public void closeSocket() {
        if (currentSocket.isConnected()) {
            try {currentSocket.close();}
            catch (IOException e) {e.printStackTrace();}
        }
    }

    private void openSocket() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress serverAddress = InetAddress.getByName(SERVER);
                    currentSocket = new Socket(serverAddress, PORT);
                } catch (Exception e) {
                    Log.e("TCP", "C: Error", e);
                }
            }
        }).start();
    }
}
