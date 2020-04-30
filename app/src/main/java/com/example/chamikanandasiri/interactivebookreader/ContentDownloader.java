package com.example.chamikanandasiri.interactivebookreader;

import android.os.AsyncTask;
import android.os.Trace;
import android.text.PrecomputedText;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ContentDownloader extends AsyncTask {
    private File outputdir, outputTempFile;
    private DownloadContentObject d;
    private URL fileurl;


    public ContentDownloader(File outputdir, File outputTempFile, DownloadContentObject d, URL fileurl) {
        this.outputdir = outputdir;
        this.outputTempFile = outputTempFile;
        this.d = d;
        this.fileurl=fileurl;
    }
    @Override
    protected Boolean doInBackground(Object[] objects) {
        try {
            URL fileurl = new URL("https://res.cloudinary.com/db2rl2mxy/raw/upload/v1588078553/a99101c088dc6d4cc97dd7d306b4a7f753164786.zip");
//                    URL imageurl=new URL(d.getImageURLs()[0]);
            Log.d("fIleurl", fileurl.toString());

            //Download zip content file
            URLConnection c = fileurl.openConnection();

            c.connect();
            InputStream input = new BufferedInputStream(fileurl.openStream());
            OutputStream output = new FileOutputStream(outputTempFile);

            byte[] buffer = new byte[1024];
            int len = 0;
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                len += count;
            }
            output.flush();
            output.close();
            input.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return false;
        }
    }
}