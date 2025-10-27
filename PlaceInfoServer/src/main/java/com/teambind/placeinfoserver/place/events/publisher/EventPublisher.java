package com.teambind.placeinfoserver.place.events.publisher;


import com.teambind.placeinfoserver.place.events.event.Event;
import com.teambind.placeinfoserver.place.utils.utils.json.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final JsonUtil jsonUtil;
	
	public void publish(Event event) {
		String json = jsonUtil.toJson(event);
		kafkaTemplate.send(event.getTopic(), json);
	}
}
