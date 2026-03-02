package com.moongeul.backend.api.setting.repository;

import com.moongeul.backend.api.setting.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
