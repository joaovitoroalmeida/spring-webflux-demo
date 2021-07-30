package academy.devdojo.webflux.util;

import academy.devdojo.webflux.domain.Anime;

public class AnimeCreator {

    private static final String ANIME_ID = "7f9b7171-6723-4b20-aff2-4a497f6af5a8";

    public static Anime createAnimeToBeSaved(){
        return Anime.builder()
                .animeId("7f9b7171-6723-4b20-aff2-4a497f6af5a8")
                .name("Pokemon")
                .build();
    }

    public static Anime createValidAnime(){
        return Anime.builder()
                .id(1)
                .animeId("7f9b7171-6723-4b20-aff2-4a497f6af5a8")
                .name("Pokemon")
                .build();
    }

    public static Anime createValidUpdateAnime(){
        return Anime.builder()
                .id(1)
                .animeId("7f9b7171-6723-4b20-aff2-4a497f6af5a8")
                .name("Naruto")
                .build();
    }
}
