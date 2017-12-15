package final_project.oschat;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class socketAsyncTask extends AsyncTask<Void, Void, Void> {
    private postSocketRunnable postExecutionRunnable;
    private String result;
    private String query;
    private int port;

//    private final String host = "192.168.0.35";
//    private final String host = "172.30.152.1";
//    private final String host = "127.0.0.1";
    private final String host = "192.168.2.11";




    socketAsyncTask(String passedQuery, int passedPort) {
        super();
        port = passedPort;
        query = passedQuery;
    }

    socketAsyncTask(String passedQuery, int passedPort, postSocketRunnable passedPostExecutionRunnable) {
        super();
        port = passedPort;
        query = passedQuery;
        postExecutionRunnable = passedPostExecutionRunnable;
    }

    protected Void doInBackground(Void... voids) {
        try {
            //open and connect the socket to the correct port and host
            Socket socket = new Socket(host, port);

            //check if the socket is open and ready
            if (!socket.isConnected()){ return null;}

            //open the socket print writer
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            //write and flush the query
            out.println(query);
            out.flush();

            //open the socket reader
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //try to read and record the socket
            try {
                int charsRead = 0;
                char[] buffer = new char[2048];
                while ((charsRead = in.read(buffer)) != -1) {
                    out.flush();
                    result += new String(buffer).substring(0, charsRead);

                    //close the reader, writer, and socket
                    in.close();
                    out.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.getMessage();
            }

            //remove the null character if there is one (trimming)
            if (result != null && result.length() >= 4 && result.substring(0, 4).equals("null")){
                result = result.substring(4);
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return null;
    }

    @Override protected void onPostExecute(Void res) {
        //if successful response
        if (result != null){
            //parse the string to an array
            JSONArray jsonArray = new JSONArray();
            try {jsonArray = new JSONArray(result);}
            catch (JSONException e) {e.printStackTrace();}

            //if there's a callback, set it up with the json
            if (postExecutionRunnable != null){
                postExecutionRunnable.setup(jsonArray);
            }
        }

        //if there's a callback execute it now
        if (postExecutionRunnable != null){
            postExecutionRunnable.run();
        }
    }
}