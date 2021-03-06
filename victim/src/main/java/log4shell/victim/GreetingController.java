package log4shell.victim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
	private static final Logger log = LoggerFactory.getLogger(GreetingController.class);

	@GetMapping("/greeting")
	public String greeting(@RequestParam(value = "name", required = false) String name) {
		if (name == null || name.isBlank()) {
			name = "stranger";
			log.info("Name is empty so used name '{}' instead", name);
		} else {
			log.info("Received name: '{}'", name);
		}

		return String.format("Hello, %s!", name);
	}
}
