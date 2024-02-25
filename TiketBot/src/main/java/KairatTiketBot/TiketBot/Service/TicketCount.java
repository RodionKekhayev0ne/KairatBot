package KairatTiketBot.TiketBot.Service;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "`count`")
@Data
public class TicketCount {



    @Id
    @Column(name = "searchId",nullable = false)
    private Integer id;

    @Column(name = "ticket_count")
    private Integer count;


}
