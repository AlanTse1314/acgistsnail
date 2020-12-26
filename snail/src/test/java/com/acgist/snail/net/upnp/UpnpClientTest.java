package com.acgist.snail.net.upnp;

import org.junit.jupiter.api.Test;

import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.Performance;

public class UpnpClientTest extends Performance {

	@Test
	public void testMSearch() {
		UpnpClient client = UpnpClient.newInstance();
		client.mSearch();
		this.pause();
	}

	@Test
	public void testRequest() {
		UpnpRequest request = UpnpRequest.newRequest("urn:schemas-upnp-org:service:WANIPConnection:1");
//		String xml = request.buildGetExternalIPAddress();
//		String xml = request.buildGetSpecificPortMappingEntry(8080, Protocol.Type.TCP);
		String xml = request.buildAddPortMapping(8080, NetUtils.LOCAL_HOST_ADDRESS, 8080, Protocol.Type.TCP);
		this.log(xml);
	}
	
	@Test
	public void testResponse() {
		// TODO：多行文本块
		String xml = "<?xml version=\"1.0\"?>"
		+ "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
		+ "	<SOAP-ENV:Body>"
		+ "		<u:GetExternalIPAddressResponse xmlns:u=\"urn:schemas-upnp-org:service:WANIPConnection:1\">"
		+ "			<NewExternalIPAddress>1.2.3.4</NewExternalIPAddress>"
		+ "		</u:GetExternalIPAddressResponse>"
		+ "	</SOAP-ENV:Body>"
		+ "</SOAP-ENV:Envelope>";
		this.log(UpnpResponse.parseGetExternalIPAddress(xml));
	}
	
}
