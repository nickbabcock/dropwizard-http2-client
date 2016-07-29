package com.github.arteam.dropwizard.http2.client;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.URI;

/**
 * An implementation of {@link HttpClient} which saves registers timing of calls
 * to external services
 *
 * @author Artem Prigoda (a.prigoda)
 * @since 26.03.16
 */
public class InstrumentedHttpClient extends HttpClient {

    private final MetricRegistry metricRegistry;
    private final String name;
    private final CountListener countListener;

    public InstrumentedHttpClient(HttpClientTransport transport, SslContextFactory sslContextFactory,
                                  MetricRegistry metricRegistry, String name) {
        super(transport, sslContextFactory);
        this.metricRegistry = metricRegistry;
        this.name = name;
        countListener = new CountListener(metricRegistry, name);
    }

    @Override
    protected HttpRequest newHttpRequest(HttpConversation conversation, URI uri) {
        final HttpRequest req = super.newHttpRequest(conversation, uri);
        req.listener(new InstrumentedListener(metricRegistry, name));
        req.listener(countListener);
        return req;
    }

    private static class CountListener extends Request.Listener.Adapter implements Response.CompleteListener {

        private final Counter inflight;
        private final Counter queue;

        CountListener(MetricRegistry metricRegistry, String name) {
            inflight = metricRegistry.counter(MetricRegistry.name(HTTP2Client.class, name, "inflight"));
            queue = metricRegistry.counter(MetricRegistry.name(HTTP2Client.class, name, "on-queue"));
        }

        @Override
        public void onQueued(Request request) {
            queue.inc();
        }

        @Override
        public void onBegin(Request request) {
            queue.dec();
        }

        @Override
        public void onCommit(Request request) {
            inflight.inc();
        }

        @Override
        public void onComplete(Result result) {
            inflight.dec();
        }
    }

    private static class InstrumentedListener extends Request.Listener.Adapter implements
            Response.CompleteListener, Response.BeginListener {

        private final Timer ttfb;
        private final Timer total;
        private final Timer queue;
        private Timer.Context totalContext;
        private Timer.Context ttfbContext;
        private Timer.Context queueContext;

        InstrumentedListener(MetricRegistry metricRegistry, String name) {
            total = metricRegistry.timer(MetricRegistry.name(HTTP2Client.class, name, "total"));
            ttfb = metricRegistry.timer(MetricRegistry.name(HTTP2Client.class, name, "ttfb"));
            queue = metricRegistry.timer(MetricRegistry.name(HTTP2Client.class, name, "queue-wait"));
        }

        @Override
        public void onQueued(Request request) {
            queueContext = queue.time();
        }

        @Override
        public void onBegin(Request request) {
            queueContext.stop();
            totalContext = total.time();
        }

        @Override
        public void onCommit(Request request) {
            ttfbContext = ttfb.time();
        }

        @Override
        public void onBegin(Response response) {
            ttfbContext.stop();
        }

        @Override
        public void onComplete(Result result) {
            totalContext.stop();
        }
    }
}
