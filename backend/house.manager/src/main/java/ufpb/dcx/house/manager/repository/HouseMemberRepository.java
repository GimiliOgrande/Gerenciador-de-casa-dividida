package ufpb.dcx.house.manager.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ufpb.dcx.house.manager.model.HouseMember;

public interface HouseMemberRepository extends JpaRepository<HouseMember, Long> {
    boolean existsByHouseIdAndUserId(Long houseId, Long userId);

    List<HouseMember> findAllByHouseIdOrderByJoinedAtAsc(Long houseId);

    Optional<HouseMember> findByHouseIdAndUserId(Long houseId, Long userId);
}
