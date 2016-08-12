package com.vaitls.movies;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MovieActivity extends SingleFragmentActivity {
    private static final String FAVORITES_FILE_NAME="movies_favorites.objs";
    private static final String TAG=MovieActivity.class.getSimpleName();
    @Override
    protected Fragment createFragment() {
        Log.d(TAG,"Creating posters fragment");
        return PostersFragment.newInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        MovieDataCache.setKey(getString(R.string.themoviedb_key));
        try(ObjectInputStream ois=
                    new ObjectInputStream(openFileInput(FAVORITES_FILE_NAME))) {
            MovieDataCache.getInstance().addFavorites((int[])ois.readObject());
        }catch (IOException e){
            // Don't care
        }catch (ClassNotFoundException e){
            Log.e(TAG, "wtf: ", e);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try(ObjectOutputStream oos=
                    new ObjectOutputStream(openFileOutput(FAVORITES_FILE_NAME,
                                Context.MODE_PRIVATE))){
            oos.writeObject(MovieDataCache.getInstance().getFavorites());
        }catch(Exception e){
            Log.e(TAG,"wtf: ",e);
        }
    }
}
