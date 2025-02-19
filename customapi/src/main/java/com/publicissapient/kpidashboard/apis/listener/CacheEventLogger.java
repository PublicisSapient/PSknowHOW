package com.publicissapient.kpidashboard.apis.listener;

import org.apache.commons.lang3.StringUtils;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheEventLogger implements CacheEventListener<Object, Object> {

	@Override
	public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
		int abbreviateLength = 50;
		log.info("Cache event {} with key {}. Old value = {}, New value = {}", cacheEvent.getType(),
				abbreviate(cacheEvent.getKey(), abbreviateLength), abbreviate(cacheEvent.getOldValue(), abbreviateLength),
				abbreviate(cacheEvent.getNewValue(), abbreviateLength));
	}

	private String abbreviate(Object value, int maxLength) {
		return StringUtils.abbreviate(String.valueOf(value), maxLength);
	}
}
