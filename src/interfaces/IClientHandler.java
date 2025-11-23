package interfaces;

public interface IClientHandler {

    /**
     * Starts the handler's main execution loop.
     * Processes incoming client commands until the connection closes.
     */
    void run();
}
