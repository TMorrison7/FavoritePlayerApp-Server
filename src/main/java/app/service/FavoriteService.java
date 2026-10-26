package app.service;

import app.exception.ResourceNotFoundException;
import app.models.Favorite;
import app.models.User;
import app.models.abstractEntities.Player;
import app.repositories.FavoriteRepository;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean addFavorite(String username, Long playerId) {
        if (favoriteRepository.findByUser_UsernameAndPlayer_Id(username, playerId).isPresent()) {
            return false;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player with ID: %d not found.", playerId)));
        favoriteRepository.saveAndFlush(new Favorite(user, player));
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeFavorite(String username, Long playerId) {
        favoriteRepository.deleteByUser_UsernameAndPlayer_Id(username, playerId);
    }

    @Transactional(readOnly = true)
    public List<Player> getFavorites(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        return favoriteRepository.findAllByUser_UsernameOrderById(username).stream()
                .map(Favorite::getPlayer)
                .toList();
    }

    @Transactional
    public void removeFavoriteForUser(String username, Long playerId) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        if (favoriteRepository.findByUser_UsernameAndPlayer_Id(username, playerId).isEmpty()) {
            throw new ResourceNotFoundException(String.format(
                    "Player with ID: %d is not a favorite of user %s.", playerId, username));
        }
        favoriteRepository.deleteByUser_UsernameAndPlayer_Id(username, playerId);
    }
}
