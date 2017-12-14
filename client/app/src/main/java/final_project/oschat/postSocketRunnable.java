package final_project.oschat;

import org.json.JSONArray;

/**
 * Created by evans on 12/8/2017.
 */

public class postSocketRunnable implements Runnable{
    JSONArray returnedArray;

    void setup(JSONArray passedArray){returnedArray = passedArray;}

    @Override public void run() {}
}
