package app.config;

import app.dto.playerdto.PlayerRequest;
import app.models.abstractEntities.Team;
import app.models.entities.College;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDate;

@Configuration
@EnableCaching
public class PlayerCacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "players", "playerSearch", "teams");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10)));
        return cacheManager;
    }

    @Bean
    public KeyGenerator playerSearchKeyGenerator() {
        return (target, method, params) -> {
            PlayerRequest request = (PlayerRequest) params[0];
            Team team = request.getTeam();
            College college = request.getCollege();

            return new PlayerSearchKey(
                    request.getClass(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getDateOfBirth(),
                    request.getBirthState(),
                    request.getBirthCity(),
                    team == null ? null : team.getId(),
                    request.getHighSchool(),
                    college == null ? null : college.getId()
            );
        };
    }

    private record PlayerSearchKey(
            Class<?> requestType,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String birthState,
            String birthCity,
            Long teamId,
            String highSchool,
            Long collegeId
    ) {
    }
}
