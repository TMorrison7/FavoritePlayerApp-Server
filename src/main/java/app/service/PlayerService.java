package app.service;

import app.models.abstractEntities.Player;
import app.repositories.PlayerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PlayerService {

    // find player by id
    // find player by first name
    // find player by last name
    // list players by state
    // list players by city
    // list players by high school



    private final PlayerRepository playerRepository;

    public List<Player> getAllPlayers(){
        return playerRepository.findAll();
    }

    public Optional<Player> getPlayerById(int id){
        Optional<Player> player = playerRepository.getReferenceById(id);
        return playerRepository.getReferenceById(id);
    }
}
