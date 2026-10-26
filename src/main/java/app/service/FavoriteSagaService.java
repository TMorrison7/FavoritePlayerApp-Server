package app.service;

import app.exception.ResourceNotFoundException;
import app.models.abstractEntities.Player;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FavoriteSagaService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteSagaService.class);

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    public List<Player> addFavorites(String username, List<Long> playerIds) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        List<Long> uniquePlayerIds = playerIds.stream().distinct().toList();
        Set<Long> existingPlayerIds = playerRepository.findAllById(uniquePlayerIds).stream()
                .map(Player::getId)
                .collect(Collectors.toSet());
        for (Long playerId : uniquePlayerIds) {
            if (!existingPlayerIds.contains(playerId)) {
                throw new ResourceNotFoundException(String.format("Player with ID: %d not found.", playerId));
            }
        }

        List<Long> completedSteps = new ArrayList<>();
        try {
            for (Long playerId : uniquePlayerIds) {
                if (favoriteService.addFavorite(username, playerId)) {
                    completedSteps.add(playerId);
                }
            }
            return favoriteService.getFavorites(username);
        } catch (RuntimeException failure) {
            compensate(username, completedSteps, failure);
            throw failure;
        }
    }

    private void compensate(String username, List<Long> completedSteps, RuntimeException failure) {
        for (int i = completedSteps.size() - 1; i >= 0; i--) {
            try {
                favoriteService.removeFavorite(username, completedSteps.get(i));
            } catch (RuntimeException compensationFailure) {
                logger.error("Failed to compensate favorite saga for user {} and player {}",
                        username, completedSteps.get(i), compensationFailure);
                failure.addSuppressed(compensationFailure);
            }
        }
    }
}
