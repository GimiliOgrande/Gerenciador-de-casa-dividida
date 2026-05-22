package ufpb.dcx.house.manager.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ufpb.dcx.house.manager.dto.HouseDtos.AddMemberRequest;
import ufpb.dcx.house.manager.dto.HouseDtos.CreateHouseRequest;
import ufpb.dcx.house.manager.dto.HouseDtos.HouseResponse;
import ufpb.dcx.house.manager.dto.HouseDtos.MemberResponse;
import ufpb.dcx.house.manager.exception.ApiException;
import ufpb.dcx.house.manager.model.House;
import ufpb.dcx.house.manager.model.HouseMember;
import ufpb.dcx.house.manager.model.MembershipRole;
import ufpb.dcx.house.manager.model.User;
import ufpb.dcx.house.manager.repository.HouseMemberRepository;
import ufpb.dcx.house.manager.repository.HouseRepository;
import ufpb.dcx.house.manager.repository.UserRepository;

@Service
public class HouseService {

    private final HouseRepository houseRepository;
    private final HouseMemberRepository houseMemberRepository;
    private final UserRepository userRepository;

    public HouseService(
        HouseRepository houseRepository,
        HouseMemberRepository houseMemberRepository,
        UserRepository userRepository
    ) {
        this.houseRepository = houseRepository;
        this.houseMemberRepository = houseMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public HouseResponse createHouse(User currentUser, CreateHouseRequest request) {
        House house = new House();
        house.setName(request.name().trim());
        house.setAddress(trimToNull(request.address()));
        house.setOwner(currentUser);
        House savedHouse = houseRepository.save(house);

        HouseMember ownerMember = new HouseMember();
        ownerMember.setHouse(savedHouse);
        ownerMember.setUser(currentUser);
        ownerMember.setRole(MembershipRole.OWNER);
        houseMemberRepository.save(ownerMember);

        return toHouseResponse(savedHouse);
    }

    @Transactional(readOnly = true)
    public List<HouseResponse> listHouses(User currentUser) {
        return houseRepository.findAllByMemberUserId(currentUser.getId())
            .stream()
            .map(this::toHouseResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public House getAccessibleHouse(Long houseId, User currentUser) {
        House house = houseRepository.findById(houseId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "House was not found."));
        ensureHouseMember(house.getId(), currentUser.getId());
        return house;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(Long houseId, User currentUser) {
        getAccessibleHouse(houseId, currentUser);
        return houseMemberRepository.findAllByHouseIdOrderByJoinedAtAsc(houseId)
            .stream()
            .map(this::toMemberResponse)
            .toList();
    }

    @Transactional
    public MemberResponse addMember(Long houseId, User currentUser, AddMemberRequest request) {
        House house = getAccessibleHouse(houseId, currentUser);
        ensureHouseOwner(houseId, currentUser.getId());

        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User with this email was not found."));

        if (houseMemberRepository.existsByHouseIdAndUserId(houseId, user.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This user is already a house member.");
        }

        HouseMember member = new HouseMember();
        member.setHouse(house);
        member.setUser(user);
        member.setRole(MembershipRole.MEMBER);
        return toMemberResponse(houseMemberRepository.save(member));
    }

    public void ensureHouseMember(Long houseId, Long userId) {
        if (!houseMemberRepository.existsByHouseIdAndUserId(houseId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this house.");
        }
    }

    public void ensureHouseOwner(Long houseId, Long userId) {
        HouseMember member = houseMemberRepository.findByHouseIdAndUserId(houseId, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this house."));
        if (member.getRole() != MembershipRole.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the house owner can do this.");
        }
    }

    public HouseResponse toHouseResponse(House house) {
        return new HouseResponse(house.getId(), house.getName(), house.getAddress(), house.getOwner().getId());
    }

    private MemberResponse toMemberResponse(HouseMember member) {
        User user = member.getUser();
        return new MemberResponse(user.getId(), user.getName(), user.getEmail(), member.getRole());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
