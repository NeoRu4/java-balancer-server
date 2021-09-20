package com.bystray;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

public class Server {

    private final static String IP_ADDRESS = "127.0.0.1";
    private final static int PORT = 8080;
    private final static int TIMEOUT_SECONDS = 15;

    private final static int CORE_POOL = 4;
    private final static int MAX_POOL = 10;
    private final static int QUEUE_CAPACITY = 2;


    private AsynchronousServerSocketChannel server;
    private ThreadPoolExecutor threadPool;

    public void run() {

        try {
            threadPool = new ThreadPoolExecutor(
                    CORE_POOL,
                    MAX_POOL,
                    TIMEOUT_SECONDS,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                    new RejectedExecution());

            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(IP_ADDRESS, PORT));

            System.out.printf("Server works on: http://%s:%s/%n", IP_ADDRESS, PORT);

            boolean runServer = true;

            while (runServer) {
                try {
                    Future<AsynchronousSocketChannel> future = server.accept();
                    AsynchronousSocketChannel clientChannel = future.get();

                    var handler = new HttpMessageHandler(clientChannel);
                    threadPool.submit(handler);

                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                    runServer = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (threadPool != null) {
                threadPool.shutdown();
            }
        }
    }

}

class RejectedExecution implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        System.out.println("Rejected of create thread, added to queue.");
    }
}
