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
//private final String host = "172.30.152.1";

    private final String host = "127.0.0.1";



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
        System.out.println("@@@@@@@@@@@@@0");
        try {
            Socket socket = new Socket(host, port);
            System.out.println("@@@@@@@@@@@@@1" + socket.isConnected());

            if (!socket.isConnected()){ return null;}

            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(query);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("@@@@@@@@@@@@@2 is ready: " + in.ready());


//            try {
//                int charsRead = 0;
//                char[] buffer = new char[2048];
//                while ((charsRead = in.read(buffer)) != -1) {
//                    result += new String(buffer).substring(0, charsRead);
//                }
//            } catch (IOException e) {
//                e.getMessage();
//            }

//            String incoming = null;
//            while (incoming == null) {
//                incoming = in.readLine();
//                if (incoming != null) {
//                    result = incoming;
//                    break;
//                }
//                System.out.println("@@@@@@@@@@@@@" + in.readLine());
//            }
//            System.out.println("@@@@@@@@@@@@@3 " + incoming);

            result = in.readLine();

            System.out.println("@@@@@@@@@@@@@4 " + result);


            try {
                socket.close();
                out.flush();
                out.close();
                in.close();
                System.out.println("@@@@@@@@@@@@@5 " + socket.isConnected());
            }
            catch (IOException e) {e.printStackTrace();}
        }
        catch (Exception e) {e.printStackTrace();}
        return null;
    }

    @Override protected void onPostExecute(Void res) {
        System.out.println("@@@@@@@@@@@@@6 done");
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