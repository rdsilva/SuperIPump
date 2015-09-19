package Sockets;

//import com.sun.javafx.runtime.Entry;
import java.net.*;

/**
 *
 * @author jimc
 */
public class SocketClient extends GenericSocket
        implements SocketListener {

    public static String DEFAULT_HOST = "localhost";
    public String host;
    private SocketListener fxListener;

    public SocketClient(SocketListener fxListener,
            String host, int port, int debugFlags) {
        super(port, debugFlags);
        this.host = host;
        this.fxListener = fxListener;
    }

    public SocketClient(SocketListener fxListener) {
        this(fxListener, DEFAULT_HOST, DEFAULT_PORT, DEBUG_NONE);
    }

    public SocketClient(SocketListener fxListener,
            String host, int port) {
        this(fxListener, host, port, DEBUG_NONE);
    }

    /**
     * Called whenever a message is read from the socket. In JavaFX, this method
     * must be run on the main thread and is accomplished by the
     * Entry.deferAction() call. Failure to do so *will* result in strange
     * errors and exceptions.
     *
     * @param line Line of text read from the socket.
     */
    @Override
    public void onMessage(final String line) {
        javafx.application.Platform.runLater(() -> {
            fxListener.onMessage(line);
        });
    }

    /**
     * Called whenever the open/closed status of the Socket changes. In JavaFX,
     * this method must be run on the main thread and is accomplished by the
     * Entry.deferAction() call. Failure to do so will* result in strange errors
     * and exceptions.
     *
     * @param isClosed true if the socket is closed
     */
    @Override
    public void onClosedStatus(final Boolean isClosed) {
        javafx.application.Platform.runLater(() -> {
            fxListener.onClosedStatus(isClosed);
        });
    }

    /**
     * Initialize the SocketClient up to and including issuing the accept()
     * method on its socketConnection.
     *
     * @throws java.net.SocketException
     */
    @Override
    public void initSocketConnection() throws SocketException {
        try {
            socketConnection = new Socket();
            /*
             * Allows the socket to be bound even though a previous
             * connection is in a timeout state.
             */
            socketConnection.setReuseAddress(true);
            /*
             * Create a socket connection to the server
             */
            socketConnection.connect(new InetSocketAddress(host, port));
            if (debugFlagIsSet(DEBUG_STATUS)) {
                System.out.println("Connected to " + host
                        + "at port " + port);
            }
        } catch (Exception e) {
            if (debugFlagIsSet(DEBUG_EXCEPTIONS)) {
                e.printStackTrace();
            }
            throw new SocketException();
        }
    }

    /**
     * For SocketClient class, no additional work is required. Method is null.
     */
    @Override
    public void closeAdditionalSockets() {
    }
}
