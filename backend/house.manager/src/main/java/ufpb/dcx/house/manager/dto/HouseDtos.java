package ufpb.dcx.house.manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ufpb.dcx.house.manager.model.MembershipRole;

public final class HouseDtos {

    private HouseDtos() {
    }

    public record CreateHouseRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 220) String address
    ) {
    }

    public record AddMemberRequest(
        @NotBlank @Email String email
    ) {
    }

    public record HouseResponse(
        Long id,
        String name,
        String address,
        Long ownerId
    ) {
    }

    public record MemberResponse(
        Long userId,
        String name,
        String email,
        MembershipRole role
    ) {
    }
}
