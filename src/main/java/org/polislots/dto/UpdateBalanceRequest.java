package org.polislots.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateBalanceRequest(
        @NotNull @Min(0) Long balance
) {}
