package ru.zelinov.entity;

import com.opencsv.bean.CsvBindByPosition;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Product implements Comparable {

    @CsvBindByPosition(position = 0)
    private Integer id;
    @CsvBindByPosition(position = 1)
    private String name;
    @CsvBindByPosition(position = 2)
    private String condition;
    @CsvBindByPosition(position = 3)
    private String state;
    @CsvBindByPosition(position = 4)
    private Float price;

    @Override
    public int compareTo(Object o) {
        return this.price > ((Product) o).price ? 1 : this.price < ((Product) o).price ? -1
                : this.id > ((Product) o).id ? 1 : this.id < ((Product) o).id ? -1
                : this.name.compareTo(((Product) o).name) != 0 ? this.name.compareTo(((Product) o).name)
                : this.condition.compareTo(((Product) o).condition) != 0 ? this.condition.compareTo(((Product) o).condition)
                : this.state.compareTo(((Product) o).state);
    }
}
