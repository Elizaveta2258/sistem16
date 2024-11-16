package client;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader consoleInput;

    public ChatClient() {
        try {
            // Подключаемся к серверу
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Подключено к чату. Введите ваше имя:");

            // Инициализируем потоки для общения
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            consoleInput = new BufferedReader(new InputStreamReader(System.in));

            // Запускаем потоки для чтения и записи
            new Thread(new ReadMessage()).start();
            new Thread(new WriteMessage()).start();

        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    private class ReadMessage implements Runnable {
        @Override
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.err.println("Ошибка чтения сообщения: " + e.getMessage());
            } finally {
                closeConnections();
            }
        }
    }

    private class WriteMessage implements Runnable {
        @Override
        public void run() {
            String message;
            try {
                while ((message = consoleInput.readLine()) != null) {
                    out.println(message);
                }
            } catch (IOException e) {
                System.err.println("Ошибка отправки сообщения: " + e.getMessage());
            } finally {
                closeConnections();
            }
        }
    }

    private void closeConnections() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (consoleInput != null) {
                consoleInput.close();
            }
            System.out.println("Соединение закрыто.");
        } catch (IOException e) {
            System.err.println("Ошибка закрытия соединений: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ChatClient();
    }
}