package app.dto.playerdto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;

// Lombok automatically hooks into the parent constructor using super(name)

@JsonTypeName("Basketball")
@EqualsAndHashCode(callSuper = true)
public final class BasketballRequest extends PlayerRequest {

}