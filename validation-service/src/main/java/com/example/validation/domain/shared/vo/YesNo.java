package com.example.validation.domain.shared.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public enum YesNo {

	Y("Y"), N("N");
	
	@Getter
	private final String value;
	
}
