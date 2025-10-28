package com.teambind.placeinfoserver.place.service.read;


import com.teambind.placeinfoserver.place.repository.PlaceInfoSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceInfoSearchService {
	private final PlaceInfoSearchRepository placeInfoSearchRepository;
	
}
