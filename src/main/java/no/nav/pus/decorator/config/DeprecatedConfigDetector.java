package no.nav.pus.decorator.config;

import java.io.File;

public class DeprecatedConfigDetector {

    public static void checkForDeprecatedConfig() {
        checkFile("/proxy.json");
        checkFile("/spa.config.json");
    }

    private static void checkFile(String path) {
        if (new File(path).exists()) {
            fail(path);
        }
    }

    private static void fail(String reason) {
        throw new IllegalStateException(""
                + "Heisann!"
                + " Vi har endret på hvordan pus-decorator konfigureres og det ser ut som du fortsatt gjør det på gamlemåten."
                + " Ta en titt på readme-en for å se hvordan det skal gjøres nå."
                + " Beklager ulempene dette medfører, hvis dette kommer veldig ubeleilig kan du vurdere å låse til en gammel versjon."
                + " Dette er nå ugyldig: "
                + reason
        );
    }

}
