package com.github.arteam.dropwizard.http2.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http2.client.HTTP2Client;

public class InstrumentedListener extends Request.Listener.Adapter {

    private final Timer ttfb;
    private final Timer total;
    private final Timer queue;
    private Timer.Context totalContext;
    private Timer.Context ttfbContext;
    private Timer.Context queueContext;

    public InstrumentedListener(MetricRegistry metricRegistry, String name) {
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

    public void onResponseBegin() {
        ttfbContext.stop();
    }

    public void onResponseComplete() {
        totalContext.stop();
    }
}
