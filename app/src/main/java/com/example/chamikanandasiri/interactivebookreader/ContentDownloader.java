package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ContentDownloader extends AsyncTask {
    private File outputdir, outputTempFile, ar;
    private DownloadContentObject d;
    private URL fileurl;
    private  ToastManager toastManager;
    private Context context;


    public ContentDownloader(Context context, DownloadContentObject d, File bookIDFile) {
        this.outputdir = bookIDFile;
        this.d = d;
        try {
            this.fileurl = new URL((d.getFileURL()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Boolean doInBackground(Object[] objects) {
        try {
//            URL fileurl = new URL("https://res.cloudinary.com/db2rl2mxy/raw/upload/v1588078553/a99101c088dc6d4cc97dd7d306b4a7f753164786.zip");

            URLConnection c = fileurl.openConnection();

            outputTempFile = createFile(outputdir.getAbsolutePath() + "/ar", d.getContId() + ".sfb");
            c.connect();
            InputStream input = c.getInputStream();
            FileOutputStream output = new FileOutputStream(outputTempFile);
            byte[] buffer = new byte[1024];
            int len = 0;
//            int count;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
//                len += count;
            }
//            output.flush();
            output.close();
            input.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return false;
        }
    }

    public File createFile(String parent, String filename) throws IOException {
        File f = new File(parent, filename);
        f.createNewFile();
        return f;

    }

}
