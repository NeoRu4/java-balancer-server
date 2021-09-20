package com.bystray;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

public class Server {

    private final static String IP_ADDRESS = "127.0.0.1";
    private final static int PORT = 8080;
    private final static int TIMEOUT_SECONDS = 15;

    private AsynchronousServerSocketChannel server;
    private ThreadPoolExecutor threadPool;

    public void run() {

        try {
            threadPool = new ThreadPoolExecutor(2, 10, TIMEOUT_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2));
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(IP_ADDRESS, PORT));

            System.out.printf("Server works on: http://%s:%s/%n", IP_ADDRESS, PORT);

            boolean runServer = true;

            while (runServer) {
                try {
                    Future<AsynchronousSocketChannel> future = server.accept();
                    AsynchronousSocketChannel clientChannel = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    var handler = new HttpMessageHandler(clientChannel);
                    threadPool.submit(handler);

                } catch (TimeoutException timeoutException) {
                    timeoutException.printStackTrace();
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                    runServer = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
