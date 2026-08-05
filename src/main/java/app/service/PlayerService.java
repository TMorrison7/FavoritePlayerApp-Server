package app.service;


import app.dto.playerdto.request.BasketballCreateRequest;
import app.dto.playerdto.request.FootballCreateRequest;
import app.dto.playerdto.request.PlayerCreateRequest;
import app.exception.ResourceNotFoundException;
import app.models.abstractEntities.Player;
import app.models.entities.BasketballPlayer;
import app.models.entities.FootballPlayer;
import app.repositories.PlayerRepository;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class PlayerService {


    private final PlayerRepository playerRepository;
    private final Validator validator;


    public Player create(PlayerCreateRequest request){

        Player player;

        Set<ConstraintViolation<PlayerCreateRequest>> violations = validator.validate(request);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }

        if(request instanceof FootballCreateRequest footballDto){
            player = FootballPlayer.builder()
                    .firstName(footballDto.getFirstName())
                    .lastName(footballDto.getLastName())
                    .dateOfBirth(footballDto.getDateOfBirth())
                    .birthState(footballDto.getBirthState())
                    .birthCity(footballDto.getBirthCity())
                    .team(footballDto.getTeam())
                    .highSchool(footballDto.getHighSchool())
                    .college(footballDto.getCollege())
                    .build();
        } else if (request instanceof BasketballCreateRequest basketballDto){
            player = BasketballPlayer.builder()
                    .firstName(basketballDto.getFirstName())
                    .lastName(basketballDto.getLastName())
                    .dateOfBirth(basketballDto.getDateOfBirth())
                    .birthState(basketballDto.getBirthState())
                    .birthCity(basketballDto.getBirthCity())
                    .team(basketballDto.getTeam())
                    .highSchool(basketballDto.getHighSchool())
                    .college(basketballDto.getCollege())
                    .build();
        } else {
            throw new IllegalArgumentException("Unknown player type");
        }

        return playerRepository.save(player);
    }

    public List<Player> search(PlayerCreateRequest request) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("firstName", request.getFirstName());
        filters.put("lastName", request.getLastName());
        filters.put("dateOfBirth", request.getDateOfBirth());
        filters.put("birthState", request.getBirthState());
        filters.put("birthCity", request.getBirthCity());
        filters.put("team", request.getTeam());
        filters.put("highSchool", request.getHighSchool());
        filters.put("college", request.getCollege());
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

    public Player getPlayerById(long id){
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Player with ID: %d not found.", id)));
    }


}
