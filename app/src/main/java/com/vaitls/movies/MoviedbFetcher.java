package com.vaitls.movies;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by evaitl on 7/30/16.
 */
public class MoviedbFetcher {
    private final static String TAG=MoviedbFetcher.class.getSimpleName();

    public byte []getUrlBytes(String urlSpec) throws IOException {
        URL url=new URL(urlSpec);
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        try{
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in=connection.getInputStream();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with "+
                urlSpec);
            }
            int bytesRead=0;
            byte[] buffer=new byte[1500];
            while((bytesRead=in.read(buffer))>0){
                 out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }
    public String getUrlString(String urlSpec) throws  IOException{
        return new String(getUrlBytes(urlSpec));
    }
}
