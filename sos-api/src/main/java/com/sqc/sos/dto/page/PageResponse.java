package com.sqc.sos.dto.page;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    List<T> content;
    PageCustom<T> page;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = new PageCustom<>(page);
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page);
    }
}
