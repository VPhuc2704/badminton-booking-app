package org.badmintonchain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {
    private final JdbcTemplate jdbcTemplate;

    public List<String> searchRelevantChunks(float[] embedding, int topK) {
        return jdbcTemplate.query("""
            SELECT chunk_text
            FROM document_chunks
            ORDER BY embedding <=> ?::vector
            LIMIT ?
        """,
                ps -> {
                    ps.setObject(1, embedding);
                    ps.setInt(2, topK);
                },
                (rs, rowNum) -> rs.getString("chunk_text"));
    }
}
