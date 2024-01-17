package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(value = {"content"})
public class SelfDescriptionItem {
    private SelfDescriptionMeta meta;
}
