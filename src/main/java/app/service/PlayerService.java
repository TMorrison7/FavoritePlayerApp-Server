package app.service;


import app.dto.playerdto.BasketballRequest;
import app.dto.playerdto.FootballRequest;
import app.dto.playerdto.PlayerRequest;
import app.exception.ResourceNotFoundException;
import app.models.abstractEntities.Player;
import app.models.entities.BasketballPlayer;
import app.models.entities.FootballPlayer;
import app.repositories.PlayerRepository;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
public class PlayerService {


    private final PlayerRepository playerRepository;
    private final Validator validator;
    private final ObjectMapper mapper = new ObjectMapper();



    public Player create(PlayerRequest request){

        Player player;

        Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(request);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }

        if(request instanceof FootballRequest footballDto){
            player = FootballPlayer.builder()
                    .firstName(footballDto.getFirstName())
                    .lastName(footballDto.getLastName())
                    .dateOfBirth(footballDto.getDateOfBirth())
                    .birthState(footballDto.getBirthState())
                    .birthCity(footballDto.getBirthCity())
                    .team(footballDto.getTeam())
                    .highSchool(footballDto.getHighSchool())
                    .sport(footballDto.getClass().getAnnotation(JsonTypeName.class).value())
                    .build();
        } else if (request instanceof BasketballRequest basketballDto){
            player = BasketballPlayer.builder()
                    .firstName(basketballDto.getFirstName())
                    .lastName(basketballDto.getLastName())
                    .dateOfBirth(basketballDto.getDateOfBirth())
                    .birthState(basketballDto.getBirthState())
                    .birthCity(basketballDto.getBirthCity())
                    .team(basketballDto.getTeam())
                    .highSchool(basketballDto.getHighSchool())
                    .sport(basketballDto.getClass().getAnnotation(JsonTypeName.class).value())
                    .build();
        } else {
            throw new IllegalArgumentException("Unknown player type");
        }

        return playerRepository.save(player);
    }

    public List<Player> search(PlayerRequest request) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("firstName", request.getFirstName());
        filters.put("lastName", request.getLastName());
        filters.put("dateOfBirth", request.getDateOfBirth());
        filters.put("birthState", request.getBirthState());
        filters.put("birthCity", request.getBirthCity());
        filters.put("team", request.getTeam());
        filters.put("highSchool", request.getHighSchool());
        filters.put("sport", request.getClass().getAnnotation(JsonTypeName.class).value());

        Specification<Player> spec = (root, query, cb) -> cb.and(
                filters.entrySet().stream()
                        .filter(entry -> entry.getValue() != null)
                        .map(entry -> cb.equal(root.get(entry.getKey()), entry.getValue()))
                        .toArray(jakarta.persistence.criteria.Predicate[]::new)
        );
        return playerRepository.findAll(spec);
    }
    
    public List<Player> getAllPlayers(){
        return playerRepository.findAll();
    }

    public Player getPlayerById(Long id){
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Player with ID: %d not found.", id)));
    }

    public Player updateFull(Long id, PlayerRequest request){

        Player currentPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        try {
            // 2. Jackson automatically merges the request fields directly into the correct concrete class
            return playerRepository.save(mapper.updateValue(currentPlayer, request));
        } catch (JsonMappingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid update data", e);
        }
    }

    public Player updatePartial(Long id, PlayerRequest request){
        Player currentPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        try {
            return playerRepository.save(mapper.updateValue(currentPlayer, request));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Update failed", e);
        }
    }
    public void delete(Long id){
        if(!playerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
        }
        playerRepository.deleteById(id);

    }
}
