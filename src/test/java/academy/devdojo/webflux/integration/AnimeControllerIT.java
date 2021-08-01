package academy.devdojo.webflux.integration;

import academy.devdojo.webflux.domain.Anime;
import academy.devdojo.webflux.exception.CustomAttributes;
import academy.devdojo.webflux.repository.AnimeRepository;
import academy.devdojo.webflux.service.AnimeService;
import academy.devdojo.webflux.util.AnimeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@WebFluxTest
@Import({AnimeService.class, CustomAttributes.class})
@ExtendWith(SpringExtension.class)
public class AnimeControllerIT {

    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient testClient;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup(){
        BlockHound.install();
    }

    @BeforeEach
    public void setUp() {
        BDDMockito.given(animeRepository.findAll()).willReturn(Flux.just(anime));
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.just(anime));
        BDDMockito.given(animeRepository.save(AnimeCreator.createAnimeToBeSaved())).willReturn(Mono.just(anime));
        BDDMockito.given(animeRepository.delete(ArgumentMatchers.any(Anime.class))).willReturn(Mono.empty());
        BDDMockito.given(animeRepository.save(AnimeCreator.createValidAnime())).willReturn(Mono.empty());
    }

    @Test
    public void bockHoundWorks(){
        try{
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("shoud fail");
        } catch (Exception exception) {
            Assertions.assertTrue(exception.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("listAll returns a flux of anime")
    public void listAllReturnFLuxOfAnimeWhenSucessful(){

        testClient.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);

             // Alternativa de uso no lugar do expect Body List
             /* .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName())
                .jsonPath("$.[0].anime_id").isEqualTo(anime.getAnimeId());
              */
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findByIdReturnMonoAnimeWhenSucessful(){

        testClient.get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    public void findByIdReturnMonoAnimeWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());

        testClient.get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatus Happened");
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void saveCreateAnimeWhenSucessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClient.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("save returns mono error with bad request when name is empty")
    public void saveReturnsErrorWhenNameIsEmpty(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

        testClient.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void deleteRemovesAnimeWhenSucessful(){
        testClient.delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete returns Mono error when anime does not exist")
    public void deleteReturnMonoErrorWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());

        testClient.delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatus Happened");
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when sucessfull")
    public void updateSaveUpdatedAnimeWhenSucessfull(){
        testClient.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(AnimeCreator.createValidAnime()))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exist")
    public void updateReturnMonoErrorWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());
        testClient.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatus Happened");
    }
}

