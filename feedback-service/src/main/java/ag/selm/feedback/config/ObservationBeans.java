package ag.selm.feedback.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.observability.ContextProviderFactory;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;

@Configuration
public class ObservationBeans {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer(
            ObservationRegistry observationRegistry) {
        return builder -> builder.contextProvider(ContextProviderFactory.create(observationRegistry))
                .addCommandListener(new MongoObservationCommandListener(observationRegistry));
    }
}
