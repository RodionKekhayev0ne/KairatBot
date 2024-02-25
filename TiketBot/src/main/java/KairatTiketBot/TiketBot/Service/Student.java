package KairatTiketBot.TiketBot.Service;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.repository.Update;

@Entity
@Table(name = "student")
@Data
public class Student {


    @Id
    private Long id;

    private String name;

    @Column(nullable = false,name = "tickets_count")
    private Integer ticketCount;





}
