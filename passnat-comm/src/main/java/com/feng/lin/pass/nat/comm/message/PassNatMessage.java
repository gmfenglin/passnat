package com.feng.lin.pass.nat.comm.message;

public class PassNatMessage {
	private int id = 0xabef0101;
	private int length;// ��Ϣ�峤��
	private byte protocol;// Э������
	private byte type;// ��������
	private byte[] body;// ��Ϣ��

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte getProtocol() {
		return protocol;
	}

	public void setProtocol(byte protocol) {
		this.protocol = protocol;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

}
