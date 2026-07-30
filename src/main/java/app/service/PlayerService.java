package app.service;

import app.dto.BasketballPlayerRequestDto;
import app.dto.FootballPlayerRequestDto;
import app.dto.PlayerRequestDto;
import app.exception.ResourceNotFoundException;
import app.models.abstractEntities.Player;
import app.models.entities.BasketballPlayer;
import app.models.entities.FootballPlayer;
import app.repositories.PlayerRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class PlayerService {


    private final PlayerRepository playerRepository;
    private final Validator validator;

    @Transactional
    public Player createPlayer(PlayerRequestDto playerRequestDto){

        Player player;

        if(playerRequestDto instanceof FootballPlayerRequestDto footballDto){
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
        } else if (playerRequestDto instanceof BasketballPlayerRequestDto basketballDto){
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

        Set<ConstraintViolation<Player>> violations = validator.validate(player);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }

        return playerRepository.save(player);
    }
    public List<Player> getAllPlayers(){
        return playerRepository.findAll();
    }

    public Player getPlayerById(long id){
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Player with ID: %d not found.", id)));
    }

    public Player getPlayerByFirstAndLastName(String firstName, String lastName){
        return playerRepository.findByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Player with name: %s %s not found.", firstName, lastName)));
    }

    public List<Player> getPlayersByFirstName(String firstName){
        List<Player> players = playerRepository.findPlayersByFirstName(firstName);

        if(CollectionUtils.isEmpty(players)){
            throw new ResourceNotFoundException(String.format("Players with first name: %s not found.", firstName));
        }
        return players;
    }

    public List<Player> getPlayersByLastName(String lastName){
        List<Player> players = playerRepository.findPlayersByLastName(lastName);

        if(CollectionUtils.isEmpty(players)){
            throw new ResourceNotFoundException(String.format("Players with last name: %s not found.", lastName));
        }
        return players;
    }

    public List<Player> getPlayersByState(String state){
        List<Player> players = playerRepository.findPlayersByBirthState(state);

        if(CollectionUtils.isEmpty(players)){
            throw new ResourceNotFoundException(String.format("No players from state: %s have been found.", state));
        }
        return players;
    }

    public List<Player> getPlayersByCity(String city){
        List<Player> players = playerRepository.findPlayersByBirthCity(city);

        if(CollectionUtils.isEmpty(players)){
            throw new ResourceNotFoundException(String.format("No players from city: %s have been found.", city));
        }
        return players;
    }

    public List<Player> getPlayersByHighSchool(String highSchool){
        List<Player> players = playerRepository.findPlayersByHighSchool(highSchool);

        if(CollectionUtils.isEmpty(players)){
            throw new ResourceNotFoundException(String.format("No players from high school: %s not found.", highSchool));
        }
        return players;
    }








}
