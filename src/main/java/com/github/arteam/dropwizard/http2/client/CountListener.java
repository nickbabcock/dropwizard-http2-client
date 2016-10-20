package com.github.arteam.dropwizard.http2.client;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http2.client.HTTP2Client;

public class CountListener extends Request.Listener.Adapter {

    private final Counter inflight;
    private final Counter queue;

    public CountListener(MetricRegistry metricRegistry, String name) {
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
    public void onFailure(Request request, Throwable failure) {
        inflight.dec();
    }

    @Override
    public void onSuccess(Request request) {
        inflight.dec();
    }
}