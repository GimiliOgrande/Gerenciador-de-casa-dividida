package ufpb.dcx.house.manager.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ufpb.dcx.house.manager.model.House;

public interface HouseRepository extends JpaRepository<House, Long> {

    @Query("select h from House h join HouseMember m on m.house = h where m.user.id = :userId order by h.createdAt desc")
    List<House> findAllByMemberUserId(@Param("userId") Long userId);
}
