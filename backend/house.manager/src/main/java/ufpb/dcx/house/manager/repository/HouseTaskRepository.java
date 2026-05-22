package ufpb.dcx.house.manager.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ufpb.dcx.house.manager.model.HouseTask;

public interface HouseTaskRepository extends JpaRepository<HouseTask, Long> {
    List<HouseTask> findAllByHouseIdOrderByDueDateAsc(Long houseId);
}
