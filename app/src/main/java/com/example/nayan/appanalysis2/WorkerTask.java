package com.example.nayan.appanalysis2;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Dev on 12/31/2017.
 */

public class WorkerTask extends AsyncTask<String, String, File> {
    @Override
    protected File doInBackground(String... params) {
        File screenshotFile = new File(Environment.getExternalStorageDirectory().getPath(), "appanalysis");
        try {
            Process screencap = Runtime.getRuntime().exec("screencap -p " + screenshotFile.getAbsolutePath());
            screencap.waitFor();
            return screenshotFile;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(File screenshot_file) {
        // Do something with the file.
    }
}
