package final_project.oschat;

import org.json.JSONArray;

/**
 * Created by evans on 12/8/2017.
 */

public class postSocketRunnable implements Runnable{
    JSONArray returnedArray = null;             //json array to hold parsed server response

    void setup(JSONArray passedArray){returnedArray = passedArray;}     //load in the server response

    @Override public void run() {}          //method to run in the UIthread as callback
}
