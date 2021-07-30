package academy.devdojo.webflux.service;

import academy.devdojo.webflux.domain.Anime;
import academy.devdojo.webflux.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private static final Logger logger = LogManager.getLogger(AnimeService.class);

    private final AnimeRepository animeRepository;

    public Flux<Anime> findAll() {
        logger.info("Realizando busca de todos os animes do banco de dados");
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(int id){
        logger.info("Realizando busca de anime, Id: {}", id);
        return animeRepository.findById(id)
                .switchIfEmpty(monoResponseStatusNotFoundException());
    }

    private Mono<? extends Anime> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

    public Mono<Anime> save(Anime anime) {
        logger.info("Salvando anime no banco de dados, Id: {}", anime.getAnimeId());
        return animeRepository.save(anime);
    }

    public Mono<Void> update(Anime anime){
        logger.info("Realizando atualizacão de anime, Id: {}", anime.getAnimeId());
        return findById(anime.getId())
                .map(animeFound -> anime.withId(animeFound.getId()))
                .flatMap(animeRepository::save)
                .then();
    }

    public Mono<Void> delete(int id) {
        logger.info("Realizando delecão de anime, Id: {}", id);
        return findById(id)
                .flatMap(animeRepository::delete);
    }
}
