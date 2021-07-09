package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class StunContextTest extends Performance {

	@Test
	void testMapping() {
		final StunContext context = StunContext.getInstance();
		int index = 0;
		while(index++ < 5 && SystemConfig.getExternalIPAddress() == null) {
			context.mapping();
			ThreadUtils.sleep(2000);
		}
		assertNotNull(SystemConfig.getExternalIPAddress());
	}
	
}
