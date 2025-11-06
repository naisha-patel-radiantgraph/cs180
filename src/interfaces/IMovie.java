package interfaces;

/**
 * Public interface for Movie domain objects.
 *
 * This interface mirrors the Movie class described in the project design.
 * Implementations should be simple POJOs that implement these accessors and
 * mutators. Keep implementations immutable where feasible (except for the
 * poster path setter which is expected to be mutable).
 *
 * Notes for implementers:
 * - title should be treated as the unique identifier for display purposes
 *   (the Database class may still use a separate ID if needed).
 * - runtime is expressed in minutes.
 * - posterPath can be a filesystem path or a URL; implementations should not
 *   attempt network access in this interface.
 */
public interface IMovie {

    /**
     * Returns the movie title.
     * @return non-null title string (empty string is allowed only if the implementation permits)
     */
    String getTitle();

    /**
     * Returns the movie genre (e.g. "Action", "Drama").
     * @return genre string, may be null if not set by implementation
     */
    String getGenre();

    /**
     * Returns the content rating (e.g. "G", "PG-13", "R").
     * @return rating string, may be null if not set by implementation
     */
    String getRating();

    /**
     * Returns the runtime of the movie in minutes.
     * @return runtime in minutes (non-negative)
     */
    int getRuntime();

    /**
     * Returns the stored poster path or URL for the movie.
     * @return poster path or URL as a string; may be null if not set
     */
    String getPosterPath();

    /**
     * Sets or updates the poster path for the movie.
     * Implementations should validate or normalize the path if necessary.
     *
     * @param path filesystem path or URL for poster image; may be null to clear the poster
     */
    void setPosterPath(String path);
}
