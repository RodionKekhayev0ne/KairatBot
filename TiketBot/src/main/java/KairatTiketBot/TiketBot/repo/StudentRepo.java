package KairatTiketBot.TiketBot.repo;

import KairatTiketBot.TiketBot.Service.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepo extends MongoRepository<Student,Long> {
}
