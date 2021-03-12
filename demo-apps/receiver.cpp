
#include <proton/connection.hpp>
#include <proton/connection_options.hpp>
#include <proton/container.hpp>
#include <proton/delivery.hpp>
#include <proton/link.hpp>
#include <proton/message.hpp>
#include <proton/message_id.hpp>
#include <proton/messaging_handler.hpp>
#include <proton/value.hpp>
#include <proton/receiver.hpp>
#include <proton/source.hpp>
#include <proton/transport.hpp>
#include <proton/ssl.hpp>
#include <iostream>
#include <map>

using proton::ssl_certificate;
using proton::ssl_client_options;


struct receive_handler : public proton::messaging_handler {
	std::string conn_url_{};
	std::string address_{};
	std::string usr_{};
	std::string pwd_{};

	int received_{ 0 };

	void on_container_start(proton::container& cont) override {

		proton::connection_options opts{};
		opts.container_id("cppclient");
		opts.user(usr_);
		opts.password(pwd_);
		opts.sasl_enabled(true);
		//opts.sasl_allowed_mechs("plain");
		//opts.sasl_allow_insecure_mechs(true);
		ssl_certificate keystore("C:/tls/keystore.p12","","password");
		ssl_certificate truststore("C:/tls/truststore.p12", "", "password");
		//ssl_client_options sslclientopts(proton::ssl::ANONYMOUS_PEER);
		ssl_client_options sslclientopts(keystore,"C:/tls/truststore-nopw.p12", proton::ssl::VERIFY_PEER);
		//opts.ssl_client_options(sslclientopts).sasl_allowed_mechs("EXTERNAL");
		opts.ssl_client_options(sslclientopts);
		cont.connect(conn_url_, opts);
		std::cout << "connected\n";

	}

	void on_connection_open(proton::connection& conn) override {
		std::string subject = conn.transport().ssl().remote_subject();
		std::cout << "Outgoing client connection connected via SSL.  Server certificate identity " <<
			subject << "\n"
			<< conn.transport().ssl().cipher() <<"\n"
			<< conn.transport().ssl().ssf() << "\n"
			<< conn.transport().ssl().protocol() << "\n"
			<< std::endl;
		conn.open_receiver(address_);
	}

	void on_receiver_open(proton::receiver& rcv) override {
		std::cout << "RECEIVE: Opened receiver for source address '"
			<< rcv.source().address() << "'\n";
	}

	void on_message(proton::delivery& dlv, proton::message& msg) override {
		std::cout << received_ << " RECEIVE: Received message '" << msg.body() << "  times '\n";
		received_++;
	}
};

int main(int argc, char** argv) {

	receive_handler handler{};
	//handler.conn_url_ = "amqp://192.168.122.1:5672";
	handler.conn_url_ = "amqps://amq-broker-a-acceptor-0-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es:443";
	handler.address_ = "app.queue.a";
	handler.usr_ = "testuser";
	handler.pwd_ = "testpwd";

	proton::container cont{ handler };

	try {
		cont.run();
	}
	catch (const std::exception& e) {
		std::cerr << e.what() << "\n";
		return 1;
	}

	return 0;
}