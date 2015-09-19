
package Sockets;

/**
 *
 * @author jimc
 */
public interface SocketListener {
    public void onMessage(String line);
    public void onClosedStatus(Boolean isClosed);
}
