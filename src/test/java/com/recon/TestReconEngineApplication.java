package com.recon;

import org.springframework.boot.SpringApplication;

public class TestReconEngineApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReconEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
