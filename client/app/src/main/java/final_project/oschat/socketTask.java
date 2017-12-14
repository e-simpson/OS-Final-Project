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

public class socketTask extends AsyncTask<Void, Void, Void> {
    private postSocketRunnable postExecutionRunnable;
    private String result;
    private String query;
    private int port;

    socketTask(String passedQuery, int passedPort, postSocketRunnable passedPostExecutionRunnable) {
        super();
        port = passedPort;
        query = passedQuery;
        postExecutionRunnable = passedPostExecutionRunnable;
    }

    protected Void doInBackground(Void... voids) {
        try {
            Socket socket = new Socket("127.0.0.1", port);
            if (!socket.isConnected()){ return null;}

            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String incoming = null;
            while (incoming == null) {
                incoming = in.readLine();
                if (incoming != null) {
                    result = incoming;
                    break;
                }
            }

            try {
                socket.close();
                out.flush();
                out.close();
                in.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
        catch (Exception e) {e.printStackTrace();}
        return null;
    }

    @Override protected void onPostExecute(Void res) {
        if (result != null && postExecutionRunnable != null){
            JSONArray jsonArray = new JSONArray();
            try {jsonArray = new JSONArray(result);}
            catch (JSONException e) {e.printStackTrace();}
            postExecutionRunnable.setup(jsonArray);
            postExecutionRunnable.run();
        }
    }
}