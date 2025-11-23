package interfaces;

import database.Database;

public interface IServer {

    /**
     * Returns the Database instance used by the server.
     * @return the shared Database object.
     */
    Database getDatabase();

    /**
     * Starts the server's main execution loop.
     * This comes from Runnable and is invoked when the server thread is started.
     */
    void run();

    /**
     * Stops the server and closes the server socket.
     */
    void stop();
}
