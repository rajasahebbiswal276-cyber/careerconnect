package com.careerconnect.dto;

import jakarta.validation.constraints.Size;

public record ApplyRequest(@Size(max = 2000, message = "Cover letter is too long") String coverLetter) {
	
}
