package com.feng.lin.pass.nat.comm.message;

public interface PassNatMessageType {
	byte protocol_base = 0x000;
	byte protocol_base_heartbeat = 0x0001;
	byte protocol_base_offline = 0x0002;
	byte protocol_http = 0x001;
	byte protocol_http_request = 0x0010;
	byte protocol_http_response = 0x0011;
}
