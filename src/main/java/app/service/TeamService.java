package app.service;


import app.dto.teamdto.CollegeTeamRequest;
import app.dto.teamdto.ProTeamRequest;
import app.dto.teamdto.TeamRequest;
import app.exception.ResourceNotFoundException;
import app.models.abstractEntities.Team;
import app.models.entities.ProTeam;
import app.repositories.TeamRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class TeamService {

    private final TeamRepository teamRepository;
    private final Validator validator;
    private final ObjectMapper mapper = new ObjectMapper();

    @CacheEvict(cacheNames = "teams", allEntries = true)
    public Team create(TeamRequest request){
        Team team;

        Set<ConstraintViolation<TeamRequest>> violations = validator.validate(request);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }

        if(request instanceof ProTeamRequest proTeamRequest){
            team = ProTeam.builder()
                    .name(proTeamRequest.getName())
                    .city(proTeamRequest.getCity())
                    .state(proTeamRequest.getState())
                    .colors(proTeamRequest.getColors())
                    .division(proTeamRequest.getDivision())
                    .build();
        }
        else if(request instanceof CollegeTeamRequest collegeTeamRequest){
            team = ProTeam.builder()
                    .name(collegeTeamRequest.getName())
                    .city(collegeTeamRequest.getCity())
                    .state(collegeTeamRequest.getState())
                    .colors(collegeTeamRequest.getColors())
                    .division(collegeTeamRequest.getDivision())
                    .build();
        }
        else {
            throw new IllegalArgumentException("Unknown team type");
        }



        return teamRepository.save(team);
    }

    @Cacheable(cacheNames = "teams", key = "#request")
    public List<Team> search(TeamRequest request){
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", request.getName());
        filters.put("city", request.getCity());
        filters.put("state", request.getState());
        filters.put("colors", request.getColors());
        filters.put("division", request.getDivision());

        Specification<Team> spec = (root, query, cb) -> cb.and(
                filters.entrySet().stream()
                        .filter(entry -> entry.getValue() != null)
                        .map(entry -> cb.equal(root.get(entry.getKey()), entry.getValue()))
                        .toArray(jakarta.persistence.criteria.Predicate[]::new)
        );
        return teamRepository.findAll(spec);
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

    @CacheEvict(cacheNames = "teams", allEntries = true)
    public Team updateFull(Long id, TeamRequest request){

        Team currentTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        try {
            return teamRepository.save(mapper.updateValue(currentTeam, request));
        } catch (JsonMappingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid update data", e);
        }
    }

    @CacheEvict(cacheNames = "teams", allEntries = true)
    public Team updatePartial(Long id, TeamRequest request){
        Team currentTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        try {
            return teamRepository.save(mapper.updateValue(currentTeam, request));
        } catch (JsonMappingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid update data", e);
        }
    }

    @CacheEvict(cacheNames = "teams", allEntries = true)
    public void delete(Long id){
        if(!teamRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
        }
        teamRepository.deleteById(id);
    }

}
