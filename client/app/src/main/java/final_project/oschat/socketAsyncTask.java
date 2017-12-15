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

    private final String host = "192.168.0.35";
//    private final String host = "172.30.152.1";
//    private final String host = "127.0.0.1";



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
            Socket socket = new Socket(host, port);

            if (!socket.isConnected()){ return null;}

            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(query);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            try {
                int charsRead = 0;
                char[] buffer = new char[2048];
                while ((charsRead = in.read(buffer)) != -1) {
                    out.flush();
                    result += new String(buffer).substring(0, charsRead);
                    in.close();
                    out.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.getMessage();
            }

            if (result.substring(0, 4).equals("null")){
                result = result.substring(4);
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return null;
    }

    @Override protected void onPostExecute(Void res) {
        if (result != null){
            JSONArray jsonArray = new JSONArray();
            try {jsonArray = new JSONArray(result);}
            catch (JSONException e) {e.printStackTrace();}

            if (postExecutionRunnable != null){
                postExecutionRunnable.setup(jsonArray);
            }
        }
        if (postExecutionRunnable != null){
            postExecutionRunnable.run();
        }
    }
}