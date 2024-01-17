package eu.merloteducation.gxfscataloglibrary.models.client;

import lombok.Getter;

@Getter
public enum QueryLanguage {
    OPENCYPHER("OPENCYPHER"),
    SPARQL("SPARQL"),
    GRAPHQL("GRAPHQL");

    private final String value;

    QueryLanguage(String value) {
        this.value = value;
    }
}
