package org.badmintonchain.service.impl;

import lombok.RequiredArgsConstructor;
import org.badmintonchain.model.entity.ServicesEntity;
import org.badmintonchain.repository.ServiceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicesManager {
    private final ServiceRepository repo;
    private final OpenAiClient openAiClient;
    private final JdbcTemplate jdbcTemplate;

    private String toText(ServicesEntity s) {
        return String.format(
                "Dịch vụ: %s. Loại: %s. Giá: %s VND. Mô tả: %s.",
                s.getServiceName(),
                s.getServiceType() != null ? s.getServiceType() : "Không rõ",
                s.getUnitPrice().toPlainString(),
                s.getDescription() != null ? s.getDescription() : "Không có"
        );
    }

    public ServicesEntity createOrUpdate(ServicesEntity s) {
        ServicesEntity saved = repo.save(s);

        // Xoá embedding cũ nếu có
        jdbcTemplate.update("DELETE FROM document_chunks WHERE metadata->>'serviceId' = ?", saved.getId().toString());

        // Tạo embedding mới
        String text = toText(saved);
        float[] embedding = openAiClient.createEmbedding(text);

        jdbcTemplate.update("""
            INSERT INTO document_chunks (id, chunk_text, metadata, embedding)
            VALUES (gen_random_uuid(), ?, ?::jsonb, ?::vector)
        """, text, "{\"source\":\"services\",\"serviceId\":\"" + saved.getId() + "\"}", embedding);

        return saved;
    }

    public void delete(Long id) {
        repo.deleteById(id);
        jdbcTemplate.update("DELETE FROM document_chunks WHERE metadata->>'serviceId' = ?", id.toString());
    }
}

