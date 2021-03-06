package no.nav.pus.decorator.proxy;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class ProxyClient extends HttpClient {

    public ProxyClient() {
        super(new SslContextFactory.Client());
        setRequestBufferSize(8192);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        // remove unwanted procotol handlers such as redirection and authentication
        // our proxy should be as transparent as possible
        getProtocolHandlers().clear();
    }

}
