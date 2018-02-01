package no.nav.pus.decorator;

import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static no.nav.pus.decorator.FragmentCreator.readTemplate;
import static org.assertj.core.api.Assertions.assertThat;

public class FragmentCreatorTest {

    @Test
    public void createFragmentTemplate(){
        String fragmentTemplate = FragmentCreator.createFragmentTemplate(readTemplate("/fragmentCreatorTest/original.html"));
        System.out.println(fragmentTemplate);
        assertThat(normalize(fragmentTemplate)).isEqualTo(normalize(readTemplate("/fragmentCreatorTest/merged.html")));
    }

    private String normalize(String s) {
        return Arrays.stream(s.split("\n"))
                .map(String::trim)
                .collect(joining("\n"));
    }

}