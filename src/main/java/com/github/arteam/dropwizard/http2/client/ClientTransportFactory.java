package com.github.arteam.dropwizard.http2.client;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.Nullable;

/**
 * Date: 11/26/15
 * Time: 10:11 AM
 * <p>
 * A service interface for discovering of factories for {@link HttpClientTransport}.
 * It allows dynamically plug-in different transports to {@link HttpClient}.
 *
 * @author Artem Prigoda
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type",
        defaultImpl = Http2ClientTransportFactory.class)
public interface ClientTransportFactory extends Discoverable {

    @Nullable
    SslContextFactory sslContextFactory();

    HttpClientTransport httpClientTransport(MetricRegistry metricRegistry, String name);
}
