package academy.devdojo.webflux.integration;

import academy.devdojo.webflux.domain.Anime;
import academy.devdojo.webflux.repository.AnimeRepository;
import academy.devdojo.webflux.util.AnimeCreator;
import academy.devdojo.webflux.util.WebTestClientUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnimeControllerIT {

    @Autowired
    private WebTestClientUtil webTestClientUtil;
    @MockBean
    private AnimeRepository animeRepository;
    private WebTestClient testClientUser;
    private WebTestClient testClientAdmin;
    private WebTestClient testClientInvalid;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup(){
        BlockHound.install();
    }

    @BeforeEach
    public void setUp() {
        testClientUser = webTestClientUtil.authenticateClient("igor", "Joao@123");
        testClientAdmin = webTestClientUtil.authenticateClient("joao", "Joao@123");
        testClientInvalid = webTestClientUtil.authenticateClient("teste", "teste");

        BDDMockito.given(animeRepository.findAll())
                .willReturn(Flux.just(anime));

        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt()))
                .willReturn(Mono.just(anime));

        BDDMockito.given(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
                .willReturn(Mono.just(anime));

        BDDMockito.given(animeRepository
                        .saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .willReturn(Flux.just(anime, anime));

        BDDMockito.given(animeRepository.delete(ArgumentMatchers.any(Anime.class)))
                .willReturn(Mono.empty());

        BDDMockito.given(animeRepository.save(AnimeCreator.createValidAnime()))
                .willReturn(Mono.empty());
    }

    @Order(1)
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

    @Order(2)
    @Test
    @DisplayName("listAll returns a flux of anime")
    public void listAllReturnFLuxOfAnimeWhenSucessful(){

        testClientAdmin.get()
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

    @Order(3)
    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findByIdReturnMonoAnimeWhenSucessful(){

        testClientUser.get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Order(4)
    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    public void findByIdReturnMonoAnimeWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());

        testClientUser.get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatus Happened");
    }

    @Order(5)
    @Test
    @DisplayName("save creates an anime when successful")
    public void saveCreateAnimeWhenSucessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClientAdmin.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Order(6)
    @Test
    @DisplayName("saveBatch creates an anime when successful")
    public void saveBatchCreateAnimeWhenSucessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClientAdmin.post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Order(7)
    @Test
    @DisplayName("save returns mono error with bad request when name is empty")
    public void saveReturnsErrorWhenNameIsEmpty(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

        testClientAdmin.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Order(8)
    @Test
    @DisplayName("delete removes the anime when successful")
    public void deleteRemovesAnimeWhenSucessful(){
        testClientAdmin.delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Order(9)
    @Test
    @DisplayName("delete returns Mono error when anime does not exist")
    public void deleteReturnMonoErrorWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());

        testClientAdmin.delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatus Happened");
    }

    @Order(10)
    @Test
    @DisplayName("update save updated anime and returns empty mono when sucessfull")
    public void updateSaveUpdatedAnimeWhenSucessfull(){
        testClientAdmin.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(AnimeCreator.createValidAnime()))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Order(11)
    @Test
    @DisplayName("update returns Mono error when anime does not exist")
    public void updateReturnMonoErrorWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());
        testClientAdmin.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatus Happened");
    }

    @Order(12)
    @Test
    @DisplayName("saveBatch returns Mono error when one of the objects in the list contains null or empty name")
    public void saveBatchReturnsMonoErrorWhenContainsInvalidName(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        BDDMockito.given(animeRepository
                        .saveAll(ArgumentMatchers.anyIterable()))
                .willReturn(Flux.just(anime, anime.withName("")));

        testClientAdmin.post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Order(13)
    @Test
    @DisplayName("listAll returns forbidden when user is successfully authenticated and does not have role ADMIN")
    public void listAllReturnForbiddenWhenUserDoesNotHaveRoleAdmin(){

        testClientUser.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Order(14)
    @Test
    @DisplayName("listAll returns unauthorized when user is successfully authenticated and does not have role ADMIN")
    public void listAllReturnUnauthorizedWhenUserDoesNotHaveRoleAdmin(){

        testClientInvalid.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}

