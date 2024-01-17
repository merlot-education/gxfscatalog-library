package eu.merloteducation.gxfscataloglibrary.models;

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
