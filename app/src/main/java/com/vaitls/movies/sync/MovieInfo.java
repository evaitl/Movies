package com.vaitls.movies.sync;

/**
 * Created by evaitl on 7/30/16.
 * <p/>
 * The fields are created to match the json information in a themoviedb api calls.
 * <p/>
 * Not all fields are returned with every call, so some will have default values.
 */
class MovieInfo {
    private boolean adult;
    private String backdrop_path;
    private int[] genre_ids;
    private int id;
    private String original_language;
    private String original_title;
    private String overview;
    private String release_date;
    private String poster_path;
    private float popularity;
    private String title;
    private boolean video;
    private float vote_average;
    private int vote_count;


    private String imdb_id;
    private ProductionCompanies[] production_companies;

    private ProductionCountries[] production_countries;
    private long revenue;
    private int runtime;
    private SpokenLanguages[] spoken_languages;
    private String status;
    private String tagline;

    /**
     * Private constructor: Only create these with gson.
     */
    private MovieInfo() {
    }

    /**
     * @return Can we put this in the movies db?
     */
    boolean isGoodData() {
        return id > 0 &&
            poster_path != null &&
            title != null &&
            release_date != null &&
            overview != null &&
            vote_average > 0 &&
            vote_count > 0;
    }

    public String getImdb_id() {
        return imdb_id;
    }

    public ProductionCompanies[] getProduction_companies() {
        return production_companies;
    }

    public ProductionCountries[] getProduction_countries() {
        return production_countries;
    }

    public long getRevenue() {
        return revenue;
    }

    public int getRuntime() {
        return runtime;
    }

    public SpokenLanguages[] getSpoken_languages() {
        return spoken_languages;
    }

    public String getStatus() {
        return status;
    }

    public String getTagline() {
        return tagline;
    }

    // Generated getters.
    public boolean isAdult() {
        return adult;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public int[] getGenre_ids() {
        return genre_ids;
    }

    public int getId() {
        return id;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public String getOverview() {
        return overview;
    }

    public float getPopularity() {
        return popularity;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getTitle() {
        return title;
    }

    public boolean isVideo() {
        return video;
    }

    public float getVote_average() {
        return vote_average;
    }

    public int getVote_count() {
        return vote_count;
    }

    class ProductionCompanies {
        String name;
        int id;
    }

    class ProductionCountries {
        String iso_3166_1;
        String name;
    }

    class SpokenLanguages {
        String iso_639_1;
        String name;
    }
}

