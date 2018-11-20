package chat;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author v.chibrikov
 * <p/>
 * Пример кода для курса на https://stepic.org/
 * <p/>
 * Описание курса и лицензия: https://github.com/vitaly-chibrikov/stepic_java_webserver
 */
@SuppressWarnings ("UnusedDeclaration")
@WebSocket
public class ChatWebSocket {
    private Session session;

    @OnWebSocketConnect
    public void onOpen(Session session) throws InterruptedException {
        this.session = session;
        File res = new File("res");
        sendListOfFiles(res);
        sendString("All files sent");

    }

    @OnWebSocketMessage
    public void onMessage(String data) {
        File file = new File("res/"+data);
        List<String> lines=null;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s: lines) {
            if (s==lines.get(0) || s.isEmpty()){
                continue;
            }
            sendString(s);
            System.out.println(s);
        }
        sendString("All data sent");

    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
    }

    public void sendString(String data) {
        try {
            session.getRemote().sendString(data);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void sendListOfFiles(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                sendListOfFiles(fileEntry);
            } else {
                sendString(fileEntry.getName());
            }
        }
    }
}
