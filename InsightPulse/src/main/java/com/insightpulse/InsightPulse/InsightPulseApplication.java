package com.insightpulse.InsightPulse;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class InsightPulseApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		String dbUrl = dotenv.get("DATASOURCE_URL");
		String dbUser = dotenv.get("DATASOURCE_USERNAME");
		String dbPass = dotenv.get("DATASOURCE_PASSWORD");

		if (dbUrl != null) System.setProperty("DATASOURCE_URL", dbUrl);
		if (dbUser != null) System.setProperty("DATASOURCE_USERNAME", dbUser);
		if (dbPass != null) System.setProperty("DATASOURCE_PASSWORD", dbPass);

		SpringApplication.run(InsightPulseApplication.class, args);
	}

}
