package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Сервер запущен. Ожидание соединений...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Ожидание подключения клиента
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());

                // Обработка клиента в отдельном потоке
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out); // Добавляем нового клиента в список
                }

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Получено от клиента: " + inputLine);
                    broadcastMessage("Клиент: " + inputLine, out); // Передаем сообщение всем клиентам, кроме отправителя
                }
            } catch (IOException e) {
                System.out.println("Ошибка обработки клиента: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    synchronized (clientWriters) {
                        clientWriters.remove(out); // Удаляем клиента из списка при отключении
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка закрытия сокета: " + e.getMessage());
                }
            }
        }

        private void broadcastMessage(String message, PrintWriter sender) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    if (writer != sender) { // Отправляем сообщение всем, кроме отправителя
                        writer.println(message); // Отправка сообщения каждому клиенту
                    }
                }
            }
        }
    }
}
