package io.github.opensabe.common.location.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpLocation {
    @NotNull
    private String ip;
    @Nullable
    private String city;
    @Nullable
    private String region;
    @Nullable
    private String country;
    @Nullable
    private Double latitude;
    @Nullable
    private Double longitude;
}
