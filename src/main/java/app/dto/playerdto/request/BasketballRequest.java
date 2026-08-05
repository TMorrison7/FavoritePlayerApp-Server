package app.dto.playerdto.request;

import app.models.abstractEntities.Team;
import app.models.entities.College;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// Lombok automatically hooks into the parent constructor using super(name)

@JsonTypeName("BASKETBALL")
@EqualsAndHashCode(callSuper = true)
public final class BasketballRequest extends PlayerRequest {

}