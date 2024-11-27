package com.publicissapient.kpidashboard.apis.listener;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

@Slf4j
public class CacheEventLogger implements CacheEventListener<Object, Object> {

	@Override
	public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
		log.info("Cache event {} with key {}. Old value = {}, New value = {}", cacheEvent.getType(),
				cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
	}

}
