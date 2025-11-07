package movie;

import interfaces.IMovie;

public class Movie implements IMovie {
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getGenre() {
        return "";
    }

    @Override
    public String getRating() {
        return "";
    }

    @Override
    public int getRuntime() {
        return 0;
    }

    @Override
    public String getPosterPath() {
        return "";
    }

    @Override
    public void setPosterPath(String path) {

    }
}
