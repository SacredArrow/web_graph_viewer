package chat;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
@SuppressWarnings ("UnusedDeclaration")
@WebSocket
public class ChatWebSocket {
    private Session session;
    private int status=0;
    private String temp_name;
//    Status
//    0 - Send list of files
//    1 - Send data of file
//    2 - Delete file
//    3 - Add file - get name
//    4 - Add file - get content
    @OnWebSocketConnect
    public void onOpen(Session session) throws InterruptedException {
        this.session = session;
        File res = new File("res");
        sendListOfFiles(res);


    }

    @OnWebSocketMessage
    public void onMessage(String data) {
        switch (data) {
            case "Files":
                status=0;
                sendListOfFiles(new File("res"));
                break;
            case "Data":
                status=1;
                break;
            case "Delete" :
                status=2;
                break;
            case "Add":
                status=3;
                break;

            default:
                switch (status) {
                    case 1:
                        sendData(new File("res/"+data));
                        break;
                    case 2:
                        new File("res/"+data).delete();
                        sendListOfFiles(new File("res"));
                        break;
                    case 3:
                        temp_name = data;
                        status = 4;
                        break;
                    case 4:
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter("res/" + temp_name);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        out.print(data);
                        out.close();
                        sendListOfFiles(new File("res"));
                }
        }



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
        sendString("All files sent");
    }
    private void sendData(File file) {
        List<String> lines=null;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s: lines) {
            if (s.isEmpty() || !Character.isDigit(s.charAt(0))){
                continue;
            }
            sendString(s);
        }
        sendString("All data sent");
    }
}
