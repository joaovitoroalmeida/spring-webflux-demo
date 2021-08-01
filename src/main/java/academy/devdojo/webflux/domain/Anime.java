package academy.devdojo.webflux.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@With
@Table("anime")
public class Anime {

    @NotNull
    @NotEmpty(message = "Esse campo não pode ser vazio")
    private String name;

    @Id
    private int id;

    @JsonProperty("anime_id")
    private String animeId = Objects.nonNull(getAnimeId()) ? getAnimeId() : UUID.randomUUID().toString();
}
