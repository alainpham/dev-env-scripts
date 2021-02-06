package inthematrix;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
// import org.apache.camel.model.rest.RestBindingMode;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

@ApplicationScoped
public class Main extends RouteBuilder {

	@Produces
	@Named("https")
	HttpComponent NoopHostnameVerifier() {
        HttpComponent httpComponent = new HttpComponent();
 
        httpComponent.setX509HostnameVerifier(NoopHostnameVerifier.INSTANCE);
 
 
        TrustManagersParameters trustManagersParameters = new TrustManagersParameters();
        X509ExtendedTrustManager extendedTrustManager = new InsecureX509TrustManager();
        trustManagersParameters.setTrustManager(extendedTrustManager);
 
        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setTrustManagers(trustManagersParameters);
        httpComponent.setSslContextParameters(sslContextParameters);
		return httpComponent;

	}

	private static class InsecureX509TrustManager extends X509ExtendedTrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
 
        }
 
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
 
        }
 
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
 
        }
 
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
 
        }
 
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
 
        }
 
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
 
        }
 
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

	@Override
    public void configure() throws Exception {
        

		// restConfiguration()
		// .apiContextRouteId("api-docs")
		// .bindingMode(RestBindingMode.json)
		// .contextPath("/camel")
		// .apiContextPath("/api-docs")
		// .apiProperty("cors", "true")
		// .apiProperty("api.title", "test")
		// .apiProperty("api.version", "test")
		// .dataFormatProperty("prettyPrint", "true");
        // ;
		


        
    }

}
