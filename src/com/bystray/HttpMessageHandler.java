package com.bystray;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.*;

public class HttpMessageHandler implements Callable<Long> {

    private final static String HEADERS =
            """
            HTTP/1.1 200 OK
            Server: balancer-server
            Content-Type: text/html
            Content-Length: %s
            Connection: close\n
            """;

    private final Integer BUFFER_SIZE = 256;
    private AsynchronousSocketChannel clientChannel;

    HttpMessageHandler(AsynchronousSocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public Long call() throws Exception {
        System.out.println("query thread open: " + Thread.currentThread().getId());
        readClientChannel(clientChannel);
        return Thread.currentThread().getId();
    }

    private void readClientChannel(AsynchronousSocketChannel clientChannel)
            throws InterruptedException, ExecutionException, IOException {

        while (clientChannel != null && clientChannel.isOpen()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            StringBuilder builder = new StringBuilder();
            boolean keepReading = true;

            while (keepReading) {
                clientChannel.read(buffer).get();

                int position = buffer.position();
                keepReading = position == BUFFER_SIZE;

                byte[] array = keepReading
                        ? buffer.array()
                        : Arrays.copyOfRange(buffer.array(), 0, position);

                builder.append(new String(array));
                buffer.clear();
            }

            ByteBuffer resp = ByteBuffer.wrap(this.getPageData().getBytes());

            Thread.sleep(1000);

            clientChannel.write(resp);
            clientChannel.close();
        }
    }

    private String getPageData() {
        String body = "<html><body><h1>Hello, my thread is - %s</h1></body></html>";
        return this.makePageHeaders(String.format(body, Thread.currentThread().getId()));
    }

    private String makePageHeaders(String body) {
        return String.format(HEADERS, body.length()) + body;
    }
}
