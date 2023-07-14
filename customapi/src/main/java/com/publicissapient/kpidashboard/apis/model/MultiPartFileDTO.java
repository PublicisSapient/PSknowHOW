package com.publicissapient.kpidashboard.apis.model;

import lombok.Data;

@Data
public class MultiPartFileDTO {

	private byte[] bytes;
	private String originalFilename;
	private long size;
}