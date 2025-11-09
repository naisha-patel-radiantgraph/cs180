package movie;

import interfaces.IMovie;

import java.io.Serializable;
import java.util.Objects;

/**
 * Simple POJO implementation of IMovie.
 */
public class Movie implements IMovie, Serializable {

    private final String title;
    private final String genre;
    private final String rating;
    private final int runtime;     // minutes, non-negative
    private String posterPath;     // may be null

    /**
     * Full constructor.
     */
    public Movie(String title, String genre, String rating, int runtime, String posterPath) {
        if (title == null) throw new IllegalArgumentException("title cannot be null");
        if (runtime < 0) throw new IllegalArgumentException("runtime cannot be negative");
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.runtime = runtime;
        this.posterPath = posterPath;
    }

    /**
     * Convenience constructor with minimal required fields.
     */
    public Movie(String title, int runtime) {
        this(title, null, null, runtime, null);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getGenre() {
        return genre;
    }

    @Override
    public String getRating() {
        return rating;
    }

    @Override
    public int getRuntime() {
        return runtime;
    }

    @Override
    public String getPosterPath() {
        return posterPath;
    }

    @Override
    public void setPosterPath(String path) {
        this.posterPath = path;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", rating='" + rating + '\'' +
                ", runtime=" + runtime +
                ", posterPath='" + posterPath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;
        return runtime == movie.runtime &&
                title.equals(movie.title) &&
                Objects.equals(genre, movie.genre) &&
                Objects.equals(rating, movie.rating) &&
                Objects.equals(posterPath, movie.posterPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, genre, rating, runtime, posterPath);
    }
}
