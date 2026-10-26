package app.service;


import app.dto.teamdto.CollegeTeamRequestDto;
import app.dto.teamdto.ProTeamRequestDto;
import app.dto.teamdto.TeamRequestDto;
import app.exception.ResourceNotFoundException;
import app.models.abstractEntities.Team;
import app.models.entities.College;
import app.models.entities.ProTeam;
import app.repositories.TeamRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final Validator validator;


    @CacheEvict(cacheNames = "teams", allEntries = true)
    public Team createTeam(TeamRequestDto teamRequestDto){
        Team team;

        if(teamRequestDto instanceof ProTeamRequestDto proTeamRequestDto){
            team = ProTeam.builder()
                    .name(proTeamRequestDto.getName())
                    .city(proTeamRequestDto.getCity())
                    .state(proTeamRequestDto.getState())
                    .colors(proTeamRequestDto.getColors())
                    .division(proTeamRequestDto.getDivision())
                    .build();
        }
        else if(teamRequestDto instanceof CollegeTeamRequestDto collegeTeamRequestDto){
            team = College.builder()
                    .name(collegeTeamRequestDto.getName())
                    .city(collegeTeamRequestDto.getCity())
                    .state(collegeTeamRequestDto.getState())
                    .colors(collegeTeamRequestDto.getColors())
                    .division(collegeTeamRequestDto.getDivision())
                    .build();
        }
        else {
            throw new IllegalArgumentException("Unknown team type");
        }

        Set<ConstraintViolation<Team>> violations = validator.validate(team);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }

        return teamRepository.save(team);
    }

    @Cacheable(cacheNames = "teams", key = "'all'")
    public List<Team> getAllTeams(){
        return teamRepository.findAll();
    }

    @Cacheable(cacheNames = "teams", key = "#id")
    public Team getTeamById(long id){
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Team with ID: %d not found.", id)));
    }

    @Cacheable(cacheNames = "teams", key = "'name:' + #name")
    public Team getTeamByName(String name){
        return teamRepository.getTeamByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Team with name: %s not found", name)));
    }

}
