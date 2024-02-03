package KairatTiketBot.TiketBot.repo;

import KairatTiketBot.TiketBot.Service.TicketCount;
import org.springframework.data.repository.CrudRepository;

public interface CountRepo extends CrudRepository<TicketCount,Integer> {
}
