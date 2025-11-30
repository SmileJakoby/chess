package server;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Session, Session> connections = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Integer, Session[]> gameConnections = new ConcurrentHashMap<>();

    public void add(Integer gameID, Session session) {
        if (gameConnections.containsKey(gameID)) {
            Session[] previousSessions = gameConnections.get(gameID);
            Session[] newSessions = new Session[previousSessions.length + 1];
            System.arraycopy(previousSessions, 0, newSessions, 0, previousSessions.length);
            newSessions[newSessions.length - 1] = session;
            gameConnections.put(gameID, newSessions);
        }
        else{
            gameConnections.put(gameID, new Session[]{session});
        }
        connections.put(session, session);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, Integer gameID, ServerMessage serverMessage) throws IOException {
        String msg = serverMessage.toString();
        System.out.println("broadcast: " + msg);
        if (gameConnections.containsKey(gameID)) {
            for (Session c : gameConnections.get(gameID)) {
                if (c.isOpen()) {
                    if (!c.equals(excludeSession)) {
                        c.getRemote().sendString(msg);
                        System.out.println("Sent Message");
                    }
                }
            }
        }
        else{
            System.out.println("Game connections does not include gameID of: " + gameID);
        }
    }

    public void broadcastAll(Session excludeSession, ServerMessage serverMessage) throws IOException {
        String msg = serverMessage.toString();
        System.out.println("broadcast: " + msg);
        for (Session c : connections.values()) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                    System.out.println("Sent Message");
                }
            }
        }
    }
}