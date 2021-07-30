package academy.devdojo.webflux.service;

import academy.devdojo.webflux.domain.Anime;
import academy.devdojo.webflux.repository.AnimeRepository;
import academy.devdojo.webflux.util.AnimeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
public class AnimeServiceTest {

    @InjectMocks
    private AnimeService animeService;
    @Mock
    private AnimeRepository animeRepository;
    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup(){
        BlockHound.install();
    }

    @BeforeEach
    public void setUp(){
        BDDMockito.given(animeRepository.findAll()).willReturn(Flux.just(anime));
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.just(anime));
        BDDMockito.given(animeRepository.save(AnimeCreator.createAnimeToBeSaved())).willReturn(Mono.just(anime));
        BDDMockito.given(animeRepository.delete(ArgumentMatchers.any(Anime.class))).willReturn(Mono.empty());
        BDDMockito.given(animeRepository.save(AnimeCreator.createValidUpdateAnime())).willReturn(Mono.empty());
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
    @DisplayName("findAll returns a flux of anime")
    public void findAllReturnFLuxOfAnimeWhenSucessful(){
        StepVerifier.create(animeService.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findByIdReturnMonoAnimeWhenSucessful(){
        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    public void findByIdReturnMonoAnimeWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());
        StepVerifier.create(animeService.findById(2))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void saveCreateAnimeWhenSucessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        StepVerifier.create(animeService.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void deleteRemovesAnimeWhenSucessful(){
        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns Mono error when anime does not exist")
    public void deleteReturnMonoErrorWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());
        StepVerifier.create(animeService.delete(1))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when sucessful")
    public void updateSaveUpdatedAnimeWhenSucessful(){
        Anime animeToBeSaved = AnimeCreator.createValidUpdateAnime();
        StepVerifier.create(animeService.update(animeToBeSaved))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exist")
    public void updateReturnMonoErrorWhenEmptyMonoIsReturned(){
        BDDMockito.given(animeRepository.findById(ArgumentMatchers.anyInt())).willReturn(Mono.empty());
        Anime animeToBeSaved = AnimeCreator.createValidUpdateAnime();
        StepVerifier.create(animeService.update(animeToBeSaved))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }
}