package no.nav.pus.decorator;

import lombok.SneakyThrows;

import java.net.URL;

public class TestUtils {

    @SneakyThrows
    public static URL url(String url) {
        return new URL(url);
    }

}
