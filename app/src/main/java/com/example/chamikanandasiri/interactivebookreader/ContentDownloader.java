package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
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
    private File outputdir, outputTempFile,ar,img;
    private DownloadContentObject d;
    private URL fileurl,imgurl;
    private Context context;


    public ContentDownloader(Context context, DownloadContentObject d, File isbn) {
        this.outputdir=isbn;
        this.d = d;
        try {
            this.fileurl=new URL((d.getFileURL()));
            this.imgurl=new URL((d.getImageURLs()[0]));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected Boolean doInBackground(Object[] objects) {
        try {
//            URL fileurl = new URL("https://res.cloudinary.com/db2rl2mxy/raw/upload/v1588078553/a99101c088dc6d4cc97dd7d306b4a7f753164786.zip");


            Log.d("fIleurl", fileurl.toString());
            Log.d("imgurl",imgurl.toString());

            URLConnection c = fileurl.openConnection();
            Log.d("Outputdir",outputdir.getAbsolutePath()+"ar");
            Log.d("contentname",d.getContName());

            outputTempFile=createFile(outputdir.getAbsolutePath()+"/ar",d.getContName()+".sfb");
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

            URLConnection ic = imgurl.openConnection();
            Log.d("Outputdir",outputdir.getAbsolutePath()+"img");
            Log.d("contentname",d.getContName());

            outputTempFile=createFile(outputdir.getAbsolutePath()+"/img",d.getContName()+".jpg");
            c.connect();
            InputStream inputimg = ic.getInputStream();
            FileOutputStream outputimg = new FileOutputStream(outputTempFile);
            byte[] imgbuffer = new byte[1024];
            int lent = 0;
            while ((len = inputimg.read(imgbuffer)) != -1) {
                outputimg.write(imgbuffer, 0, len);
            }

            outputimg.close();
            inputimg.close();

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

    public File createFile(String parent ,String filename) throws IOException {
        File f =new File(parent,filename);
        f.createNewFile();
        return f;

    }

}
