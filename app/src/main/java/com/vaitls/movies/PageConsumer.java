package com.vaitls.movies;

/**
 * Created by evaitl on 7/31/16.
 *
 * Only api 24 and above has java.util.function.Consumer&lt;&gt;, so
 * do a cheapo local version.
 */
public interface PageConsumer {
    public void accept(MoviePage mp);
}
