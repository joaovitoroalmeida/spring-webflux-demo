package academy.devdojo.webflux.service;

import academy.devdojo.webflux.domain.Anime;
import academy.devdojo.webflux.repository.AnimeRepository;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;

    public Flux<Anime> findAll() {
        log.info("Realizando busca de todos os animes do banco de dados");
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(int id){
        log.info("Realizando busca de anime, Id: {}", id);
        return animeRepository.findById(id)
                .switchIfEmpty(monoResponseStatusNotFoundException());
    }

    private Mono<? extends Anime> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

    public Mono<Anime> save(Anime anime) {
        log.info("Salvando anime no banco de dados, Id: {}", anime.getAnimeId());
        return animeRepository.save(anime);
    }

    @Transactional
    public Flux<Anime> saveAll(List<Anime> animes) {
        log.info("Salvando lista de animes {}", animes);
        return animeRepository.saveAll(animes)
                .doOnNext(this::throwResponseStatusExceptionWhenEmptyName);
    }

    private void throwResponseStatusExceptionWhenEmptyName(Anime animes) {
        if(StringUtil.isNullOrEmpty(animes.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Name");
        }
    }

    public Mono<Void> update(Anime anime){
        log.info("Realizando atualizacão de anime, Id: {}", anime.getAnimeId());
        return findById(anime.getId())
                .map(animeFound -> anime.withId(animeFound.getId()))
                .flatMap(animeRepository::save)
                .then();
    }

    public Mono<Void> delete(int id) {
        log.info("Realizando delecão de anime, Id: {}", id);
        return findById(id)
                .flatMap(animeRepository::delete);
    }
}
