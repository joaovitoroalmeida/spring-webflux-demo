package academy.devdojo.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringWebfluxEssentialsApplication {

//	static {
//		BlockHound.install(builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUId"));
//	}
	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxEssentialsApplication.class, args);
	}

}
