package movie;

import org.junit.Test;
import static org.junit.Assert.*;
import interfaces.IMovie;

public class MovieTest {

    @Test
    public void testFullConstructorAndGetters() {
        Movie movie = new Movie("Inception", "Sci-Fi", "PG-13", 148, "/path/to/poster.jpg");
        assertEquals("Inception", movie.getTitle());
        assertEquals("Sci-Fi", movie.getGenre());
        assertEquals("PG-13", movie.getRating());
        assertEquals(148, movie.getRuntime());
        assertEquals("/path/to/poster.jpg", movie.getPosterPath());
    }

    @Test
    public void testMinimalConstructor() {
        Movie movie = new Movie("Memento", 113);
        assertEquals("Memento", movie.getTitle());
        assertNull(movie.getGenre());
        assertNull(movie.getRating());
        assertEquals(113, movie.getRuntime());
        assertNull(movie.getPosterPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullTitle() {
        new Movie(null, "Drama", "R", 100, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNegativeRuntime() {
        new Movie("Bad Movie", "Comedy", "PG", -5, null);
    }

    @Test
    public void testSetPosterPath() {
        Movie movie = new Movie("Interstellar", 169);
        assertNull(movie.getPosterPath());
        movie.setPosterPath("/new/poster.jpg");
        assertEquals("/new/poster.jpg", movie.getPosterPath());
        movie.setPosterPath(null);
        assertNull(movie.getPosterPath());
    }

    @Test
    public void testToStringNonNull() {
        Movie movie = new Movie("The Prestige", "Drama", "PG-13", 130, null);
        String s = movie.toString();
        assertTrue(s.contains("The Prestige"));
        assertTrue(s.contains("PG-13"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Movie a = new Movie("Tenet", "Action", "PG-13", 150, "/a.jpg");
        Movie b = new Movie("Tenet", "Action", "PG-13", 150, "/a.jpg");
        Movie c = new Movie("Tenet", "Action", "PG-13", 150, "/b.jpg");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    public void testImplementsIMovie() {
        IMovie movie = new Movie("Dunkirk", 106);
        assertTrue(movie instanceof IMovie);
    }
}
