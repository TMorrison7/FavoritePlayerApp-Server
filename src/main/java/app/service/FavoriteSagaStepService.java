package app.service;

import app.exception.ResourceNotFoundException;
import app.models.Favorite;
import app.models.FavoriteSaga;
import app.models.FavoriteSagaStatus;
import app.models.User;
import app.models.abstractEntities.Player;
import app.repositories.FavoriteRepository;
import app.repositories.FavoriteSagaRepository;
import app.repositories.PlayerRepository;
import app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteSagaStepService {

    private final FavoriteSagaRepository sagaRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public FavoriteSagaExecution begin(String sagaId) {
        FavoriteSaga saga = getSaga(sagaId);
        if (saga.getStatus() == FavoriteSagaStatus.PENDING) {
            saga.setStatus(FavoriteSagaStatus.PROCESSING);
        }
        return new FavoriteSagaExecution(saga.getStatus(), List.copyOf(saga.getPlayerIds()));
    }

    @Transactional
    public void addFavorite(String sagaId, Long playerId) {
        FavoriteSaga saga = getSaga(sagaId);
        if (saga.getStatus() != FavoriteSagaStatus.PROCESSING
                || saga.getCompletedPlayerIds().contains(playerId)) {
            return;
        }

        if (favoriteRepository.findByUser_UsernameAndPlayer_Id(saga.getUsername(), playerId).isPresent()) {
            return;
        }

        User user = userRepository.findByUsername(saga.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + saga.getUsername()));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Player with ID: %d not found.", playerId)));
        favoriteRepository.saveAndFlush(new Favorite(user, player));
        saga.getCompletedPlayerIds().add(playerId);
    }

    @Transactional
    public boolean beginCompensation(String sagaId, String failureMessage) {
        FavoriteSaga saga = getSaga(sagaId);
        if (saga.getStatus() != FavoriteSagaStatus.PROCESSING) {
            return false;
        }
        saga.setFailureMessage(failureMessage == null
                ? null
                : failureMessage.substring(0, Math.min(failureMessage.length(), 2000)));
        saga.setStatus(FavoriteSagaStatus.COMPENSATING);
        return true;
    }

    @Transactional
    public Long compensateNext(String sagaId) {
        FavoriteSaga saga = getSaga(sagaId);
        if (saga.getStatus() != FavoriteSagaStatus.COMPENSATING
                || saga.getCompletedPlayerIds().isEmpty()) {
            return null;
        }

        int lastIndex = saga.getCompletedPlayerIds().size() - 1;
        Long playerId = saga.getCompletedPlayerIds().get(lastIndex);
        favoriteRepository.findByUser_UsernameAndPlayer_Id(saga.getUsername(), playerId)
                .ifPresent(favoriteRepository::delete);
        saga.getCompletedPlayerIds().remove(lastIndex);
        return playerId;
    }

    @Transactional
    public void complete(String sagaId) {
        FavoriteSaga saga = getSaga(sagaId);
        saga.setStatus(FavoriteSagaStatus.COMPLETED);
        saga.setFailureMessage(null);
    }

    @Transactional
    public void fail(String sagaId) {
        FavoriteSaga saga = getSaga(sagaId);
        if (saga.getStatus() == FavoriteSagaStatus.COMPENSATING) {
            saga.setStatus(FavoriteSagaStatus.FAILED);
        }
    }

    private FavoriteSaga getSaga(String sagaId) {
        return sagaRepository.findById(sagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite saga not found: " + sagaId));
    }
}
