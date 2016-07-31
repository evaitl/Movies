package com.vaitls.movies;

/**
 * Created by evaitl on 7/30/16.
 */
public class MoviePage {
    private int page;
    private MovieInfo[] results;
    private int total_results;
    private int total_pages;

    public int getPage() {
        return page;
    }

    public MovieInfo[] getResults() {
        return results;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public int getTotal_results() {
        return total_results;
    }
}
