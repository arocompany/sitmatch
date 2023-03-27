package com.nex.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JpaItemWriter;

import java.util.List;

public class JpaItemListWriter<T> extends JpaItemWriter<List<T>> {

    private JpaItemWriter<T> jpaItemWriter;

    public JpaItemListWriter(JpaItemWriter<T> jpaItemWriter) {
        this.jpaItemWriter = jpaItemWriter;
    }


    @Override
    public void write(Chunk<? extends List<T>> items) {
        Chunk<T> totalItems = new Chunk<>();

        for (List<T> list : items) {
            totalItems.addAll(list);
        }

        jpaItemWriter.write(totalItems);
    }

}