package eu.merloteducation.gxfscataloglibrary.models.client;

import lombok.Getter;

@Getter
public enum SelfDescriptionStatus {
    ACTIVE("ACTIVE"),
    EOL("EOL"),
    DEPRECATED("DEPRECATED"),
    REVOKED("REVOKED");

    private final String value;

    SelfDescriptionStatus(String value) {
        this.value = value;
    }
}
