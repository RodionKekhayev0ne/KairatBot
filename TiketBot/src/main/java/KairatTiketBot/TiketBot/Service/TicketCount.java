package KairatTiketBot.TiketBot.Service;


import jakarta.persistence.*;

@Entity
@Table(name = "tickets_count")
public class TicketCount {



    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    public void setCount(Integer count) {
        this.count = count;
    }

    Integer count;

    public Integer getId() {
        return id;
    }

    public Integer getCount() {
        return count;
    }
}
